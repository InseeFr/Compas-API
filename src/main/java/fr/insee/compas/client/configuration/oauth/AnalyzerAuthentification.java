package fr.insee.compas.client.configuration.oauth;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AnalyzerAuthentification {

    @Value("${compas.service.keycloak.client}")
    private String clientId;

    @Value("${compas.service.keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${compas.web.springdoc.token-url}")
    private String urlKeycloak;

    private LocalDateTime dateToRefreshToken;
    private String token;

    public String execute() {
        if (!tokenIsValid()) {
            log.info("début de la récupération du jeton keycloak");
            log.info("Requête sur  : {}", urlKeycloak);
            log.info("Avec le compte : {}", clientId);

            try (HttpClient httpClient = HttpClient.newHttpClient()) {
                HttpRequest httpRequest =
                        HttpRequest.newBuilder()
                                .POST(
                                        BodyPublishers.ofString(
                                                "client_id="
                                                        + clientId
                                                        + "&client_secret="
                                                        + clientSecret
                                                        + "&grant_type=client_credentials"
                                                        + "&scope=role-as-group"))
                                .uri(URI.create(urlKeycloak))
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .build();
                HttpResponse<String> response =
                        httpClient.send(httpRequest, BodyHandlers.ofString());

                log.debug("code retour de la réponse {}", response.statusCode());
                ObjectMapper mapper = new ObjectMapper();
                var jsonMap = mapper.readValue(response.body(), Map.class);
                token = String.valueOf(jsonMap.get("access_token"));
                updateDateToRefreshToken(
                        Integer.valueOf(String.valueOf(jsonMap.get("expires_in"))));
            } catch (IOException e) {
                log.error("Erreur : ", e);
            } catch (InterruptedException e) {
                log.warn("Interruption : ", e);
                Thread.currentThread().interrupt();
            }
        }
        return token;
    }

    private void updateDateToRefreshToken(Integer duration) {
        // soustraction de 10 secondes par sécurité avant la fin de validité du jeton
        dateToRefreshToken = LocalDateTime.now().plusSeconds(duration).minusSeconds(10);
    }

    private boolean tokenIsValid() {
        return dateToRefreshToken != null && dateToRefreshToken.isAfter(LocalDateTime.now());
    }
}
