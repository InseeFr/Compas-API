package fr.insee.compas.service;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.insee.compas.exception.SonarApiException;
import fr.insee.compas.model.compas.IndicateurSonar;
import fr.insee.compas.model.sonar.Paging;
import fr.insee.compas.model.sonar.RecuperationMeasures;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SonarService {

    public static final String GITLAB_INTERNE = "gitlab";

    @Value("${fr.insee.compas.sonar.token:}")
    private String tokenGitlab;

    @Value("${fr.insee.compas.github.sonar.token:}")
    private String tokenGithub;

    @Value("${fr.insee.compas.proxy.name:}")
    private String proxyName;

    @Value("${fr.insee.compas.proxy.port:}")
    private int proxyPort;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public SonarService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String getNbIssueSonarAccessibility(
            String projetSonar, String source, String projectName) {
        if (projetSonar == null || projetSonar.isBlank()) {
            log.debug("Projet {} vide ou null – appel NbISSUE ignoré", projectName);
            return null;
        }

        String url = String.format(this.getUrlNbIssue(source), projetSonar, "accessibility");
        log.debug("Appel Sonar NBISSUE pour le projet: {}", projectName);
        try {
            ResponseEntity<String> response =
                    this.sonarClient(source)
                            .exchange(url, HttpMethod.GET, this.getEntity(source), String.class);
            if (response.getBody() == null) {
                log.debug("Réponse vide sur NBISSUE pour le projet {}", projectName);
                return null;
            }
            Paging jsonMap = objectMapper.readValue(response.getBody(), Paging.class);
            return String.valueOf(jsonMap.getTotal());
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Aucune donnée NBISSUE sonar pour le projet {}", projectName);
            return null;

        } catch (HttpStatusCodeException e) {
            throw new SonarApiException(
                    "Erreur HTTP Sonar (" + e.getStatusCode() + ") pour le projet " + projectName,
                    e);

        } catch (RestClientException | JsonProcessingException e) {
            throw new SonarApiException(
                    "Erreur lors de l'appel à l'API Sonar pour les NBISSUE sur le projet "
                            + projectName,
                    e);
        }
    }

    public RecuperationMeasures getDataFromSonarAPIMeasures(
            String projetSonar, String source, String projectName) {
        if (projetSonar == null || projetSonar.isBlank()) {
            log.debug("Projet {} vide ou null – appel API measures ignoré", projectName);
            return null;
        }
        String metrics =
                Arrays.stream(IndicateurSonar.values())
                        .map(IndicateurSonar::getKey)
                        .collect(Collectors.joining(","));

        String url = String.format(getUrlApiMeasures(source), projetSonar, metrics);
        log.debug("Appel Sonar API measures pour le projet: {}", projectName);

        try {
            ResponseEntity<String> response =
                    sonarClient(source)
                            .exchange(url, HttpMethod.GET, getEntity(source), String.class);

            if (response.getBody() == null) {
                log.debug("Réponse vide sur api measures pour le projet {}", projectName);
                return null;
            }

            return objectMapper.readValue(response.getBody(), RecuperationMeasures.class);

        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Aucune donnée API measures pour sonar pour le projet {}", projectName);
            return null;

        } catch (HttpStatusCodeException e) {
            throw new SonarApiException(
                    "Erreur HTTP Sonar (" + e.getStatusCode() + ") pour le projet " + projectName,
                    e);

        } catch (RestClientException | JsonProcessingException e) {
            throw new SonarApiException(
                    "Erreur lors de l'appel à l'API Sonar pour les API measures sur le projet "
                            + projectName,
                    e);
        }
    }

    private String getUrlNbIssue(String source) {
        return GITLAB_INTERNE.equals(source)
                ? "https://sonar.insee.fr/api/issues/search?componentKeys=%s&tags=%s"
                : "https://sonarcloud.io/api/issues/search?componentKeys=%s&tags=%s";
    }

    private String getUrlApiMeasures(String source) {
        return GITLAB_INTERNE.equals(source)
                ? "https://sonar.insee.fr/api/measures/component?component=%s&metricKeys=%s"
                : "https://sonarcloud.io/api/measures/component?component=%s&metricKeys=%s";
    }

    private HttpEntity<Void> getEntity(String source) {
        String token = GITLAB_INTERNE.equals(source) ? tokenGitlab : tokenGithub;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return new HttpEntity<>(headers);
    }

    private RestTemplate sonarClient(String source) {
        if (GITLAB_INTERNE.equals(source)) {
            return restTemplate;
        }

        return new RestTemplateBuilder()
                .requestFactory(
                        () -> {
                            Proxy proxy =
                                    new Proxy(
                                            Proxy.Type.HTTP,
                                            new InetSocketAddress(proxyName, proxyPort));
                            SimpleClientHttpRequestFactory factory =
                                    new SimpleClientHttpRequestFactory();
                            factory.setReadTimeout(Duration.ofSeconds(30));
                            factory.setConnectTimeout(Duration.ofSeconds(30));
                            factory.setProxy(proxy);
                            return factory;
                        })
                .build();
    }
}
