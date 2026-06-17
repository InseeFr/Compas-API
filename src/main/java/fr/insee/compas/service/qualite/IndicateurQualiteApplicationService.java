package fr.insee.compas.service.qualite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    private final CouvertureCalculateurService couvertureCalculateurService;
    private final FiabiliteCalculateurService fiabiliteCalculateurService;
    private final DetteTechniqueCalculateurService detteTechniqueCalculateurService;

    private final UtilsService utilsService;

    private final ConversionService conversionService;

    public IndicateurQualiteApplicationService(
            OscarService oscarService,
            TableFaitsService tableFaitsService,
            CouvertureCalculateurService couvertureCalculateurService,
            FiabiliteCalculateurService fiabiliteCalculateurService,
            DetteTechniqueCalculateurService detteTechniqueCalculateurService,
            UtilsService utilsService,
            ConversionService conversionService) {
        this.oscarService = oscarService;
        this.tableFaitsService = tableFaitsService;
        this.couvertureCalculateurService = couvertureCalculateurService;
        this.fiabiliteCalculateurService = fiabiliteCalculateurService;
        this.detteTechniqueCalculateurService = detteTechniqueCalculateurService;

        this.utilsService = utilsService;
        this.conversionService = conversionService;
    }

    public List<IndicateurQualiteView> getIndicateurNiveauApplication(
            Date dateOrigine, Date datePassee) {

        Map<Integer, IndicateurQualiteView> mapQualite =
                tableFaitsService.getIndicateurApplicationQualite(dateOrigine);
        Map<Integer, IndicateurQualiteView> mapQualiteHisto =
                tableFaitsService.getIndicateurApplicationQualite(datePassee);

        // Récupérer les informations des modules depuis l'API
        List<Application> applications = oscarService.getApplications();

        List<IndicateurQualiteView> resultat = new ArrayList<>();

        // Traiter chaque application
        for (Application application : applications) {
            IndicateurQualiteView viewApplication = mapQualite.get(application.getIdApplication());
            IndicateurQualiteView viewApplicationHisto =
                    mapQualiteHisto.get(application.getIdApplication());
            if (viewApplication == null) {
                viewApplication = new IndicateurQualiteView();
            }
            viewApplication.setApplicationName(application.getAppName());
            viewApplication.setSndi(application.getSndi());
            viewApplication.setDomaineSndi(application.getDomaineSndi());
            viewApplication.setDomaineFonctionnel(application.getDomaineFonctionnel());
            viewApplication.setApplicationId(application.getIdApplication());

            couvertureCalculateurService.calculCouvertureEtEvolution(
                    viewApplication, viewApplicationHisto, Context.APPLICATION, null);
            fiabiliteCalculateurService.calculFiabilite(
                    viewApplication, viewApplicationHisto, Context.APPLICATION, null);
            detteTechniqueCalculateurService.calculDetteTechnique(
                    viewApplication, viewApplicationHisto, Context.APPLICATION, null);

            viewApplication.calculerLettreGlobalQualiteEtEvolution();
            resultat.add(viewApplication);
        }

        return resultat;
    }
}
