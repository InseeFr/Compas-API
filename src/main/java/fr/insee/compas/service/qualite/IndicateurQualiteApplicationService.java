package fr.insee.compas.service.qualite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.service.UtilsService;
import fr.insee.compas.service.conversion.ConversionService;
import fr.insee.compas.view.IndicateurQualiteView;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IndicateurQualiteApplicationService {

    private final OscarService oscarService;

    private final TableFaitsService tableFaitsService;

    private final UtilsService utilsService;

    private final ConversionService conversionService;

    public IndicateurQualiteApplicationService(
            OscarService oscarService,
            TableFaitsService tableFaitsService,
            UtilsService utilsService,
            ConversionService conversionService) {
        this.oscarService = oscarService;
        this.tableFaitsService = tableFaitsService;
        this.utilsService = utilsService;
        this.conversionService = conversionService;
    }

    public List<IndicateurQualiteView> getIndicateurNiveauApplication() {

        Map<Integer, IndicateurQualiteView> mapQualite =
                tableFaitsService.getIndicateurApplicationQualite();

        // Récupérer les informations des modules depuis l'API
        List<Application> applications = oscarService.getApplications();

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

            calculLettreCouvertureTest(viewApplication);

            calculLettreFiabilite(viewApplication);
            calculLettreDetteTechnique(viewApplication);

            viewApplication.calculerLettreGlobalQualite();
            resultat.add(viewApplication);
        }

        return resultat;
    }

    private void calculLettreCouvertureTest(IndicateurQualiteView viewApplication) {
        log.debug(viewApplication.getApplicationName());

        if (StringUtils.isNotEmpty(viewApplication.getNbLigneCode())) {
            if (viewApplication.getNbLigneCodeNonTeste().isEmpty()) {
                viewApplication.setNbLigneCodeNonTeste("0");
            }
            if (Double.parseDouble(viewApplication.getNbLigneCode()) > 0) {
                double percentage =
                        utilsService.calculPourcentageCouvertureTest(
                                (int) Double.parseDouble(viewApplication.getNbLigneCode()),
                                (int) Double.parseDouble(viewApplication.getNbLigneCodeNonTeste()));

                String pourcentage = (int) percentage + " %";
                // Obtenir la note
                String lettre = conversionService.convertPourcentageEnNote(percentage);
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

    private void calculLettreDetteTechnique(IndicateurQualiteView viewApplication) {
        if (StringUtils.isNotEmpty(viewApplication.getDetteTechnique())) {
            viewApplication.setLettreDetteTechnique(
                    conversionService.convertDetteTechnique(viewApplication.getDetteTechnique()));
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
