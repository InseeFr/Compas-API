package fr.insee.compas.client.configuration.oauth;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApiRhAuthentification {

    @Value("${compas.service.keycloak.client}")
    private String clientId;

    @Value("${compas.service.keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${compas.web.springdoc.token-url}")
    private String urlKeycloak;

    // --- Config API RH (endpoint agents) ---
    @Value("${apirh.base-url}")
    private String apiRhAgentsUrl;

    private LocalDateTime dateToRefreshToken;
    private String token;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Récupère la map idep -> email en interrogeant l'API RH /agents avec : Profil=Trombi,
     * populations.etat=actuel, nombre=10000, populations.formateur=false
     */
    public Map<String, String> recupererIdepEtEmails() {
        Map<String, String> idepEmailMap = new HashMap<>();
        String bearer = obtenirToken();

        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            String url = buildAgentsUrlWithParams(apiRhAgentsUrl);

            log.info("Appel API RH : {}", url);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Authorization", "Bearer " + bearer)
                            .header("Accept", "application/json")
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Code retour API RH : {}", response.statusCode());

            if (response.statusCode() == 200) {
                parseAgents(response.body(), idepEmailMap);
                log.info("Récupération terminée : {} agents trouvés", idepEmailMap.size());
            } else {
                log.warn("Échec de la requête API RH /agents (HTTP {})", response.statusCode());
            }
        } catch (IOException e) {
            log.error("Erreur IO lors de l'appel à l'API RH : ", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interruption : ", e);
        }

        return idepEmailMap;
    }

    private String obtenirToken() {
        if (!tokenIsValid()) {
            log.info("Début de la récupération du jeton Keycloak");
            log.info("Requête sur : {}", urlKeycloak);
            log.info("Avec le compte : {}", clientId);

            try (HttpClient httpClient = HttpClient.newHttpClient()) {
                String form =
                        "client_id="
                                + urlEncode(clientId)
                                + "&client_secret="
                                + urlEncode(clientSecret)
                                + "&grant_type=client_credentials"
                                + "&scope=role-as-group";

                HttpRequest httpRequest =
                        HttpRequest.newBuilder()
                                .POST(BodyPublishers.ofString(form))
                                .uri(URI.create(urlKeycloak))
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .build();

                HttpResponse<String> response =
                        httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                log.debug("Code retour Keycloak : {}", response.statusCode());

                if (response.statusCode() == 200) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jsonMap =
                            objectMapper.readValue(response.body(), Map.class);
                    token = String.valueOf(jsonMap.get("access_token"));
                    Object expiresIn = jsonMap.get("expires_in");
                    updateDateToRefreshToken(Integer.parseInt(String.valueOf(expiresIn)));
                } else {
                    log.warn("Impossible d'obtenir le jeton (HTTP {})", response.statusCode());
                }
            } catch (IOException e) {
                log.error("Erreur IO : ", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interruption : ", e);
            }
        }
        return token;
    }

    /** Retourne l'email du responsable du SNDI donné (ou empty si introuvable). */
    public Optional<String> findResponsableEmailByUnite(String uniteCode) {
        final String url = buildUrl(uniteCode);
        log.info("Recherche email responsable HIE {} via API RH : {}", uniteCode, url);

        final HttpResponse<String> response = sendRequest(url);
        if (response == null) return Optional.empty();

        if (response.statusCode() != 200) {
            log.warn("API RH responsable HIE {} -> HTTP {}", uniteCode, response.statusCode());
            return Optional.empty();
        }

        return parseEmailFromBody(response.body());
    }

    /* -------------------------- helpers -------------------------- */

    private String buildUrl(String uniteCode) {
        final String encodedUnite = URLEncoder.encode(uniteCode, StandardCharsets.UTF_8);
        return apiRhAgentsUrl
                + "?Profil=Trombi"
                + "&populations.etat=actuel"
                + "&affectation.estResponsable=true"
                + "&affectation.unite="
                + encodedUnite;
    }

    private HttpResponse<String> sendRequest(String url) {
        final String bearer = obtenirToken();
        final HttpClient httpClient = HttpClient.newHttpClient();
        final HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Bearer " + bearer)
                        .header("Accept", "application/json")
                        .GET()
                        .build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // ✅ requis par Sonar
            log.error("Thread interrompu lors de l'appel à l'API RH {}", url, e);
            return null;
        } catch (IOException e) {
            log.error("Erreur I/O lors de l'appel à l'API RH {}", url, e);
            return null;
        } catch (RuntimeException e) {
            log.error("Erreur inattendue lors de l'appel à l'API RH {}", url, e);
            return null;
        }
    }

    private Optional<String> parseEmailFromBody(String body) {
        try {
            final JsonNode root = objectMapper.readTree(body);
            final JsonNode list = resolveContentArray(root);
            if (list == null || list.isEmpty()) return Optional.empty();

            final JsonNode agent = list.get(0); // premier responsable
            return resolveEmailFromAgent(agent);
        } catch (Exception e) {
            log.error("Erreur lors du parsing JSON de la réponse API RH", e);
            return Optional.empty();
        }
    }

    /**
     * Retourne le tableau d’agents à partir de la réponse : 'content' si présent, sinon la racine
     * si déjà un tableau.
     */
    private JsonNode resolveContentArray(JsonNode root) {
        JsonNode list = root.path("content");
        if (list.isArray()) return list;
        return root.isArray() ? root : null;
    }

    /** Tente d’abord l’email direct, sinon passe par IDEP -> map idep->email. */
    private Optional<String> resolveEmailFromAgent(JsonNode agent) {
        String email = textAt(agent, "coordonnees.email");
        if (email != null && !email.isBlank()) {
            return Optional.of(email);
        }

        String idep = textAt(agent, "identifiantsAgent.idep");
        if (idep == null || idep.isBlank()) return Optional.empty();

        Map<String, String> map = recupererIdepEtEmails();
        String fallback = map.get(idep);
        return (fallback != null && !fallback.isBlank()) ? Optional.of(fallback) : Optional.empty();
    }

    private void updateDateToRefreshToken(Integer duration) {
        dateToRefreshToken = LocalDateTime.now().plusSeconds(duration).minusSeconds(10);
    }

    private boolean tokenIsValid() {
        return dateToRefreshToken != null
                && dateToRefreshToken.isAfter(LocalDateTime.now())
                && token != null;
    }

    // --- Helpers JSON & URL ---

    private void parseAgents(String json, Map<String, String> map) throws IOException {
        JsonNode root = objectMapper.readTree(json);

        JsonNode list = root.path("content");
        if (list != null && list.isArray()) {
            int i = 0;
            for (JsonNode agent : list) {
                if (i == 0) {
                    log.debug("Premier agent (aperçu) : {}", agent.toString());
                }
                extraireIdepEmail(agent, map);
                i++;
            }
            return;
        }

        if (root.isArray()) {
            for (JsonNode agent : root) extraireIdepEmail(agent, map);
        } else if (root.isObject()) {
            extraireIdepEmail(root, map);
        } else {
            log.warn("Format inattendu : impossible d'identifier la liste d'agents.");
        }
    }

    private void extraireIdepEmail(JsonNode agentNode, Map<String, String> map) {
        try {
            String idep = textAt(agentNode, "identifiantsAgent.idep");
            String email = textAt(agentNode, "coordonnees.email"); // email pro dans votre exemple

            if (isNotBlank(idep) && isNotBlank(email)) {
                map.put(idep, email);
                log.debug("Agent: {} -> {}", idep, email);
            } else {
                log.trace(
                        "Agent ignoré (idep/email manquants) : idep='{}', email='{}'", idep, email);
            }
        } catch (Exception e) {
            log.warn("Impossible de lire un agent : {}", e.getMessage());
        }
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    /** Accède à un chemin du type "a.b.c" et retourne la valeur texte si présente */
    private String textAt(JsonNode node, String dottedPath) {
        JsonNode cur = node;
        for (String p : dottedPath.split("\\.")) {
            if (cur == null) return null;
            cur = cur.path(p);
            if (cur.isMissingNode() || cur.isNull()) return null;
        }
        return cur.isTextual() ? cur.asText() : null;
    }

    /** Construit l'URL /agents avec les paramètres souhaités */
    private static String buildAgentsUrlWithParams(String baseUrl) {
        StringBuilder sb = new StringBuilder(baseUrl);
        if (!baseUrl.contains("?")) sb.append('?');
        else sb.append('&');
        sb.append("Profil=").append(urlEncode("Trombi"));
        sb.append("&populations.etat=").append(urlEncode("actuel"));
        sb.append("&nombre=").append(10000);
        sb.append("&populations.formateur=").append(false);
        return sb.toString();
    }
}
