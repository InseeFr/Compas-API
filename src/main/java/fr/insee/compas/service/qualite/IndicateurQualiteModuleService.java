package fr.insee.compas.service.qualite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.view.IndicateurQualiteView;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IndicateurQualiteModuleService {

    private final OscarService oscarService;

    private final TableFaitsService tableFaitsService;

    private final CouvertureCalculateurService couvertureCalculateurService;

    private final FiabiliteCalculateurService fiabiliteCalculateurService;

    private final DetteTechniqueCalculateurService detteTechniqueCalculateurService;

    public IndicateurQualiteModuleService(
            OscarService oscarService,
            TableFaitsService tableFaitsService,
            CouvertureCalculateurService couvertureCalculateurService,
            FiabiliteCalculateurService fiabiliteCalculateurService,
            DetteTechniqueCalculateurService detteTechniqueCalculateurService) {
        this.oscarService = oscarService;
        this.tableFaitsService = tableFaitsService;
        this.couvertureCalculateurService = couvertureCalculateurService;
        this.fiabiliteCalculateurService = fiabiliteCalculateurService;
        this.detteTechniqueCalculateurService = detteTechniqueCalculateurService;
    }

    /**
     * Extrait en base de données les indicateurs de types qualité dans des Map<IdModule,TableFait>
     * Calcul les indicateurs finaux à partir des indicateurs brute
     *
     * @return Liste des indicateurs avec les informations de filtre des modules pour affichages
     *     dans le table.
     */
    public List<IndicateurQualiteView> getIndicateurNiveauModule(
            Date dateOrigine, Date datePassee) {

        // Récupérer les informations des modules depuis l'API
        List<Module> modules = oscarService.getModules();

        // Metrics au niveau module
        Map<Integer, IndicateurQualiteView> mapQualite =
                tableFaitsService.getIndicateurModuleQualite(dateOrigine);

        Map<Integer, IndicateurQualiteView> mapQualiteHisto =
                tableFaitsService.getIndicateurModuleQualite(datePassee);

        log.debug("date du jour {}", new Date());
        log.debug("date du mois avant {}", datePassee);
        List<IndicateurQualiteView> resultat = new ArrayList<>();

        // Traiter chaque module
        for (Module module : modules) {
            IndicateurQualiteView viewModule = mapQualite.get(module.getId());
            IndicateurQualiteView viewModuleHisto = mapQualiteHisto.get(module.getId());
            if (viewModule == null) {
                viewModule = new IndicateurQualiteView();
            }
            viewModule.setModuleId(module.getId());
            viewModule.setApplicationName(module.getAppName());
            viewModule.setSndi(module.getSndi());
            viewModule.setDomaineSndi(module.getDomaineSndi());
            viewModule.setModuleName(module.getModName());
            viewModule.setDomaineFonctionnel(module.getDomaineFonctionnel());

            couvertureCalculateurService.calculCouvertureEtEvolution(
                    viewModule, viewModuleHisto, Context.MODULE, module);

            fiabiliteCalculateurService.calculFiabilite(
                    viewModule, viewModuleHisto, Context.MODULE, module);
            detteTechniqueCalculateurService.calculDetteTechnique(
                    viewModule, viewModuleHisto, Context.MODULE, module);

            viewModule.calculerLettreGlobalQualiteEtEvolution();
            resultat.add(viewModule);
        }

        return resultat;
    }
}
