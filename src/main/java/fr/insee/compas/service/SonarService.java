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

    @Value("${fr.insee.compas.sonar.token:}")
    private String tokenGitlab;

    @Value("${fr.insee.compas.github.sonar.token:}")
    private String tokenGithubSonar;

    @Value("${fr.insee.compas.proxy.name:}")
    private String proxyName;

    @Value("${fr.insee.compas.proxy.port:}")
    private int proxyPort;

    private String urlGitlab =
            "http://sonar.insee.fr/api/measures/component?component=%s&metricKeys=%s";
    private String urlGithub =
            "https://sonarcloud.io/api/measures/component?component=%s&metricKeys=%s";

    private final ObjectMapper objectMapper;

    public SonarService() {

        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public RecuperationMeasures getDataFromSonarAPIMeasures(String projetSonar, String source)
            throws IOException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyName, proxyPort));
        OkHttpClient clientExterne =
                new OkHttpClient.Builder()
                        .proxy(proxy)
                        .connectTimeout(30, TimeUnit.SECONDS) // Timeout pour la connexion
                        .readTimeout(30, TimeUnit.SECONDS) // Timeout pour la lecture des données
                        .writeTimeout(30, TimeUnit.SECONDS) // Timeout pour l'écriture des données
                        .build();
        OkHttpClient clientInterne = new OkHttpClient.Builder().build();

        String url;
        String token;
        Request request;

        String metrics =
                Arrays.stream(IndicateurSonar.values())
                        .map(IndicateurSonar::getKey)
                        .collect(Collectors.joining(","));

        if ("gitlab".equals(source)) {
            url = String.format(urlGitlab, projetSonar, metrics);
            token = tokenGitlab;
            request =
                    new Request.Builder()
                            .url(url)
                            .addHeader("Authorization", "Bearer " + token)
                            .build();
            try (Response response = clientInterne.newCall(request).execute()) {
                if (response.body() == null || !response.isSuccessful()) {
                    return null;
                }
                String jsonString = response.body().string();
                return objectMapper.readValue(jsonString, RecuperationMeasures.class);
            }

        } else {
            url = String.format(urlGithub, projetSonar, metrics);
            token = tokenGithubSonar;
            request =
                    new Request.Builder()
                            .url(url)
                            .addHeader("Authorization", "Bearer " + token)
                            .build();
            try (Response response = clientExterne.newCall(request).execute()) {
                if (response.body() == null || !response.isSuccessful()) {
                    return null;
                }
                String jsonString = response.body().string();
                return objectMapper.readValue(jsonString, RecuperationMeasures.class);
            }
        }
    }
}
