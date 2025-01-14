package fr.insee.compas.service;

import static fr.insee.compas.service.SonarService.getIndicateurSonar;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.Indicateur;
import fr.insee.compas.model.compas.ModuleGrade;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.sonar.CouvertureTest;
import fr.insee.compas.repository.TableFaitsRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QualiteService {

    private final OscarService oscarService;

    private final UtilsService utilsService;

    private final TableFaitsService tableFaitsService;

    private final TableFaitsRepository tableFaitsRepository;

    private final AgregationService agregationService;

    public QualiteService(
            OscarService oscarService,
            UtilsService utilsService,
            TableFaitsService tableFaitsService,
            TableFaitsRepository tableFaitsRepository,
            AgregationService agregationService) {
        this.oscarService = oscarService;
        this.utilsService = utilsService;
        this.tableFaitsService = tableFaitsService;
        this.tableFaitsRepository = tableFaitsRepository;
        this.agregationService = agregationService;
    }

    public void miseAJourLinesTableFaitsEnBaseDeDonnees() throws IOException {

        List<Module> modules = oscarService.getModules();
        LocalDate now = LocalDate.now();
        log.debug("la date est {}", now);
        int compteurModuleOscarWithProjectKey = 0;
        int compteurModuleOscarWithProjectKeySansAnalyse = 0;
        for (Module module : modules) {
            if (!"null".equals(module.getKeySonar())
                    && !module.getKeySonar().equals("Sans objet")) {
                compteurModuleOscarWithProjectKey++;
                CouvertureTest ceLignes =
                        getIndicateurSonar(module.getKeySonar(), "lines_to_cover");
                CouvertureTest ceLignesTestes =
                        getIndicateurSonar(module.getKeySonar(), "uncovered_lines");
                if (!ceLignes.getCouverture().equals("Aucune couverture disponible")) {
                    // Utilisation d'un BigDecimal car en base nous avons un numéric. Risque
                    // d'overflow
                    tableFaitsRepository.save(
                            new TableFaits(
                                    module.getId(),
                                    module.getIdApplication(),
                                    Indicateur.NBR_LIGNE.getValue(),
                                    now,
                                    new BigDecimal(ceLignes.getCouverture()),
                                    0,
                                    null));
                    tableFaitsRepository.save(
                            new TableFaits(
                                    module.getId(),
                                    module.getIdApplication(),
                                    Indicateur.NBR_LIGNE_TEST.getValue(),
                                    now,
                                    new BigDecimal(ceLignesTestes.getCouverture()),
                                    0,
                                    null));
                } else {
                    log.warn("le module {} n'a pas une analyse sonar correcte", module.getId());
                    compteurModuleOscarWithProjectKeySansAnalyse++;
                }
            }
        }
        log.info(
                "Nombre de module Oscar avec un project Key Sonar : {}",
                compteurModuleOscarWithProjectKey);
        log.info(
                "Nombre de module Oscar avec un project Key Sonar mais pas d'analyse valide  : {}",
                compteurModuleOscarWithProjectKeySansAnalyse);
    }

    public Map<Integer, ModuleGrade> calculateTestGradesNiveauModule() throws IOException {

        // Récupérer les informations des modules depuis l'API
        List<Module> modules = oscarService.getModules();

        // Matrics au niveau module
        Map<Integer, TableFaits> mapByIdModuleLigneCode =
                tableFaitsService.getMapMetricByModule(Indicateur.NBR_LIGNE.getValue());
        Map<Integer, TableFaits> mapByIdModuleLigneNonTeste =
                tableFaitsService.getMapMetricByModule(Indicateur.NBR_LIGNE_TEST.getValue());

        // Construire une map enrichie pour tous les modules
        Map<Integer, ModuleGrade> result = new HashMap<>();

        // Traiter chaque module
        for (Module module : modules) {
            ModuleGrade moduleGrade =
                    new ModuleGrade(
                            module.getModName(),
                            module.getAppName(),
                            module.getSndi(),
                            module.getDomaineSndi(),
                            null,
                            null);
            Integer moduleId = module.getId();
            TableFaits moduleLigneCode = mapByIdModuleLigneCode.get(moduleId);
            TableFaits moduleLigneCodeNonTeste = mapByIdModuleLigneNonTeste.get(moduleId);

            if (moduleLigneCode != null && moduleLigneCode.getValeur() != null) {
                // Calculer le pourcentage
                double percentage =
                        utilsService.calculPourcentageCouvertureTest(
                                moduleLigneCode.getValeur().intValue(),
                                moduleLigneCodeNonTeste.getValeur().intValue());

                String pourcentage = (int) percentage + " %";

                // Obtenir la note
                String grade = utilsService.convertPourcentageEnNote(percentage);

                // Ajouter le module avec le grade calculé
                moduleGrade.setGrade(grade);
                moduleGrade.setPourcentage(pourcentage);
                result.put(moduleId, moduleGrade);

            } else {
                // Ajouter le module avec un grade par défaut
                if ("Sans objet".equals(module.getKeySonar().trim())) {
                    moduleGrade.setGrade("SO");
                    moduleGrade.setPourcentage("SO");
                    result.put(moduleId, moduleGrade);
                } else {
                    moduleGrade.setGrade("NR");
                    moduleGrade.setPourcentage("NR");
                    result.put(moduleId, moduleGrade);
                }
            }
        }

        return result;
    }

    public Map<Integer, ModuleGrade> calculateTestGradesNiveauApplication() throws IOException {
        // Metrics au niveau module
        Map<Integer, TableFaits> mapByIdModuleLigneCode =
                tableFaitsService.getMapMetricByModule(Indicateur.NBR_LIGNE.getValue());
        Map<Integer, TableFaits> mapByIdModuleLigneNonTeste =
                tableFaitsService.getMapMetricByModule(Indicateur.NBR_LIGNE_TEST.getValue());
        // Récupérer les informations des applications depuis l'API
        List<Application> applications = oscarService.getApplications();

        // Récupérer les informations des modules depuis l'API
        List<Module> modules = oscarService.getModules();

        Map<Integer, TableFaits> metricsLigneCode =
                agregationService.calculAgregationSum(
                        Indicateur.NBR_LIGNE.getValue(), mapByIdModuleLigneCode, modules);
        Map<Integer, TableFaits> metricsLigneCodeNonTeste =
                agregationService.calculAgregationSum(
                        Indicateur.NBR_LIGNE_TEST.getValue(), mapByIdModuleLigneNonTeste, modules);

        // Construire une map enrichie pour tous les modules,
        Map<Integer, ModuleGrade> result = new HashMap<>();

        // Traiter chaque application
        for (Application application : applications) {
            ModuleGrade moduleGrade =
                    new ModuleGrade(
                            null,
                            application.getAppName(),
                            application.getSndi(),
                            application.getDomaineSndi(),
                            null,
                            null);
            Integer applicationId = application.getIdApplication();
            TableFaits ligneCode = metricsLigneCode.get(applicationId);
            TableFaits ligneCodeNonTeste = metricsLigneCodeNonTeste.get(applicationId);

            if (ligneCode != null && ligneCode.getValeur() != null) {
                // Calculer le pourcentage
                double percentage =
                        utilsService.calculPourcentageCouvertureTest(
                                ligneCode.getValeur().intValue(),
                                ligneCodeNonTeste.getValeur().intValue());

                String pourcentage = (int) percentage + " %";

                // Obtenir la note
                String grade = utilsService.convertPourcentageEnNote(percentage);

                // Ajouter le module avec le grade calculé
                moduleGrade.setGrade(grade);
                moduleGrade.setPourcentage(pourcentage);
                result.put(applicationId, moduleGrade);

            } else {
                // Ajouter le module avec un grade par défaut
                moduleGrade.setGrade("NR");
                moduleGrade.setPourcentage("NR");
                result.put(applicationId, moduleGrade);
            }
        }

        return result;
    }
}
