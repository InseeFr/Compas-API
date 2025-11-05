package fr.insee.compas.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.insee.compas.model.compas.IndicateurSonar;
import fr.insee.compas.model.sonar.*;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    public SonarService() {

        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String getNbIssueSonarAccessibility(String projetSonar, String source) {
        OkHttpClient client =
                GITLAB_INTERNE.equals(source)
                        ? new OkHttpClient.Builder().build()
                        : new OkHttpClient.Builder()
                                .proxy(
                                        new Proxy(
                                                Proxy.Type.HTTP,
                                                new InetSocketAddress(proxyName, proxyPort)))
                                .connectTimeout(30, TimeUnit.SECONDS)
                                .readTimeout(30, TimeUnit.SECONDS)
                                .writeTimeout(30, TimeUnit.SECONDS)
                                .build();

        String urlTemplate =
                GITLAB_INTERNE.equals(source)
                        ? "http://sonar.insee.fr/api/issues/search?componentKeys=%s&tags=%s"
                        : "https://sonarcloud.io/api/issues/search?componentKeys=%s&tags=%s";

        String url = String.format(urlTemplate, projetSonar, "accessibility");
        String token = GITLAB_INTERNE.equals(source) ? tokenGitlab : tokenGithub;

        Request request =
                new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + token)
                        .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null || !response.isSuccessful()) {
                return null;
            }
            String jsonString = response.body().string();
            Paging jsonMap = objectMapper.readValue(jsonString, Paging.class);
            return String.valueOf(jsonMap.getTotal());
        } catch (IOException e) {
            throw new SonarApiException(
                    "Erreur lors de l'appel à l'API Sonar pour le projet " + projetSonar, e);
        }
    }

    public RecuperationMeasures getDataFromSonarAPIMeasures(String projetSonar, String source)
            throws IOException {

        OkHttpClient client =
                GITLAB_INTERNE.equals(source)
                        ? new OkHttpClient.Builder().build()
                        : new OkHttpClient.Builder()
                                .proxy(
                                        new Proxy(
                                                Proxy.Type.HTTP,
                                                new InetSocketAddress(proxyName, proxyPort)))
                                .connectTimeout(30, TimeUnit.SECONDS)
                                .readTimeout(30, TimeUnit.SECONDS)
                                .writeTimeout(30, TimeUnit.SECONDS)
                                .build();

        String metrics =
                Arrays.stream(IndicateurSonar.values())
                        .map(IndicateurSonar::getKey)
                        .collect(Collectors.joining(","));

        String urlTemplate =
                GITLAB_INTERNE.equals(source)
                        ? "http://sonar.insee.fr/api/measures/component?component=%s&metricKeys=%s"
                        : "https://sonarcloud.io/api/measures/component?component=%s&metricKeys=%s";

        String url = String.format(urlTemplate, projetSonar, metrics);
        String token = GITLAB_INTERNE.equals(source) ? tokenGitlab : tokenGithub;

        Request request =
                new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + token)
                        .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null || !response.isSuccessful()) {
                return null;
            }
            String jsonString = response.body().string();
            return objectMapper.readValue(jsonString, RecuperationMeasures.class);
        }
    }

    private static class SonarApiException extends RuntimeException {
        public SonarApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
