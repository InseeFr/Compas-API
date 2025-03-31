package fr.insee.compas.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.compas.builder.OscarBuilder;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OscarService {

    @Value("${spring.cloud.openfeign.client.config.oscar-service.url}")
    private String urlOscar;

    private final RestTemplate restTemplate;
    private final OscarBuilder oscarBuilder;
    private final ObjectMapper objectMapper;

    public OscarService(
            RestTemplate restTemplate, OscarBuilder oscarBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.oscarBuilder = oscarBuilder;
        this.objectMapper = objectMapper;
    }

    public List<Module> getModules() {
        List<Module> modules = new ArrayList<>();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String urlModules = urlOscar + "modules";

        ResponseEntity<String> response =
                restTemplate.exchange(urlModules, HttpMethod.GET, request, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());

            for (JsonNode noeud : root) {
                modules.add(oscarBuilder.buildModule(noeud));
            }
        } catch (JsonProcessingException e) {
            log.error("Erreur lors du traitement JSON : {}", e.getMessage());
        }

        return modules;
    }

    public List<Application> getApplications() {
        List<Application> applications = new ArrayList<>();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String urlApplication = urlOscar + "applications";

        ResponseEntity<String> response =
                restTemplate.exchange(urlApplication, HttpMethod.GET, request, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            for (JsonNode noeud : root) {
                applications.add(oscarBuilder.buildApplication(noeud));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return applications;
    }
}
