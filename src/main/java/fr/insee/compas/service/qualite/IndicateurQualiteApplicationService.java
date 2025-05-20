package fr.insee.compas.service.qualite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.service.UtilsService;
import fr.insee.compas.view.IndicateurQualiteView;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IndicateurQualiteApplicationService {

    private final OscarService oscarService;

    private final TableFaitsService tableFaitsService;

    private final UtilsService utilsService;

    public IndicateurQualiteApplicationService(
            OscarService oscarService,
            TableFaitsService tableFaitsService,
            UtilsService utilsService) {
        this.oscarService = oscarService;
        this.tableFaitsService = tableFaitsService;
        this.utilsService = utilsService;
    }

    public List<IndicateurQualiteView> getIndicateurNiveauApplication() {

        Map<Integer, IndicateurQualiteView> mapQualite =
                tableFaitsService.getIndicateurApplicationQualite();

        // Récupérer les informations des modules depuis l'API
        List<Application> applications = oscarService.getApplications();

        // Metrics au niveau application
        Map<Integer, TableFaits> mapByIdModuleCveCritical =
                tableFaitsService.getMapMetricByApplication(
                        IndicateurType.CVE_CRITICAL_APPLI.getValue());
        Map<Integer, TableFaits> mapByIdModuleCveHigh =
                tableFaitsService.getMapMetricByApplication(
                        IndicateurType.CVE_HIGH_APPLI.getValue());
        Map<Integer, TableFaits> mapByIdModuleCveMedium =
                tableFaitsService.getMapMetricByApplication(
                        IndicateurType.CVE_MEDIUM_APPLI.getValue());
        Map<Integer, TableFaits> mapByIdModuleCveLow =
                tableFaitsService.getMapMetricByApplication(
                        IndicateurType.CVE_LOW_APPLI.getValue());

        List<IndicateurQualiteView> resultat = new ArrayList<>();

        // Traiter chaque application
        for (Application application : applications) {
            IndicateurQualiteView viewApplication = mapQualite.get(application.getIdApplication());
            if (viewApplication == null) {
                viewApplication = new IndicateurQualiteView();
            }
            viewApplication.setApplicationName(application.getAppName());
            viewApplication.setSndi(application.getSndi());
            viewApplication.setDomaineSndi(application.getDomaineSndi());
            viewApplication.setDomaineFonctionnel(application.getDomaineFonctionnel());
            viewApplication.setApplicationId(application.getIdApplication());

            Integer moduleApplication = application.getIdApplication();
            calculLettreCouvertureTest(viewApplication);

            calculLettreCve(
                    mapByIdModuleCveCritical,
                    moduleApplication,
                    viewApplication,
                    mapByIdModuleCveHigh,
                    mapByIdModuleCveMedium,
                    mapByIdModuleCveLow);
            calculLettreFiabilite(viewApplication);
            calculLettreDetteTechnique(viewApplication);

            viewApplication.calculerLettreGlobalQualite();
            resultat.add(viewApplication);
        }

        return resultat;
    }

    private void calculLettreCouvertureTest(IndicateurQualiteView viewApplication) {
        if (StringUtils.isNotEmpty(viewApplication.getNbLigneCode())) {
            if (Double.parseDouble(viewApplication.getNbLigneCode()) > 0) {
                double percentage =
                        utilsService.calculPourcentageCouvertureTest(
                                (int) Double.parseDouble(viewApplication.getNbLigneCode()),
                                (int) Double.parseDouble(viewApplication.getNbLigneCodeNonTeste()));

                String pourcentage = (int) percentage + " %";
                // Obtenir la note
                String lettre = utilsService.convertPourcentageEnNote(percentage);
                viewApplication.setPourcentageCouvertureTestUniaire(pourcentage);
                viewApplication.setLettreCouvertureTestUniaire(lettre);
            } else {
                // SI la somme des modules est négative tous les modules sont SO
                viewApplication.setPourcentageCouvertureTestUniaire("");
                viewApplication.setLettreCouvertureTestUniaire("SO");
                viewApplication.setLettreDetteTechnique("SO");
                viewApplication.setLettreFiabilite("SO");
            }

        } else {
            viewApplication.setPourcentageCouvertureTestUniaire("");
            viewApplication.setLettreCouvertureTestUniaire("NR");
            viewApplication.setLettreDetteTechnique("NR");
            viewApplication.setLettreFiabilite("NR");
        }
    }

    private void calculLettreCve(
            Map<Integer, TableFaits> mapByIdModuleCveCritical,
            Integer moduleApplication,
            IndicateurQualiteView viewApplication,
            Map<Integer, TableFaits> mapByIdModuleCveHigh,
            Map<Integer, TableFaits> mapByIdModuleCveMedium,
            Map<Integer, TableFaits> mapByIdModuleCveLow) {
        if (mapByIdModuleCveCritical != null
                && mapByIdModuleCveCritical.get(moduleApplication) != null) {
            viewApplication.setNbCveCritical(
                    mapByIdModuleCveCritical.get(moduleApplication).getValeur().toString());
            viewApplication.setNbCveHigh(
                    mapByIdModuleCveHigh.get(moduleApplication).getValeur().toString());
            viewApplication.setNbCveMedium(
                    mapByIdModuleCveMedium.get(moduleApplication).getValeur().toString());
            viewApplication.setNbCveLow(
                    mapByIdModuleCveLow.get(moduleApplication).getValeur().toString());

            BigDecimal calcul =
                    utilsService.getCalculIndicateurCve(
                            mapByIdModuleCveCritical.get(moduleApplication).getValeur(),
                            mapByIdModuleCveHigh.get(moduleApplication).getValeur(),
                            mapByIdModuleCveMedium.get(moduleApplication).getValeur(),
                            mapByIdModuleCveLow.get(moduleApplication).getValeur());
            viewApplication.setLettreNiveauCve(
                    utilsService.convertNiveauCveEnLettre(calcul.doubleValue()));
        }
    }

    private void calculLettreDetteTechnique(IndicateurQualiteView viewApplication) {
        if (StringUtils.isNotEmpty(viewApplication.getDetteTechnique())) {
            viewApplication.setLettreDetteTechnique(
                    utilsService.getLettreDetteTechnique(viewApplication.getDetteTechnique()));
        }
    }

    private void calculLettreFiabilite(IndicateurQualiteView viewApplication) {
        if (StringUtils.isNotEmpty(viewApplication.getFiabilite())) {
            viewApplication.setLettreFiabilite(
                    Character.toString(
                            (char) ('A' + Double.parseDouble(viewApplication.getFiabilite()) - 1)));
        }
    }
}
