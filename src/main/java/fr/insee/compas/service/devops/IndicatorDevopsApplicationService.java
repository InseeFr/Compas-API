package fr.insee.compas.service.devops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.view.IndicateurDevopsView;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IndicatorDevopsApplicationService {

    private final OscarService oscarService;

    private final TableFaitsService tableFaitsService;

    @Autowired
    public IndicatorDevopsApplicationService(
            OscarService oscarService, TableFaitsService tableFaitsService) {
        this.oscarService = oscarService;
        this.tableFaitsService = tableFaitsService;
    }

    public List<IndicateurDevopsView> getIndicateurNiveauApplication() {

        Map<Integer, IndicateurDevopsView> mapQualite =
                tableFaitsService.getIndicateurApplicationDevops();

        // Récupérer les informations des modules depuis l'API
        List<Application> applications = oscarService.getApplications();

        List<IndicateurDevopsView> resultat = new ArrayList<>();

        // Traiter chaque application
        for (Application application : applications) {
            IndicateurDevopsView viewApplication = mapQualite.get(application.getIdApplication());
            if (viewApplication == null) {
                viewApplication = new IndicateurDevopsView();
            }
            viewApplication.setApplicationName(application.getAppName());
            viewApplication.setSndi(application.getSndi());
            viewApplication.setDomaineSndi(application.getDomaineSndi());
            viewApplication.setDomaineFonctionnel(application.getDomaineFonctionnel());
            viewApplication.setApplicationId(application.getIdApplication());
            viewApplication.setLettreDistanceCount(
                    IndicateurDevopsLetterUtils.calculLettreDistanceCount(
                            viewApplication.getDistanceCount()));
            viewApplication.setLettreDeploymentCount(
                    IndicateurDevopsLetterUtils.calculLettreDeploymentCount(
                            viewApplication.getNbDeploymentCount()));
            viewApplication.setLettreContributorCount(
                    IndicateurDevopsLetterUtils.calculLettreContributorCount(
                            viewApplication.getNbContributorCount()));
            viewApplication.calculerLettreGlobalDevops();
            resultat.add(viewApplication);
        }

        return resultat;
    }
}
