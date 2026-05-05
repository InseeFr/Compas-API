package fr.insee.compas.service.spoc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import fr.insee.compas.model.mail.Mail;

import tools.jackson.databind.ObjectMapper;

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
                "user1",
                "pass1",
                url,
                "sender@insee.fr",
                defaultReceivers,
                defaultReceiverAdjMail,
                addBalfOscar);
    }

    /** Service pointant sur un port fermé pour simuler une erreur réseau. */
    private SpocService newBrokenService() {
        return new SpocService(
                "user1",
                "pass1",
                "http://localhost:1", // port fermé → connexion refusée
                "sender@insee.fr",
                new String[] {},
                new String[] {},
                false);
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

    // ═══════════════════════════════════════════════════════════════════════════
    // Tests existants conservés
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    void sendMail_addMode_mergesCleansAndSends() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200).withBody("OK")));

        SpocService svc =
                newService(true, new String[] {"balf@insee.fr"}, new String[] {"balfadj@insee.fr"});

        Mail mail = new Mail();
        mail.setObject("Sujet X");
        mail.setMessage("Contenu Y");
        mail.setTo(Arrays.asList("rga@insee.fr", "  ", null, "rga@insee.fr"));
        mail.setCc(List.of());

        svc.sendMail(mail);

        List<LoggedRequest> requests = server.findAll(postRequestedFor(urlEqualTo("/spoc")));
        assertThat(requests).hasSize(1);

        LoggedRequest req = requests.getFirst();
        assertThat(req.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(req.getHeader("Accept")).isEqualTo("application/json");
        String expectedAuth =
                "Basic " + Base64.getEncoder().encodeToString("user1:pass1".getBytes());
        assertThat(req.getHeader("Authorization")).isEqualTo(expectedAuth);

        String body = req.getBodyAsString();
        assertThat(getRecipientAddressesFrom(body)).containsExactly("rga@insee.fr");
        assertThat(getCcHeaderValueFrom(body)).isEqualTo("balf@insee.fr,balfadj@insee.fr");
        assertThat(body)
                .contains("\"Sender\":\"sender@insee.fr\"")
                .contains("\"Subject\":\"Sujet X\"")
                .contains("\"Content\":\"Contenu Y\"")
                .contains("\"ContentReference\":\"null\"");
    }

    @Test
    void sendMail_replaceMode_doesNotAddDefaultReceivers() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc =
                newService(
                        false, new String[] {"balf@insee.fr"}, new String[] {"balfadj@insee.fr"});

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(List.of("rga@insee.fr"));
        mail.setCc(List.of());

        svc.sendMail(mail);

        String body =
                server.findAll(postRequestedFor(urlEqualTo("/spoc"))).getFirst().getBodyAsString();
        assertThat(getRecipientAddressesFrom(body)).containsExactly("rga@insee.fr");
        assertThat(getCcHeaderValueFrom(body)).isNull();
    }

    @Test
    void sendMail_noReceivers_noCall() {
        SpocService svc = newService(false, new String[] {}, new String[] {});

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(Arrays.asList(null, "   "));
        mail.setCc(List.of());

        svc.sendMail(mail);

        assertThat(server.findAll(postRequestedFor(urlEqualTo("/spoc")))).isEmpty();
    }

    @Test
    void sendMailTo_delegates_andMerges() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc =
                newService(
                        true,
                        new String[] {"default@insee.fr"},
                        new String[] {"defaultadj@insee.fr"});

        svc.sendMailTo(List.of("rga@insee.fr"), "Sub", "Body");

        String body =
                server.findAll(postRequestedFor(urlEqualTo("/spoc"))).getFirst().getBodyAsString();
        assertThat(getRecipientAddressesFrom(body)).containsExactly("rga@insee.fr");
        assertThat(getCcHeaderValueFrom(body)).isEqualTo("default@insee.fr,defaultadj@insee.fr");
    }

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

        assertThat(server.findAll(postRequestedFor(urlEqualTo("/spoc")))).hasSize(1);
    }

    /** Mail null → aucun appel HTTP, pas d'exception. */
    @Test
    void sendMail_nullMail_noCallAndNoException() {
        SpocService svc = newService(false, new String[] {"balf@insee.fr"}, new String[] {});

        assertThatNoException().isThrownBy(() -> svc.sendMail(null));

        assertThat(server.findAll(postRequestedFor(urlEqualTo("/spoc")))).isEmpty();
    }

    /** addBalfOscar=false + TO vide → TO = defaultReceivers, CC = defaultReceiverAdjMail. */
    @Test
    void sendMail_replaceMode_toEmpty_usesDefaultReceivers() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc =
                newService(
                        false, new String[] {"balf@insee.fr"}, new String[] {"balfadj@insee.fr"});

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(List.of()); // TO vide → fallback sur les défauts
        mail.setCc(List.of());

        svc.sendMail(mail);

        String body =
                server.findAll(postRequestedFor(urlEqualTo("/spoc"))).getFirst().getBodyAsString();
        // TO = defaultReceiverMail
        assertThat(getRecipientAddressesFrom(body)).containsExactly("balf@insee.fr");
        // CC = defaultReceiverAdjMail
        assertThat(getCcHeaderValueFrom(body)).isEqualTo("balfadj@insee.fr");
    }

    /** addBalfOscar=false + TO vide + aucun défaut → aucun destinataire → pas d'appel. */
    @Test
    void sendMail_replaceMode_toEmptyAndNoDefaults_noCall() {
        SpocService svc = newService(false, new String[] {}, new String[] {});

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(List.of());
        mail.setCc(List.of());

        svc.sendMail(mail);

        assertThat(server.findAll(postRequestedFor(urlEqualTo("/spoc")))).isEmpty();
    }

    /** CC fourni dans le mail + addBalfOscar=false et TO présent → CC conservé dans le header. */
    @Test
    void sendMail_replaceMode_withInitialCc_ccIsKept() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc = newService(false, new String[] {}, new String[] {});

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(List.of("rga@insee.fr"));
        mail.setCc(List.of("cc1@insee.fr", "cc2@insee.fr"));

        svc.sendMail(mail);

        String body =
                server.findAll(postRequestedFor(urlEqualTo("/spoc"))).getFirst().getBodyAsString();
        assertThat(getRecipientAddressesFrom(body)).containsExactly("rga@insee.fr");
        // CC du mail conservé
        String ccValue = getCcHeaderValueFrom(body);
        assertThat(ccValue).contains("cc1@insee.fr").contains("cc2@insee.fr");
    }

    /** Doublons dans TO et CC → dédupliqués avant envoi. */
    @Test
    void sendMail_deduplicatesRecipientsInToAndCc() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc = newService(false, new String[] {}, new String[] {});

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(Arrays.asList("rga@insee.fr", "rga@insee.fr", " rga@insee.fr "));
        mail.setCc(Arrays.asList("cc@insee.fr", "cc@insee.fr"));

        svc.sendMail(mail);

        String body =
                server.findAll(postRequestedFor(urlEqualTo("/spoc"))).getFirst().getBodyAsString();
        assertThat(getRecipientAddressesFrom(body)).containsExactly("rga@insee.fr");
        assertThat(getCcHeaderValueFrom(body)).isEqualTo("cc@insee.fr");
    }

    /**
     * addBalfOscar=true : les défauts sont ajoutés en CC même si CC initial est non vide. Les
     * doublons entre CC initial et défauts sont dédupliqués.
     */
    @Test
    void sendMail_addMode_mergesDefaultsWithExistingCc() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc =
                newService(true, new String[] {"balf@insee.fr"}, new String[] {"balfadj@insee.fr"});

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(List.of("rga@insee.fr"));
        // CC initial contient déjà balf → le doublon doit être dédupliqué
        mail.setCc(Arrays.asList("existing@insee.fr", "balf@insee.fr"));

        svc.sendMail(mail);

        String body =
                server.findAll(postRequestedFor(urlEqualTo("/spoc"))).getFirst().getBodyAsString();
        String ccValue = getCcHeaderValueFrom(body);
        // existing + balf (dédupliqué) + balfadj
        assertThat(ccValue).contains("existing@insee.fr");
        assertThat(ccValue).contains("balf@insee.fr");
        assertThat(ccValue).contains("balfadj@insee.fr");
        // balf ne doit apparaître qu'une seule fois
        assertThat(ccValue.split("balf@insee.fr", -1).length - 1).isEqualTo(1);
    }

    /** sendMailTo avec receivers null → pas d'exception, TO vide → fallback défauts. */
    @Test
    void sendMailTo_nullReceivers_noException() {
        // Pas de défauts non plus → aucun appel
        SpocService svc = newService(false, new String[] {}, new String[] {});

        assertThatNoException().isThrownBy(() -> svc.sendMailTo(null, "Sub", "Body"));

        assertThat(server.findAll(postRequestedFor(urlEqualTo("/spoc")))).isEmpty();
    }

    /** Erreur réseau (connexion refusée) → pas d'exception propagée. */
    @Test
    void sendMail_networkError_noException() {
        SpocService svc = newBrokenService();

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(List.of("rga@insee.fr"));
        mail.setCc(List.of());

        assertThatNoException().isThrownBy(() -> svc.sendMail(mail));
    }

    /** getDefaultReceivers retourne une copie défensive de la liste configurée. */
    @Test
    void getDefaultReceivers_returnsConfiguredList() {
        SpocService svc =
                newService(false, new String[] {"a@insee.fr", "b@insee.fr"}, new String[] {});

        List<String> receivers = svc.getDefaultReceivers();

        assertThat(receivers).containsExactly("a@insee.fr", "b@insee.fr");
        // copie défensive : modification ne doit pas affecter le service
        receivers.add("pirate@evil.com");
        assertThat(svc.getDefaultReceivers()).containsExactly("a@insee.fr", "b@insee.fr");
    }

    /** getDefaultReceiverAdjMail retourne une copie défensive de la liste configurée. */
    @Test
    void getDefaultReceiverAdjMail_returnsConfiguredList() {
        SpocService svc = newService(false, new String[] {}, new String[] {"adj@insee.fr"});

        List<String> adjReceivers = svc.getDefaultReceiverAdjMail();

        assertThat(adjReceivers).containsExactly("adj@insee.fr");
        adjReceivers.add("pirate@evil.com");
        assertThat(svc.getDefaultReceiverAdjMail()).containsExactly("adj@insee.fr");
    }

    /** Réponse 4xx (ex: 400) → pas d'exception, 1 seule requête envoyée. */
    @Test
    void sendMail_http400_logsAndDoesNotThrow() {
        stubFor(
                post(urlEqualTo("/spoc"))
                        .willReturn(aResponse().withStatus(400).withBody("Bad Request")));

        SpocService svc = newService(false, new String[] {}, new String[] {});

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(List.of("rga@insee.fr"));
        mail.setCc(List.of());

        assertThatNoException().isThrownBy(() -> svc.sendMail(mail));
        assertThat(server.findAll(postRequestedFor(urlEqualTo("/spoc")))).hasSize(1);
    }

    /** TO null dans Mail (pas de liste) → traité comme liste vide. */
    @Test
    void sendMail_toNull_treatedAsEmpty_fallbackToDefaults() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc = newService(false, new String[] {"balf@insee.fr"}, new String[] {});

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(null); // null explicite
        mail.setCc(null);

        svc.sendMail(mail);

        String body =
                server.findAll(postRequestedFor(urlEqualTo("/spoc"))).getFirst().getBodyAsString();
        // TO null → vide → fallback sur defaultReceiverMail
        assertThat(getRecipientAddressesFrom(body)).containsExactly("balf@insee.fr");
    }

    /** Constructeur filtre les valeurs blank dans les tableaux de défauts. */
    @Test
    void constructor_filtersBlankDefaultReceivers() {
        SpocService svc =
                newService(
                        false,
                        new String[] {"valid@insee.fr", "  ", "", null},
                        new String[] {"  ", "adjvalid@insee.fr"});

        assertThat(svc.getDefaultReceivers()).containsExactly("valid@insee.fr");
        assertThat(svc.getDefaultReceiverAdjMail()).containsExactly("adjvalid@insee.fr");
    }

    /** Mail avec pièce jointe → requête multipart/form-data avec boundary. */
    @Test
    void sendMail_withAttachment_sendsMultipartRequest() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc = newService(false, new String[] {}, new String[] {});

        // Créer un fichier temporaire comme pièce jointe
        java.io.File tmpFile = java.io.File.createTempFile("test-attachment", ".txt");
        tmpFile.deleteOnExit();
        java.nio.file.Files.writeString(tmpFile.toPath(), "contenu de la pièce jointe");

        Mail mail = new Mail();
        mail.setObject("Sujet avec PJ");
        mail.setMessage("Corps du mail");
        mail.setTo(List.of("rga@insee.fr"));
        mail.setCc(List.of());
        mail.setAttachments(List.of(tmpFile));

        svc.sendMail(mail);

        List<LoggedRequest> requests = server.findAll(postRequestedFor(urlEqualTo("/spoc")));
        assertThat(requests).hasSize(1);

        LoggedRequest req = requests.getFirst();

        // Content-Type doit être multipart/form-data avec un boundary
        String contentType = req.getHeader("Content-Type");
        assertThat(contentType).startsWith("multipart/form-data").contains("boundary=");

        // Le corps doit contenir la partie JSON (request)
        String body = req.getBodyAsString();
        assertThat(body)
                .contains("Content-Disposition: form-data; name=\"request\"")
                .contains("Content-Type: application/json")
                .contains("Sujet avec PJ")
                .contains("rga@insee.fr");

        // Le corps doit contenir la partie fichier (attachments)
        assertThat(body)
                .contains("Content-Disposition: form-data; name=\"attachments\"")
                .contains("filename=\"" + tmpFile.getName() + "\"")
                .contains("contenu de la pièce jointe");

        // Le nom du fichier doit apparaître dans le payload JSON (champ Attachments)
        assertThat(body).contains(tmpFile.getName());
    }

    /** Mail avec plusieurs pièces jointes → toutes présentes dans le multipart. */
    @Test
    void sendMail_withMultipleAttachments_allIncludedInMultipart() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc = newService(false, new String[] {}, new String[] {});

        java.io.File file1 = java.io.File.createTempFile("pj1", ".txt");
        java.io.File file2 = java.io.File.createTempFile("pj2", ".txt");
        file1.deleteOnExit();
        file2.deleteOnExit();
        java.nio.file.Files.writeString(file1.toPath(), "contenu fichier 1");
        java.nio.file.Files.writeString(file2.toPath(), "contenu fichier 2");

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(List.of("rga@insee.fr"));
        mail.setCc(List.of());
        mail.setAttachments(List.of(file1, file2));

        svc.sendMail(mail);

        String body =
                server.findAll(postRequestedFor(urlEqualTo("/spoc"))).getFirst().getBodyAsString();

        // Les deux fichiers doivent être présents
        assertThat(body)
                .contains("filename=\"" + file1.getName() + "\"")
                .contains("contenu fichier 1")
                .contains("filename=\"" + file2.getName() + "\"")
                .contains("contenu fichier 2");

        // Les deux noms doivent figurer dans le JSON (champ Attachments du recipient)
        assertThat(body).contains(file1.getName()).contains(file2.getName());
    }

    /** Liste de pièces jointes null → traité comme liste vide → requête JSON simple. */
    @Test
    void sendMail_nullAttachments_sendsJsonRequest() throws Exception {
        stubFor(post(urlEqualTo("/spoc")).willReturn(aResponse().withStatus(200)));

        SpocService svc = newService(false, new String[] {}, new String[] {});

        Mail mail = new Mail();
        mail.setObject("S");
        mail.setMessage("C");
        mail.setTo(List.of("rga@insee.fr"));
        mail.setCc(List.of());
        mail.setAttachments(null); // null → List.of() via Optional

        svc.sendMail(mail);

        LoggedRequest req = server.findAll(postRequestedFor(urlEqualTo("/spoc"))).getFirst();

        // Sans PJ → Content-Type JSON simple
        assertThat(req.getHeader("Content-Type")).isEqualTo("application/json");
    }
}
