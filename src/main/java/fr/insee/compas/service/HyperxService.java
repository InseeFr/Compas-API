package fr.insee.compas.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.compas.model.hyperx.IndicateurRecuperationSecuriteVM;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HyperxService {

    private final RestTemplate restTemplate;
    private final String urlHyperx;
    private final String keyHyperx;

    public HyperxService(
            RestTemplateBuilder builder,
            @Value("${fr.insee.compas.url.hyperx}") String urlHyperx,
            @Value("${fr.insee.compas.key.hyperx}") String keyHyperx) {
        this.restTemplate = builder.build();
        this.urlHyperx = urlHyperx;
        this.keyHyperx = keyHyperx;
    }

    public IndicateurRecuperationSecuriteVM maxMajVm(String application) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", keyHyperx);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String apiUrl =
                urlHyperx + "/vms?application=" + application + "&list_colonnes=delai_majlin";
        ResponseEntity<String> response =
                restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Erreur API: " + response.getStatusCode());
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        int max = 0;
        int nb = 0;
        try {
            root = mapper.readTree(response.getBody());
            for (JsonNode node : root) {
                int value = 0;
                if (node.get("delai_majlin") != null) {
                    value = node.get("delai_majlin").asInt();
                }

                if (value > max) {
                    max = value;
                }
                if (value > 30) {
                    nb++;
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new IndicateurRecuperationSecuriteVM(max, nb);
    }
}
