package fr.insee.compas.service.qualite;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.Indicateur;
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
