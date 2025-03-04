package fr.insee.compas.service.qualite;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.GitlabService;
import fr.insee.compas.service.OscarService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RecupCveService {

    public RecupCveService(
            TableFaitsRepository tableFaitsRepository,
            OscarService oscarService,
            UtilsCveService utilCveService,
            GitlabService gitlabService) {
        this.tableFaitsRepository = tableFaitsRepository;
        this.oscarService = oscarService;
        this.utilService = utilCveService;
        this.gitlabService = gitlabService;
    }

    private final TableFaitsRepository tableFaitsRepository;
    private final OscarService oscarService;
    private final UtilsCveService utilService;
    private final GitlabService gitlabService;

    public void recupereCve() {

        List<Module> modules = oscarService.getModules();
        List<Application> applications = oscarService.getApplications();

        Map<Integer, Map<String, Set<String>>> inventaireByModule =
                recupereInventaireCveAllModule();
        Map<Integer, Map<String, Set<String>>> inventaireByApplication = new HashMap<>();
        // On met à jour les cve pour les modules et on calcule au niveau application
        for (Module module : modules) {
            Map<String, Set<String>> inventaireModule = inventaireByModule.get(module.getId());
            if (inventaireModule != null && !inventaireModule.isEmpty()) {

                putCveModuleInBdd(module, inventaireByModule.get(module.getId()));

                // Récupérer ou initialiser l'inventaire au niveau application
                if (inventaireByApplication.get(module.getIdApplication()) == null) {
                    inventaireByApplication.put(module.getIdApplication(), inventaireModule);
                } else {
                    // Fusionner les données module -> application
                    Map<String, Set<String>> inventaireApplication =
                            inventaireByApplication.get(module.getIdApplication());
                    Map<String, Set<String>> inventaireConcatene =
                            utilService.concatInventaireCve(
                                    inventaireApplication, inventaireModule);
                    inventaireByApplication.put(module.getIdApplication(), inventaireConcatene);
                }
            }
        }

        // On met à jour les cve aux niveaux application
        for (Application application : applications) {
            if (inventaireByApplication.get(application.getIdApplication()) != null) {
                putCveApplicationInBdd(
                        application, inventaireByApplication.get(application.getIdApplication()));
            }
        }
    }

    public Map<Integer, Map<String, Set<String>>> recupereInventaireCveAllModule() {

        List<Module> modules = oscarService.getModules();
        Map<Integer, Map<String, Set<String>>> inventaires = new HashMap<>();
        try {
            for (Module module : modules) {
                String fileJson = gitlabService.getJson(module);
                if (!fileJson.isEmpty()) {
                    // Extraction des Cve du fichier json
                    Map<String, Set<String>> inventaireModule = getCveFromJson(fileJson);
                    if (inventaireModule != null && !inventaireModule.isEmpty()) {
                        inventaires.put(module.getId(), inventaireModule);
                    }
                } else {
                    log.error(
                            "Echec de la récupération du fichier pour le module  {}",
                            module.getModName());
                }
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt(); // Réinterrompt le thread
            }
            log.error("An error occurred: {}", e.getMessage());
        }

        return inventaires;
    }

    private void putCveModuleInBdd(Module module, Map<String, Set<String>> cveData) {
        for (Map.Entry<String, Set<String>> entry : cveData.entrySet()) {
            TableFaits fait =
                    TableFaits.builder()
                            .idModule(module.getId())
                            .idApplication(module.getIdApplication())
                            .idIndicateur(utilService.getIndicateurModule(entry.getKey()))
                            .valeur(BigDecimal.valueOf(entry.getValue().size()))
                            .idSource(SourceType.GITLAB.getValue())
                            .commentaire("")
                            .date(LocalDate.now())
                            .build();
            tableFaitsRepository.save(fait);
        }
    }

    private void putCveApplicationInBdd(Application application, Map<String, Set<String>> cveData) {
        for (Map.Entry<String, Set<String>> entry : cveData.entrySet()) {
            TableFaits fait =
                    TableFaits.builder()
                            .idModule(null)
                            .idApplication(application.getIdApplication())
                            .idIndicateur(utilService.getIndicateurApplication(entry.getKey()))
                            .valeur(BigDecimal.valueOf(entry.getValue().size()))
                            .idSource(SourceType.GITLAB.getValue())
                            .commentaire("")
                            .date(LocalDate.now())
                            .build();
            tableFaitsRepository.save(fait);
        }
    }

    public Map<String, Set<String>> getCveFromJson(String jsonFile) {
        try {
            JsonNode rootNode = new ObjectMapper().readTree(jsonFile);
            JsonNode resultsNode = rootNode.get("Results");

            if (resultsNode == null || !resultsNode.isArray()) {
                return new HashMap<>();
            }

            return utilService.parseResults(resultsNode);
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier JSON : {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
