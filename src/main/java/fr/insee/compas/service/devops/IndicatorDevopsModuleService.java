package fr.insee.compas.service.devops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.view.IndicateurDevopsView;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IndicatorDevopsModuleService {

    private final OscarService oscarService;

    private final TableFaitsService tableFaitsService;

    @Autowired
    public IndicatorDevopsModuleService(
            OscarService oscarService, TableFaitsService tableFaitsService) {
        this.oscarService = oscarService;
        this.tableFaitsService = tableFaitsService;
    }

    public List<IndicateurDevopsView> getIndicateurNiveauModule() {

        // Récupérer les informations des modules depuis l'API
        List<Module> modules = oscarService.getModules();

        // Metrics au niveau module
        Map<Integer, IndicateurDevopsView> mapDevops =
                tableFaitsService.getIndicateurModuleDevops();

        List<IndicateurDevopsView> resultat = new ArrayList<>();

        // Traiter chaque module
        for (Module module : modules) {
            IndicateurDevopsView viewModule = mapDevops.get(module.getId());
            if (viewModule == null) {
                viewModule = new IndicateurDevopsView();
            }
            viewModule.setModuleId(module.getId());
            viewModule.setApplicationName(module.getAppName());
            viewModule.setApplicationId(module.getIdApplication());
            viewModule.setSndi(module.getSndi());
            viewModule.setDomaineSndi(module.getDomaineSndi());
            viewModule.setModuleName(module.getModName());
            viewModule.setDomaineFonctionnel(module.getDomaineFonctionnel());
            viewModule.setLettreDistanceCount(
                    IndicateurDevopsLetterUtils.calculLettreDistanceCount(
                            viewModule.getDistanceCount()));
            viewModule.setLettreDeploymentCount(
                    IndicateurDevopsLetterUtils.calculLettreDeploymentCount(
                            viewModule.getNbDeploymentCount()));
            viewModule.setLettreContributorCount(
                    IndicateurDevopsLetterUtils.calculLettreContributorCount(
                            viewModule.getNbContributorCount()));
            viewModule.calculerLettreGlobalDevops();
            resultat.add(viewModule);
        }

        return resultat;
    }
}
