package fr.insee.compas.service.qualite;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.IndicateurSonar;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.sonar.Component;
import fr.insee.compas.model.sonar.Measure;
import fr.insee.compas.model.sonar.RecuperationMeasures;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.SonarService;
import fr.insee.compas.service.UtilsService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RecuperationIndicateurSonarService {

    private final OscarService oscarService;
    private final TableFaitsRepository tableFaitsRepository;
    private final SonarService sonarService;

    public RecuperationIndicateurSonarService(
            OscarService oscarService,
            TableFaitsRepository tableFaitsRepository,
            SonarService sonarService) {
        this.oscarService = oscarService;
        this.tableFaitsRepository = tableFaitsRepository;
        this.sonarService = sonarService;
    }

    public Map<String, RecuperationMeasures> putIndicateursSonarModule() throws IOException {
        List<Module> modules = oscarService.getModules();
        Map<String, RecuperationMeasures> result = new HashMap<>();
        LocalDate now = LocalDate.now();
        log.debug("la date est {}", now);
        int compteurModuleOscarWithProjectKey = 0;
        int compteurModuleOscarWithProjectKeySansAnalyse = 0;
        for (Module module : modules) {
            boolean analyseSonarNonNulEtNonSansObjet =
                    !"null".equals(module.getKeySonar())
                            && !module.getKeySonar().equals("Sans objet");
            // cas ou une key sonar est renseigné
            if (analyseSonarNonNulEtNonSansObjet) {
                compteurModuleOscarWithProjectKey++;
                boolean moduleSansAnalyse = false;
                // on lance la recherche sur le sonar interne
                RecuperationMeasures measures =
                        sonarService.getDataFromSonarAPIMeasures(module.getKeySonar(), "gitlab");

                if (measures != null
                        && measures.getComponent() != null
                        && !measures.getComponent().getMeasures().isEmpty()) {
                    result.put(module.getKeySonar(), measures);
                    // on a un retour
                    moduleSansAnalyse = putIndicateurSonarInBdd(module, null, measures, now);
                }
                if (!moduleSansAnalyse) {
                    // on fait l'appel à l'api de sonarcloud
                    measures =
                            sonarService.getDataFromSonarAPIMeasures(
                                    module.getKeySonar(), "github");
                    if (measures != null
                            && measures.getComponent() != null
                            && !measures.getComponent().getMeasures().isEmpty()) {
                        // on a un retour
                        result.put(module.getKeySonar(), measures);
                        moduleSansAnalyse = putIndicateurSonarInBdd(module, null, measures, now);
                    }
                }

                if (!moduleSansAnalyse) {
                    log.warn("Le module {} n'a pas une analyse Sonar correcte", module.getId());
                    compteurModuleOscarWithProjectKeySansAnalyse++;
                }
            }
            if (module.getKeySonar().equals("Sans objet")) {
                log.info(
                        "le module est SO pour sonar, on met donc -1 pour l'indicateur ligne de"
                                + " code");
                tableFaitsRepository.save(
                        TableFaits.builder()
                                .idModule(module.getId())
                                .idApplication(module.getIdApplication())
                                .idIndicateur(IndicateurType.NBR_LIGNE.getValue())
                                .date(now)
                                .valeur(new BigDecimal(-1))
                                .idSource(0)
                                .build());
            }
        }

        log.info(
                "Nombre de modules Oscar avec un project Key Sonar : {}",
                compteurModuleOscarWithProjectKey);
        log.info(
                "Nombre de modules Oscar avec un project Key Sonar mais pas d'analyse valide : {}",
                compteurModuleOscarWithProjectKeySansAnalyse);

        return result;
    }

    public boolean putIndicateurSonarInBdd(
            Module module, Application application, RecuperationMeasures measures, LocalDate date) {
        boolean estVide = true;
        for (IndicateurSonar metric : IndicateurSonar.values()) {
            Optional<Measure> measureOpt =
                    measures.getComponent().getMeasures().stream()
                            .filter(m -> m.getMetric().equals(metric.getKey()))
                            .findFirst();

            if (measureOpt.isPresent() && application == null) {
                Measure measure = measureOpt.get();
                tableFaitsRepository.save(
                        TableFaits.builder()
                                .idModule(module.getId())
                                .idApplication(module.getIdApplication())
                                .idIndicateur(metric.getIndicateurType().getValue())
                                .date(date)
                                .valeur(new BigDecimal(measure.getValue()))
                                .idSource(0)
                                .build());

                estVide = false;
            } else if (measureOpt.isPresent() && module == null) {
                Measure measure = measureOpt.get();
                tableFaitsRepository.save(
                        TableFaits.builder()
                                .idModule(null)
                                .idApplication(application.getIdApplication())
                                .idIndicateur(metric.getIndicateurType().getValue())
                                .date(date)
                                .valeur(new BigDecimal(measure.getValue()))
                                .idSource(0)
                                .build());

                estVide = false;
            }
        }
        return !estVide;
    }

    public void putIndicateursSonarApplication(Map<String, RecuperationMeasures> analyseModule) {
        Map<Application, Set<String>> applications = oscarService.mapApplicationsToKeySonars();

        for (Map.Entry<Application, Set<String>> entry : applications.entrySet()) {
            Set<String> keysSonar = entry.getValue();
            RecuperationMeasures sommeMesures = new RecuperationMeasures();
            if (!keysSonar.isEmpty() && keysSonar.stream().allMatch(s -> s.equals("Sans objet"))) {
                Component component = new Component();
                Measure ligneNegative = new Measure("lines_to_cover", "-1");
                component.setMeasures(new ArrayList<>(List.of(ligneNegative)));
                sommeMesures.setComponent(component);
            }
            for (String keySonar : entry.getValue()) {
                sommeMesures =
                        UtilsService.concatenationMeasures(
                                sommeMesures, analyseModule.get(keySonar));
            }
            if (sommeMesures != null
                    && sommeMesures.getComponent() != null
                    && sommeMesures.getComponent().getMeasures() != null) {
                putIndicateurSonarInBdd(null, entry.getKey(), sommeMesures, LocalDate.now());
            }
        }
    }
}
