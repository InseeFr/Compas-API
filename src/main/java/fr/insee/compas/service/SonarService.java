package fr.insee.compas.service;

import java.io.IOException;
import java.util.Arrays;
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
    private String token;

    private OkHttpClient client;
    private ObjectMapper objectMapper;

    public SonarService() {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public RecuperationMeasures getDataFromSonarAPIMeasures(String projetSonar) throws IOException {

        String metrics =
                Arrays.stream(IndicateurSonar.values())
                        .map(IndicateurSonar::getKey)
                        .collect(Collectors.joining(","));

        String url =
                String.format(
                        "http://sonar.insee.fr/api/measures/component?component=%s&metricKeys=%s",
                        projetSonar, metrics);

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
}
