package fr.insee.compas.service.spoc;

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
import java.util.Base64;
import java.util.stream.Collectors;

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

    /**
     * Envoie un mail en complétant/écrasant les destinataires avec ceux par défaut selon la conf.
     *
     * @param mail objet mail (doit contenir subject, message, to/cc éventuellement)
     */
    public void sendMail(Mail mail) {
        if (mail == null) {
            log.warn("Mail null : envoi annulé");
            return;
        }

        // Récupération TO / CC depuis l'objet Mail
        List<String> to = new ArrayList<>(Optional.ofNullable(mail.getTo()).orElse(List.of()));
        List<String> cc = new ArrayList<>(Optional.ofNullable(mail.getCc()).orElse(List.of()));

        if (addBalfOscar) {
            cc.addAll(defaultReceiverMail);
            cc.addAll(defaultReceiverAdjMail);
        } else if (to.isEmpty()) {
            to.addAll(defaultReceiverMail);
            cc.addAll(defaultReceiverAdjMail);
        }

        // Nettoyage basique TO/CC
        to =
                to.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .distinct()
                        .collect(Collectors.toCollection(ArrayList::new));

        cc =
                cc.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .distinct()
                        .collect(Collectors.toCollection(ArrayList::new));

        // Vérification qu'il reste au moins un destinataire (TO ou CC)
        List<String> allRecipientsForCheck =
                java.util.stream.Stream.concat(to.stream(), cc.stream())
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .distinct()
                        .toList();

        if (allRecipientsForCheck.isEmpty()) {
            log.warn("Aucun destinataire après application des règles -> envoi annulé");
            return;
        }

        Map<String, Object> payload =
                buildSpocPayload(senderMail, mail.getObject(), mail.getMessage(), to, cc);

        String bodyJson;
        try {
            bodyJson = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Erreur de sérialisation JSON pour le corps SPOC", e);
            return;
        }

        var request =
                HttpRequest.newBuilder()
                        .uri(URI.create(spocApiUrl))
                        .timeout(Duration.ofSeconds(20))
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .header("Authorization", basicAuth(spocUsername, spocPassword))
                        .POST(BodyPublishers.ofString(bodyJson, StandardCharsets.UTF_8))
                        .build();

        try {
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

    /**
     * Variante pratique : envoie directement à une liste de destinataires en TO (les défauts seront
     * ajoutés selon la conf).
     */
    public void sendMailTo(List<String> receivers, String subject, String content) {
        Mail mail = new Mail();
        mail.setObject(subject);
        mail.setMessage(content);
        mail.setTo(receivers == null ? List.of() : receivers);
        mail.setCc(List.of()); // pas de CC explicite dans cette variante
        sendMail(mail);
    }

    private String basicAuth(String username, String password) {
        var token = (username + ":" + password).getBytes(StandardCharsets.UTF_8);
        return "Basic " + Base64.getEncoder().encodeToString(token);
    }

    private Map<String, Object> buildSpocPayload(
            String sender,
            String subject,
            String content,
            Collection<String> to,
            Collection<String> cc) {

        Map<String, Object> root = new LinkedHashMap<>();

        // ---------- MessageTemplate ----------
        Map<String, Object> messageTemplate = new LinkedHashMap<>();
        messageTemplate.put("Sender", Optional.ofNullable(sender).orElse(""));
        messageTemplate.put("Subject", Optional.ofNullable(subject).orElse(""));
        messageTemplate.put("Content", Optional.ofNullable(content).orElse(""));
        messageTemplate.put("PlainTextContent", "");
        messageTemplate.put("ContentReference", "null");

        // Headers (pour le Cc)
        List<Map<String, Object>> headers = new ArrayList<>();
        if (cc != null && !cc.isEmpty()) {
            Map<String, Object> ccHeader = new LinkedHashMap<>();
            ccHeader.put("Name", "Cc");
            ccHeader.put("Value", String.join(";", cc)); // séparateur à adapter si besoin
            headers.add(ccHeader);
        }
        if (!headers.isEmpty()) {
            messageTemplate.put("Header", headers);
        }

        // ---------- Recipients ----------
        List<String> allRecipients =
                (to == null
                        ? List.<String>of()
                        : to.stream()
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(s -> !s.isBlank())
                                .distinct()
                                .toList());

        List<Map<String, Object>> recipientList =
                allRecipients.stream()
                        .map(
                                addr -> {
                                    Map<String, Object> r = new LinkedHashMap<>();
                                    r.put("Address", addr);
                                    r.put("Properties", List.of());
                                    r.put("Attachments", List.of());
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
