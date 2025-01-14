package fr.insee.compas.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.compas.mapper.OscarBuilder;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OscarService {

    public List<Module> getModules() {

        List<Module> modules = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        HttpEntity<String> request = new HttpEntity<>(headers);
        String urlOscar = "https://api-oscar-gestion.insee.fr/modules";
        ResponseEntity<String> response =
                restTemplate.exchange(urlOscar, HttpMethod.GET, request, String.class);

        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(response.getBody());

            OscarBuilder builder = new OscarBuilder();
            for (JsonNode noeud : root) {
                modules.add(builder.buildModule(noeud));
            }
        } catch (JsonProcessingException e) {
            log.error("Erreur lors du traitement JSON : {}", e.getMessage());
        }

        return modules;
    }

    public List<Application> getApplications() {

        List<Application> applications = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        HttpEntity<String> request = new HttpEntity<>(headers);
        String urlOscar = "https://api-oscar-gestion.insee.fr/applications";
        ResponseEntity<String> response =
                restTemplate.exchange(urlOscar, HttpMethod.GET, request, String.class);
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(response.getBody());
            OscarBuilder builder = new OscarBuilder();
            for (JsonNode noeud : root) {

                applications.add(builder.buildApplication(noeud));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return applications;
    }

    private static JsonNode getPathApplication(JsonNode noeud) {
        return noeud.path("applicationTechnique").path("application");
    }
}
