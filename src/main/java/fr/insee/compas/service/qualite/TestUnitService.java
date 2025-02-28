package fr.insee.compas.service.qualite;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.ModuleGrade;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.AgregationService;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.service.UtilsService;

@Service
public class TestUnitService {

    private final OscarService oscarService;

    private final TableFaitsService tableFaitsService;

    private final UtilsService utilsService;

    private final AgregationService agregationService;

    public TestUnitService(
            OscarService oscarService,
            TableFaitsService tableFaitsService,
            UtilsService utilsService,
            AgregationService agregationService) {
        this.oscarService = oscarService;
        this.tableFaitsService = tableFaitsService;
        this.utilsService = utilsService;
        this.agregationService = agregationService;
    }

    public Map<Integer, ModuleGrade> calculateTestGradesNiveauModule() throws IOException {

        // Récupérer les informations des modules depuis l'API
        final List<Module> modules = oscarService.getModules();

        // Matrics au niveau module
        final Map<Integer, TableFaits> mapByIdModuleLigneCode =
                tableFaitsService.getMapMetricByModule(IndicateurType.NBR_LIGNE.getValue());
        final Map<Integer, TableFaits> mapByIdModuleLigneNonTeste =
                tableFaitsService.getMapMetricByModule(IndicateurType.NBR_LIGNE_TEST.getValue());

        // Construire une map enrichie pour tous les modules
        final Map<Integer, ModuleGrade> result = new HashMap<>();

        // Traiter chaque module
        for (final Module module : modules) {
            final ModuleGrade moduleGrade =
                    new ModuleGrade(
                            module.getModName(),
                            module.getAppName(),
                            module.getSndi(),
                            module.getDomaineSndi(),
                            null,
                            null);
            final Integer moduleId = module.getId();
            final TableFaits moduleLigneCode = mapByIdModuleLigneCode.get(moduleId);
            final TableFaits moduleLigneCodeNonTeste = mapByIdModuleLigneNonTeste.get(moduleId);

            if (moduleLigneCode != null && moduleLigneCode.getValeur() != null) {
                // Calculer le pourcentage
                final double percentage =
                        utilsService.calculPourcentageCouvertureTest(
                                moduleLigneCode.getValeur().intValue(),
                                moduleLigneCodeNonTeste.getValeur().intValue());

                final String pourcentage = (int) percentage + " %";

                // Obtenir la note
                final String grade = utilsService.convertPourcentageEnNote(percentage);

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
        final Map<Integer, TableFaits> mapByIdModuleLigneCode =
                tableFaitsService.getMapMetricByModule(IndicateurType.NBR_LIGNE.getValue());
        final Map<Integer, TableFaits> mapByIdModuleLigneNonTeste =
                tableFaitsService.getMapMetricByModule(IndicateurType.NBR_LIGNE_TEST.getValue());
        // Récupérer les informations des applications depuis l'API
        final List<Application> applications = oscarService.getApplications();

        // Récupérer les informations des modules depuis l'API
        final List<Module> modules = oscarService.getModules();

        final Map<Integer, TableFaits> metricsLigneCode =
                agregationService.calculAgregationSum(
                        IndicateurType.NBR_LIGNE.getValue(), mapByIdModuleLigneCode, modules);
        final Map<Integer, TableFaits> metricsLigneCodeNonTeste =
                agregationService.calculAgregationSum(
                        IndicateurType.NBR_LIGNE_TEST.getValue(),
                        mapByIdModuleLigneNonTeste,
                        modules);

        // Construire une map enrichie pour tous les modules,
        final Map<Integer, ModuleGrade> result = new HashMap<>();

        // Traiter chaque application
        for (final Application application : applications) {
            final ModuleGrade moduleGrade =
                    new ModuleGrade(
                            null,
                            application.getAppName(),
                            application.getSndi(),
                            application.getDomaineSndi(),
                            null,
                            null);
            final Integer applicationId = application.getIdApplication();
            final TableFaits ligneCode = metricsLigneCode.get(applicationId);
            final TableFaits ligneCodeNonTeste = metricsLigneCodeNonTeste.get(applicationId);

            if (ligneCode != null && ligneCode.getValeur() != null) {
                // Calculer le pourcentage
                final double percentage =
                        utilsService.calculPourcentageCouvertureTest(
                                ligneCode.getValeur().intValue(),
                                ligneCodeNonTeste.getValeur().intValue());

                final String pourcentage = (int) percentage + " %";

                // Obtenir la note
                final String grade = utilsService.convertPourcentageEnNote(percentage);

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
