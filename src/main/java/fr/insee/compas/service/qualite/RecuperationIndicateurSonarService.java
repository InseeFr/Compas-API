package fr.insee.compas.service.qualite;

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
    private static final String SANS_OBJET = "Sans objet";

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

    public Map<String, RecuperationMeasures> putIndicateursSonarModule() {
        List<Module> modules = oscarService.getModules();
        Map<String, RecuperationMeasures> result = new HashMap<>();
        LocalDate now = LocalDate.now();

        int withProjectKey = 0;
        int withoutValidAnalysis = 0;

        for (Module module : modules) {

            if (isSansObjet(module)) {
                saveSansObjetIndicator(module, now);
                continue;
            }

            if (hasValidSonarKey(module)) {
                withProjectKey++;

                boolean hasAnalysis = processSonarAnalysis(module, now, result);

                if (!hasAnalysis) {
                    log.warn(
                            "Le projet {} n'a pas une analyse Sonar correcte", module.getModName());
                    withoutValidAnalysis++;
                }
            }
        }
        log.info("Nombre de modules Oscar avec un project Key Sonar : {}", withProjectKey);
        log.info(
                "Nombre de modules Oscar avec un project Key Sonar mais pas d'analyse valide : {}",
                withoutValidAnalysis);

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
            if (!keysSonar.isEmpty() && keysSonar.stream().allMatch(SANS_OBJET::equals)) {
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

    private boolean hasValidSonarKey(Module module) {
        return module.getKeySonar() != null
                && !"null".equals(module.getKeySonar())
                && !SANS_OBJET.equals(module.getKeySonar());
    }

    private boolean isSansObjet(Module module) {
        return SANS_OBJET.equals(module.getKeySonar());
    }

    private boolean processSonarAnalysis(
            Module module, LocalDate now, Map<String, RecuperationMeasures> result) {

        RecuperationMeasures measures =
                fetchMeasures(module.getKeySonar(), "gitlab", module.getModName());

        if (isValidMeasures(measures)) {
            result.put(module.getKeySonar(), measures);
            return putIndicateurSonarInBdd(module, null, measures, now);
        }

        measures = fetchMeasures(module.getKeySonar(), "github", module.getModName());

        if (isValidMeasures(measures)) {
            result.put(module.getKeySonar(), measures);
            return putIndicateurSonarInBdd(module, null, measures, now);
        }

        return false;
    }

    private RecuperationMeasures fetchMeasures(
            String keySonar, String provider, String projectName) {
        return sonarService.getDataFromSonarAPIMeasures(keySonar, provider, projectName);
    }

    private boolean isValidMeasures(RecuperationMeasures measures) {
        return measures != null
                && measures.getComponent() != null
                && !measures.getComponent().getMeasures().isEmpty();
    }

    private void saveSansObjetIndicator(Module module, LocalDate now) {
        log.info(
                "le module {} est SO pour sonar, on met donc -1 pour l'indicateur ligne de code",
                module.getModName());

        tableFaitsRepository.save(
                TableFaits.builder()
                        .idModule(module.getId())
                        .idApplication(module.getIdApplication())
                        .idIndicateur(IndicateurType.NBR_LIGNE.getValue())
                        .date(now)
                        .valeur(BigDecimal.valueOf(-1))
                        .idSource(0)
                        .build());
    }
}
