package fr.insee.compas.service.qualite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.service.UtilsService;
import fr.insee.compas.view.IndicateurModuleQualiteView;

@Service
public class IndicateurQualiteModuleService {

    private final OscarService oscarService;

    private final TableFaitsService tableFaitsService;

    private final UtilsService utilsService;

    public IndicateurQualiteModuleService(
            OscarService oscarService,
            TableFaitsService tableFaitsService,
            UtilsService utilsService) {
        this.oscarService = oscarService;
        this.tableFaitsService = tableFaitsService;
        this.utilsService = utilsService;
    }

    /**
     * Extrait en base de données les indicateurs de types qualité dans des Map<IdModule,TableFait>
     * Calcul les indicateurs finaux à partir des indicateurs brute
     *
     * @return Liste des indicateurs avec les informations de filtre des modules pour affichages
     *     dans le table.
     */
    public List<IndicateurModuleQualiteView> getIndicateurNiveauModule() {

        // Récupérer les informations des modules depuis l'API
        List<Module> modules = oscarService.getModules();

        // Matrics au niveau module
        Map<Integer, TableFaits> mapByIdModuleLigneCode =
                tableFaitsService.getMapMetricByModule(IndicateurType.NBR_LIGNE.getValue());
        Map<Integer, TableFaits> mapByIdModuleLigneNonTeste =
                tableFaitsService.getMapMetricByModule(IndicateurType.NBR_LIGNE_TEST.getValue());
        Map<Integer, TableFaits> mapByIdModuleCveCritical =
                tableFaitsService.getMapMetricByModule(IndicateurType.CVE_CRITICAL.getValue());
        Map<Integer, TableFaits> mapByIdModuleCveHigh =
                tableFaitsService.getMapMetricByModule(IndicateurType.CVE_HIGH.getValue());
        Map<Integer, TableFaits> mapByIdModuleCveMedium =
                tableFaitsService.getMapMetricByModule(IndicateurType.CVE_MEDIUM.getValue());
        Map<Integer, TableFaits> mapByIdModuleCveLow =
                tableFaitsService.getMapMetricByModule(IndicateurType.CVE_LOW.getValue());

        List<IndicateurModuleQualiteView> resultat = new ArrayList<>();

        // Traiter chaque module
        for (Module module : modules) {
            IndicateurModuleQualiteView viewModule = new IndicateurModuleQualiteView();
            viewModule.setModuleId(module.getId());
            viewModule.setApplicationName(module.getAppName());
            viewModule.setSndi(module.getSndi());
            viewModule.setDomaine(module.getDomaineSndi());
            viewModule.setModuleName(module.getModName());

            Integer moduleId = module.getId();
            calculIndicateurCouvertureTestUnitaire(
                    module,
                    mapByIdModuleLigneCode,
                    moduleId,
                    mapByIdModuleLigneNonTeste,
                    viewModule);

            calculIndicateurCve(
                    moduleId,
                    mapByIdModuleCveCritical,
                    mapByIdModuleCveHigh,
                    mapByIdModuleCveMedium,
                    mapByIdModuleCveLow,
                    viewModule);
            resultat.add(viewModule);
        }

        return resultat;
    }

    private void calculIndicateurCve(
            Integer moduleId,
            Map<Integer, TableFaits> mapByIdModuleCveCritical,
            Map<Integer, TableFaits> mapByIdModuleCveHigh,
            Map<Integer, TableFaits> mapByIdModuleCveMedium,
            Map<Integer, TableFaits> mapByIdModuleCveLow,
            IndicateurModuleQualiteView viewModule) {
        if (mapByIdModuleCveCritical.size() > 1 && mapByIdModuleCveCritical.get(moduleId) != null) {
            TableFaits c = mapByIdModuleCveCritical.get(moduleId);
            TableFaits e = mapByIdModuleCveHigh.get(moduleId);
            TableFaits m = mapByIdModuleCveMedium.get(moduleId);
            TableFaits f = mapByIdModuleCveLow.get(moduleId);

            viewModule.setNbCveLow(String.valueOf(f.getValeur()));
            viewModule.setNbCveMedium(String.valueOf(m.getValeur()));
            viewModule.setNbCveHigh(String.valueOf(e.getValeur()));
            viewModule.setNbCveCritical(String.valueOf(c.getValeur()));
            BigDecimal calcul =
                    utilsService.getCalculIndicateurCve(
                            c.getValeur(), e.getValeur(), m.getValeur(), f.getValeur());
            viewModule.setLettreNiveauCve(
                    utilsService.convertNiveauCveEnLettre(calcul.doubleValue()));
        }
    }

    private void calculIndicateurCouvertureTestUnitaire(
            Module module,
            Map<Integer, TableFaits> mapByIdModuleLigneCode,
            Integer moduleId,
            Map<Integer, TableFaits> mapByIdModuleLigneNonTeste,
            IndicateurModuleQualiteView viewModule) {

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
            String lettre = utilsService.convertPourcentageEnNote(percentage);

            // Ajouter le module avec le grade calculé
            viewModule.setPourcentageCouvertureTestUniaire(pourcentage);
            viewModule.setLettreCouvertureTestUniaire(lettre);

        } else {
            // Ajouter le module avec un grade par défaut
            if ("Sans objet".equals(module.getKeySonar().trim())) {
                viewModule.setPourcentageCouvertureTestUniaire("SO");
                viewModule.setLettreCouvertureTestUniaire("SO");

            } else {
                viewModule.setPourcentageCouvertureTestUniaire("NR");
                viewModule.setLettreCouvertureTestUniaire("NR");
            }
        }
    }
}
