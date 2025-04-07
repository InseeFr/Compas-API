package fr.insee.compas.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.compas.builder.OscarBuilder;
import fr.insee.compas.client.OscarClient;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;
import fr.insee.compas.repository.ModuleOscarRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OscarService {

    @Value("${spring.cloud.openfeign.client.config.oscar-service.url}")
    private String urlOscar;

    private final OscarClient oscarClient;
    private final RestTemplate restTemplate;
    private final OscarBuilder oscarBuilder;
    private final ObjectMapper objectMapper;
    private final ModuleOscarRepository moduleOscarRepo;

    public OscarService(
            ModuleOscarRepository moduleOscarRepo,
            OscarClient oscarClient,
            RestTemplate restTemplate,
            OscarBuilder oscarBuilder,
            ObjectMapper objectMapper) {
        this.oscarClient = oscarClient;
        this.restTemplate = restTemplate;
        this.oscarBuilder = oscarBuilder;
        this.objectMapper = objectMapper;
        this.moduleOscarRepo = moduleOscarRepo;
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

    public Map<String, List<ModuleHistorique>> getModulesHistorique() {
        Map<String, List<ModuleHistorique>> modulesHistoriqueMap = new HashMap<>();

        ResponseEntity<String> response = oscarClient.getModuleHistoriqueOscar();
        try {
            JsonNode root = objectMapper.readTree(response.getBody());

            for (JsonNode noeud : root) {
                ModuleHistorique moduleHistorique = oscarBuilder.buildModuleHistorique(noeud);
                String moduleId = String.valueOf(moduleHistorique.getIdModule());

                modulesHistoriqueMap
                        .computeIfAbsent(moduleId, k -> new ArrayList<>())
                        .add(moduleHistorique);
            }
        } catch (JsonProcessingException e) {
            log.error("Erreur lors du traitement JSON : {}", e.getMessage());
        }

        return modulesHistoriqueMap;
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

    @Transactional
    public void miseAjourModuleOscarEnBaseDeDonnees() {
        List<Module> modules = getModules();
        moduleOscarRepo.desactivateAllModules();
        for (Module module : modules) {
            log.debug("Mise à jour du module :{}", module.getId());
            moduleOscarRepo.upsertProduct(module.getId());
        }
    }
}
