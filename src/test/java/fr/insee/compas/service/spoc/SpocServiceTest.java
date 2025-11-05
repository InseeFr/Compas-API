package fr.insee.compas.service.spoc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import java.util.Base64;

import org.junit.jupiter.api.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import fr.insee.compas.model.mail.Mail;

class SpocServiceTest {

    private WireMockServer server;

    @BeforeEach
    void startWiremock() {
        server = new WireMockServer(options().dynamicPort());
        server.start();
        configureFor("localhost", server.port());
    }

    @AfterEach
    void stopWiremock() {
        if (server != null) server.stop();
    }

    private SpocService newService(boolean addBalfOscar, String... defaultReceivers) {
        String url = server.baseUrl() + "/spoc";
        return new SpocService(
                "user1", // spoc.username
                "pass1", // compas.service.spoc.password
                url, // spoc.url
                "sender@insee.fr", // sender.mail
                defaultReceivers, // default.receiver.mail
                addBalfOscar // receiver.mail.add.balf.oscar
                );
    }

    @SuppressWarnings("unchecked")
    private static List<String> getRecipientAddressesFrom(String body) throws Exception {
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> root = om.readValue(body, Map.class);
        Map<String, Object> recipients = (Map<String, Object>) root.get("Recipients");
        List<Map<String, Object>> list = (List<Map<String, Object>>) recipients.get("Recipient");
        List<String> addrs = new ArrayList<>();
        for (Map<String, Object> r : list) {
            addrs.add((String) r.get("Address"));
        }
        return addrs;
    }

    // --- 1) addBalfOscar=true : fusionne récepteurs (mail + défauts), nettoie doublons/blancs,
    // envoie la requête
    @Test
    void sendMail_addMode_mergesCleansAndSends() throws Exception {
        // stub SPOC 200
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200).withBody("OK")));

        SpocService svc = newService(true, "balf@insee.fr");

        Mail mail =
                new Mail(
                        "Sujet X",
                        "Contenu Y",
                        Arrays.asList(
                                "rga@insee.fr", "  ", null, "rga@insee.fr")); // doublon + blancs
        svc.sendMail(mail);

        // 1 seule requête envoyée
        List<LoggedRequest> requests = server.findAll(postRequestedFor(urlEqualTo("/spoc")));
        assertThat(requests).hasSize(1);

        LoggedRequest req = requests.getFirst();

        // En-têtes
        assertThat(req.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(req.getHeader("Accept")).isEqualTo("application/json");
        String expectedAuth =
                "Basic " + Base64.getEncoder().encodeToString("user1:pass1".getBytes());
        assertThat(req.getHeader("Authorization")).isEqualTo(expectedAuth);

        // Corps JSON : sujets + destinataires fusionnés et dédoublonnés
        String body = req.getBodyAsString();
        List<String> addrs = getRecipientAddressesFrom(body);
        assertThat(addrs).containsExactlyInAnyOrder("rga@insee.fr", "balf@insee.fr");

        // Champs MessageTemplate
        assertThat(body)
                .contains("\"Sender\":\"sender@insee.fr\"")
                .contains("\"Subject\":\"Sujet X\"")
                .contains("\"Content\":\"Contenu Y\"")
                .contains("\"ContentReference\":\"null\"");
    }

    // --- 2) addBalfOscar=false : écrase les récepteurs par défaut
    @Test
    void sendMail_replaceMode_overridesReceivers() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc = newService(false, "balf@insee.fr");

        // Même si on passe "rga@insee.fr", on doit n'envoyer qu'à "balf@insee.fr"
        Mail mail = new Mail("S", "C", List.of("rga@insee.fr"));
        svc.sendMail(mail);

        List<LoggedRequest> requests = server.findAll(postRequestedFor(urlEqualTo("/spoc")));
        assertThat(requests).hasSize(1);

        List<String> addrs = getRecipientAddressesFrom(requests.getFirst().getBodyAsString());
        assertThat(addrs).containsExactly("balf@insee.fr");
    }

    // --- 3) Aucun destinataire après règles -> pas d’appel HTTP
    @Test
    void sendMail_noReceivers_noCall() {
        // Pas de stub nécessaire si pas d'appel
        SpocService svc = newService(false);

        Mail mail = new Mail("S", "C", Arrays.asList(null, "   "));
        svc.sendMail(mail);

        List<LoggedRequest> requests = server.findAll(postRequestedFor(urlEqualTo("/spoc")));
        assertThat(requests).isEmpty();
    }

    // --- 4) sendMailTo délègue bien à sendMail (et fusionne si add=true)
    @Test
    void sendMailTo_delegates_andMerges() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc = newService(true, "default@insee.fr");

        svc.sendMailTo(List.of("rga@insee.fr"), "Sub", "Body");

        List<LoggedRequest> requests = server.findAll(postRequestedFor(urlEqualTo("/spoc")));
        assertThat(requests).hasSize(1);

        List<String> addrs = getRecipientAddressesFrom(requests.getFirst().getBodyAsString());
        assertThat(addrs).containsExactlyInAnyOrder("rga@insee.fr", "default@insee.fr");
    }

    // --- 5) Réponse 500 : on tente l’envoi (1 requête), pas d’exception levée
    @Test
    void sendMail_http500_logsWarning_noThrow() {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(500).withBody("OOPS")));

        SpocService svc = newService(true, "balf@insee.fr");
        svc.sendMail(new Mail("S", "C", List.of("rga@insee.fr")));

        List<LoggedRequest> requests = server.findAll(postRequestedFor(urlEqualTo("/spoc")));
        assertThat(requests).hasSize(1);
    }
}
