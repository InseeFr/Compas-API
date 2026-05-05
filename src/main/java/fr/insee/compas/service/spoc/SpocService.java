package fr.insee.compas.service.spoc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.compas.model.mail.Mail;

import lombok.extern.slf4j.Slf4j;

/**
 * Service d'envoi d'emails via l'API SPOC.
 *
 * <p>Corps attendu par SPOC : { "MessageTemplate": { "Sender": "expediteur@insee.fr", "Subject":
 * "Objet", "Content": "Contenu texte", "ContentReference": "null" }, "Recipients": { "Recipient": [
 * {"Address": "dest1@insee.fr","Attachements":[]}, {"Address": "dest2@insee.fr","Attachements":[]}
 * ] } }
 */
@Service
@Slf4j
public class SpocService {
    private static final String APPLICATION_JSON = "application/json";
    private static final String CRLF = "\r\n";

    private final String spocUsername;
    private final String spocPassword;
    private final String spocApiUrl;
    private final String senderMail;
    private final List<String> defaultReceiverMail;
    private final List<String> defaultReceiverAdjMail;
    private final boolean addBalfOscar;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public SpocService(
            @Value("${spoc.username}") String spocUsername,
            @Value("${compas.service.spoc.password}") String spocPassword,
            @Value("${spoc.url}") String spocApiUrl,
            @Value("${sender.mail}") String senderMail,
            @Value("${default.receiver.mail}") String[] defaultReceiverMail,
            @Value("${default.receiver.adj.mail}") String[] defaultReceiverAdjMail,
            @Value("${receiver.mail.add.balf.oscar}") boolean addBalfOscar) {

        this.spocUsername = Objects.requireNonNull(spocUsername, "spoc.username manquant");
        this.spocPassword = Objects.requireNonNull(spocPassword, "spoc.password manquant");
        this.spocApiUrl = Objects.requireNonNull(spocApiUrl, "spoc.url manquant");
        this.senderMail = Objects.requireNonNull(senderMail, "sender.mail manquant");
        this.defaultReceiverMail =
                Arrays.stream(Optional.ofNullable(defaultReceiverMail).orElse(new String[0]))
                        .filter(s -> s != null && !s.isBlank())
                        .toList();
        this.defaultReceiverAdjMail =
                Arrays.stream(Optional.ofNullable(defaultReceiverAdjMail).orElse(new String[0]))
                        .filter(s -> s != null && !s.isBlank())
                        .toList();
        this.addBalfOscar = addBalfOscar;

        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .followRedirects(Redirect.NORMAL)
                        .build();
    }

    public void sendMail(Mail mail) {
        if (mail == null) {
            log.warn("Mail null : envoi annulé");
            return;
        }

        List<String> to = new ArrayList<>(Optional.ofNullable(mail.getTo()).orElse(List.of()));
        List<String> cc = new ArrayList<>(Optional.ofNullable(mail.getCc()).orElse(List.of()));

        if (addBalfOscar) {
            cc.addAll(defaultReceiverMail);
            cc.addAll(defaultReceiverAdjMail);
        } else if (to.isEmpty()) {
            to.addAll(defaultReceiverMail);
            cc.addAll(defaultReceiverAdjMail);
        }

        to = cleanList(to);
        cc = cleanList(cc);

        if (Stream.concat(to.stream(), cc.stream()).filter(s -> !s.isBlank()).findAny().isEmpty()) {
            log.warn("Aucun destinataire -> envoi annulé");
            return;
        }

        List<File> files = Optional.ofNullable(mail.getAttachments()).orElse(List.of());
        List<String> fileNames = files.stream().map(File::getName).toList();

        Map<String, Object> payload =
                buildSpocPayload(
                        senderMail, mail.getObject(), mail.getMessage(), fileNames, to, cc);

        String bodyJson;
        try {
            bodyJson = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Erreur sérialisation JSON SPOC", e);
            return;
        }

        log.debug("Payload SPOC : {}", bodyJson);

        try {
            HttpRequest request =
                    files.isEmpty()
                            ? buildJsonRequest(bodyJson)
                            : buildMultipartRequest(bodyJson, files);

            var response = httpClient.send(request, BodyHandlers.ofString());
            log.info("SPOC -> HTTP {}", response.statusCode());
            log.debug("Réponse SPOC : {}", response.body());

            if (response.statusCode() >= 400) {
                log.warn(
                        "Echec envoi SPOC (HTTP {}). Corps: {}",
                        response.statusCode(),
                        response.body());
            }
        } catch (IOException e) {
            log.error("Erreur IO lors de l'envoi SPOC", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Envoi SPOC interrompu", e);
        }
    }

    public void sendMailTo(List<String> receivers, String subject, String content) {
        Mail mail = new Mail();
        mail.setObject(subject);
        mail.setMessage(content);
        mail.setTo(receivers == null ? List.of() : receivers);
        mail.setCc(List.of());
        sendMail(mail);
    }

    private HttpRequest buildJsonRequest(String bodyJson) {
        return HttpRequest.newBuilder()
                .uri(URI.create(spocApiUrl))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", APPLICATION_JSON)
                .header("Content-Type", APPLICATION_JSON)
                .header("Authorization", basicAuth(spocUsername, spocPassword))
                .POST(BodyPublishers.ofString(bodyJson, StandardCharsets.UTF_8))
                .build();
    }

    private HttpRequest buildMultipartRequest(String bodyJson, List<File> files)
            throws IOException {
        String boundary = "----Boundary" + System.currentTimeMillis();
        byte[] multipartBody = buildMultipartBody(boundary, bodyJson, files);

        return HttpRequest.newBuilder()
                .uri(URI.create(spocApiUrl))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", APPLICATION_JSON)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Authorization", basicAuth(spocUsername, spocPassword))
                .POST(BodyPublishers.ofByteArray(multipartBody))
                .build();
    }

    private byte[] buildMultipartBody(String boundary, String bodyJson, List<File> files)
            throws IOException {
        ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();

        String dash = "--";

        out.write((dash + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        out.write(
                ("Content-Disposition: form-data; name=\"request\"" + CRLF)
                        .getBytes(StandardCharsets.UTF_8));
        out.write(
                ("Content-Type: application/json" + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));
        out.write(bodyJson.getBytes(StandardCharsets.UTF_8));
        out.write(CRLF.getBytes(StandardCharsets.UTF_8));

        for (File file : files) {
            out.write((dash + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
            out.write(
                    ("Content-Disposition: form-data; name=\"attachments\"; filename=\""
                                    + file.getName()
                                    + "\""
                                    + CRLF)
                            .getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Type: text/plain" + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));
            out.write(java.nio.file.Files.readAllBytes(file.toPath()));
            out.write(CRLF.getBytes(StandardCharsets.UTF_8));
        }

        out.write((dash + boundary + dash + CRLF).getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    private List<String> cleanList(List<String> list) {
        return list.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String basicAuth(String username, String password) {
        var token = (username + ":" + password).getBytes(StandardCharsets.UTF_8);
        return "Basic " + Base64.getEncoder().encodeToString(token);
    }

    private Map<String, Object> buildSpocPayload(
            String sender,
            String subject,
            String content,
            List<String> fileNames,
            Collection<String> to,
            Collection<String> cc) {

        Map<String, Object> root = new LinkedHashMap<>();

        Map<String, Object> messageTemplate = new LinkedHashMap<>();
        if (cc != null && !cc.isEmpty()) {
            List<Map<String, Object>> headers = new ArrayList<>();
            Map<String, Object> ccHeader = new LinkedHashMap<>();
            ccHeader.put("Name", "Cc");
            ccHeader.put("Value", String.join(",", cc));
            headers.add(ccHeader);
            messageTemplate.put("Header", headers);
        }

        messageTemplate.put("Sender", Optional.ofNullable(sender).orElse(""));
        messageTemplate.put("Subject", Optional.ofNullable(subject).orElse(""));
        messageTemplate.put("Content", Optional.ofNullable(content).orElse(""));
        messageTemplate.put("PlainTextContent", "");
        messageTemplate.put("ContentReference", "null");

        List<String> allRecipients =
                to == null
                        ? List.of()
                        : to.stream()
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(s -> !s.isBlank())
                                .distinct()
                                .toList();

        List<Map<String, Object>> recipientList =
                allRecipients.stream()
                        .map(
                                addr -> {
                                    Map<String, Object> r = new LinkedHashMap<>();
                                    r.put("Address", addr);
                                    r.put("Properties", List.of());
                                    r.put("Attachments", fileNames);
                                    return r;
                                })
                        .toList();

        Map<String, Object> recipients = new LinkedHashMap<>();
        recipients.put("Recipient", recipientList);

        root.put("MessageTemplate", messageTemplate);
        root.put("Recipients", recipients);

        return root;
    }

    public List<String> getDefaultReceivers() {
        return new ArrayList<>(defaultReceiverMail);
    }

    public List<String> getDefaultReceiverAdjMail() {
        return new ArrayList<>(defaultReceiverAdjMail);
    }
}
