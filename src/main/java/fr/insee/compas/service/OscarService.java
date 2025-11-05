package fr.insee.compas.service;

import java.util.*;

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

    public List<Module> getModulesIhm() {
        List<Module> modules = getModules();
        return modules.stream()
                .filter(
                        module ->
                                module.getTypeLivrable() != null
                                        && module.getTypeLivrable().contains("IHM"))
                .toList();
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

    public Map<Application, Set<String>> mapApplicationsToKeySonars() {
        List<Module> modules = getModules();
        Map<Application, Set<String>> appToKeySonars = new HashMap<>();

        for (Module module : modules) {
            // Create a representative Application object from Module data
            Application app =
                    Application.builder()
                            .idApplication(module.getIdApplication())
                            .appName(module.getAppName())
                            .domaineSndi(module.getDomaineSndi())
                            .domaineFonctionnel(module.getDomaineFonctionnel())
                            .sndi(module.getSndi())
                            .build();

            // Initialize set if application not already in map
            appToKeySonars.computeIfAbsent(app, k -> new HashSet<>()).add(module.getKeySonar());
        }

        return appToKeySonars;
    }

    /** Récupère le RGA de l'application à partir de son id Oscar */
    public String getRgaById(Integer idApplication) {
        try {
            var response = oscarClient.getApplicationOscar(idApplication);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getRga();
            } else {
                log.warn("Impossible de récupérer le RGA (code HTTP {})", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error(
                    "Erreur lors de la récupération du RGA pour l'application {}",
                    idApplication,
                    e);
            return null;
        }
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

    public Map<Application, Set<Integer>> mapApplicationsWithModules() {
        List<Module> modules = getModules();
        Map<Application, Set<Integer>> appWithModules = new HashMap<>();

        for (Module module : modules) {
            Application app =
                    Application.builder()
                            .idApplication(module.getIdApplication())
                            .appName(module.getAppName())
                            .domaineSndi(module.getDomaineSndi())
                            .domaineFonctionnel(module.getDomaineFonctionnel())
                            .sndi(module.getSndi())
                            .build();

            appWithModules.computeIfAbsent(app, k -> new HashSet<>()).add(module.getId());
        }

        return appWithModules;
    }
}
