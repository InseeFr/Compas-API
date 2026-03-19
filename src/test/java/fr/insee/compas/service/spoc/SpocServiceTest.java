package fr.insee.compas.service.spoc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    private SpocService newService(
            boolean addBalfOscar, String[] defaultReceivers, String[] defaultReceiverAdjMail) {
        String url = server.baseUrl() + "/spoc";
        return new SpocService(
                "user1", // spoc.username
                "pass1", // compas.service.spoc.password
                url, // spoc.url
                "sender@insee.fr", // sender.mail
                defaultReceivers, // default.receiver.mail
                defaultReceiverAdjMail, // default.receiver.adj.mail
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

    @SuppressWarnings("unchecked")
    private static String getCcHeaderValueFrom(String body) throws Exception {
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> root = om.readValue(body, Map.class);
        Map<String, Object> messageTemplate = (Map<String, Object>) root.get("MessageTemplate");
        if (messageTemplate == null) return null;
        List<Map<String, Object>> headers =
                (List<Map<String, Object>>) messageTemplate.get("Header");
        if (headers == null) return null;

        for (Map<String, Object> h : headers) {
            if ("Cc".equalsIgnoreCase((String) h.get("Name"))) {
                return (String) h.get("Value");
            }
        }
        return null;
    }

    // --- 1) addBalfOscar=true : TO nettoyés, CC = défauts, et CC uniquement dans le header
    @Test
    void sendMail_addMode_mergesCleansAndSends() throws Exception {
        // stub SPOC 200
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200).withBody("OK")));

        SpocService svc =
                newService(true, new String[] {"balf@insee.fr"}, new String[] {"balfadj@insee.fr"});

        Mail mail = new Mail();
        mail.setObject("Sujet X");
        mail.setMessage("Contenu Y");
        // TO = RGA (+ blancs / null / doublon)
        mail.setTo(
                Arrays.asList(
                        "rga@insee.fr", "  ", null, "rga@insee.fr" // doublon + blancs
                        ));
        // CC vide au départ
        mail.setCc(List.of());

        svc.sendMail(mail);

        // 1 seule requête envoyée
        List<LoggedRequest> requests = server.findAll(postRequestedFor(urlEqualTo("/spoc")));
        assertThat(requests).hasSize(1);

        LoggedRequest req = requests.getFirst();

        // En-têtes HTTP
        assertThat(req.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(req.getHeader("Accept")).isEqualTo("application/json");
        String expectedAuth =
                "Basic " + Base64.getEncoder().encodeToString("user1:pass1".getBytes());
        assertThat(req.getHeader("Authorization")).isEqualTo(expectedAuth);

        String body = req.getBodyAsString();

        // Recipients : uniquement les TO nettoyés (ici : rga)
        List<String> addrs = getRecipientAddressesFrom(body);
        assertThat(addrs).containsExactly("rga@insee.fr");

        // Header Cc : contient les défauts
        String ccValue = getCcHeaderValueFrom(body);
        assertThat(ccValue).isEqualTo("balf@insee.fr,balfadj@insee.fr");

        // Champs MessageTemplate
        assertThat(body)
                .contains("\"Sender\":\"sender@insee.fr\"")
                .contains("\"Subject\":\"Sujet X\"")
                .contains("\"Content\":\"Contenu Y\"")
                .contains("\"ContentReference\":\"null\"");
    }

    // --- 2) addBalfOscar=false : écrase les récepteurs par défaut (TO = défauts, CC vide)
    // --- 2) addBalfOscar=false : n'ajoute pas les récepteurs par défaut
    @Test
    void sendMail_replaceMode_doesNotAddDefaultReceivers() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc =
                newService(
                        false, new String[] {"balf@insee.fr"}, new String[] {"balfadj@insee.fr"});

        // Même si on a un defaultReceiver, en mode addBalfOscar=false
        // on n'ajoute PAS "balf@insee.fr" -> seul le TO d'origine est utilisé.
        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(List.of("rga@insee.fr"));
        mail.setCc(List.of());

        svc.sendMail(mail);

        List<LoggedRequest> requests = server.findAll(postRequestedFor(urlEqualTo("/spoc")));
        assertThat(requests).hasSize(1);

        String body = requests.getFirst().getBodyAsString();
        List<String> addrs = getRecipientAddressesFrom(body);
        // En mode "replace=false" actuel : on garde le TO fourni, on ignore les defaults
        assertThat(addrs).containsExactly("rga@insee.fr");

        // Pas de CC automatique dans ce mode
        String ccValue = getCcHeaderValueFrom(body);
        assertThat(ccValue).isNull();
    }

    // --- 3) Aucun destinataire après règles -> pas d’appel HTTP
    @Test
    void sendMail_noReceivers_noCall() {
        // Pas de stub nécessaire si pas d'appel
        SpocService svc = newService(false, new String[] {}, new String[] {}); // pas de défauts

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        // TO avec null + blancs -> sera nettoyé, mais pas de défauts => 0 destinataire
        mail.setTo(Arrays.asList(null, "   "));
        mail.setCc(List.of());

        svc.sendMail(mail);

        List<LoggedRequest> requests = server.findAll(postRequestedFor(urlEqualTo("/spoc")));
        assertThat(requests).isEmpty();
    }

    // --- 4) sendMailTo délègue bien à sendMail (TO = param, CC = défauts si add=true)
    @Test
    void sendMailTo_delegates_andMerges() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc =
                newService(
                        true,
                        new String[] {"default@insee.fr"},
                        new String[] {"defaultadj@insee.fr"});

        svc.sendMailTo(List.of("rga@insee.fr"), "Sub", "Body");

        List<LoggedRequest> requests = server.findAll(postRequestedFor(urlEqualTo("/spoc")));
        assertThat(requests).hasSize(1);

        String body = requests.getFirst().getBodyAsString();
        List<String> addrs = getRecipientAddressesFrom(body);
        // TO uniquement : rga
        assertThat(addrs).containsExactly("rga@insee.fr");

        // CC = defaultReceiver
        String ccValue = getCcHeaderValueFrom(body);
        assertThat(ccValue).isEqualTo("default@insee.fr,defaultadj@insee.fr");
    }

    // --- 5) Réponse 500 : on tente l’envoi (1 requête), pas d’exception levée
    @Test
    void sendMail_http500_logsWarning_noThrow() {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(500).withBody("OOPS")));

        SpocService svc =
                newService(true, new String[] {"balf@insee.fr"}, new String[] {"balfadj@insee.fr"});

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(List.of("rga@insee.fr"));
        mail.setCc(List.of());

        svc.sendMail(mail);

        List<LoggedRequest> requests = server.findAll(postRequestedFor(urlEqualTo("/spoc")));
        assertThat(requests).hasSize(1);
    }
}
