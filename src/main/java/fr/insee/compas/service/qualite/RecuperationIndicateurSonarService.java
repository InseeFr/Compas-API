package fr.insee.compas.service.qualite;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.IndicateurSonar;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.sonar.Measure;
import fr.insee.compas.model.sonar.RecuperationMeasures;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.SonarService;

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

    public void putIndicateursSonar() throws IOException {
        List<Module> modules = oscarService.getModules();
        LocalDate now = LocalDate.now();
        log.debug("la date est {}", now);
        int compteurModuleOscarWithProjectKey = 0;
        int compteurModuleOscarWithProjectKeySansAnalyse = 0;
        for (Module module : modules) {
            if (!"null".equals(module.getKeySonar())
                    && !module.getKeySonar().equals("Sans objet")) {
                compteurModuleOscarWithProjectKey++;
                RecuperationMeasures measures =
                        sonarService.getDataFromSonarAPIMeasures(module.getKeySonar());
                if (measures != null
                        && measures.getComponent() != null
                        && !measures.getComponent().getMeasures().isEmpty()) {
                    boolean moduleSansAnalyse = putIndicateurSonarInBdd(module, measures, now);
                    if (!moduleSansAnalyse) {
                        compteurModuleOscarWithProjectKeySansAnalyse++;
                    }
                } else {
                    log.warn("Le module {} n'a pas une analyse Sonar correcte", module.getId());
                    compteurModuleOscarWithProjectKeySansAnalyse++;
                }
            } else if (!"null".equals(module.getKeySonar())) {
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
    }

    public boolean putIndicateurSonarInBdd(
            Module module, RecuperationMeasures measures, LocalDate date) {
        boolean estVide = true;
        for (IndicateurSonar metric : IndicateurSonar.values()) {
            Optional<Measure> measureOpt =
                    measures.getComponent().getMeasures().stream()
                            .filter(m -> m.getMetric().equals(metric.getKey()))
                            .findFirst();

            if (measureOpt.isPresent()) {
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

                if (metric == IndicateurSonar.LINES_TO_COVER) {
                    estVide = false;
                }
            }
        }
        return !estVide;
    }
}
