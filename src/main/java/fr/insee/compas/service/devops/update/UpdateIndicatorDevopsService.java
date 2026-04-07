package fr.insee.compas.service.devops.update;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.devops.update.strat.IUpdateDevopsStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsable de la mise à jour en base des indicateurs DevOps (jours depuis dernière MEP,
 * nombre de déploiements, nombre de contributions).
 *
 * <p>Les indicateurs sont calculés à deux niveaux :
 *
 * <ul>
 *   <li>par module applicatif
 *   <li>par application (moyenne arrondie des modules)
 * </ul>
 *
 * <p>Les résultats sont persistés en base via {@link TableFaitsRepository}.
 */
@Service
@Slf4j
public class UpdateIndicatorDevopsService {

    private final IUpdateDevopsStrategy updateCountDeploy;

    private final IUpdateDevopsStrategy updateNbrMep;

    private final IUpdateDevopsStrategy updateContributorCount;

    private final OscarService oscarService;

    public UpdateIndicatorDevopsService(
            @Qualifier("CountDeploy") IUpdateDevopsStrategy updateCountDeploy,
            @Qualifier("NbrMep") IUpdateDevopsStrategy updateNbrMep,
            @Qualifier("Contributeur") IUpdateDevopsStrategy updateContributorCount,
            OscarService oscarService) {
        this.updateCountDeploy = updateCountDeploy;
        this.updateNbrMep = updateNbrMep;
        this.updateContributorCount = updateContributorCount;
        this.oscarService = oscarService;
    }

    /**
     * Lance la mise à jour des trois indicateurs principaux :
     *
     * <ul>
     *   <li>{@link IndicateurType#NBR_JOUR_MEP}
     *   <li>{@link IndicateurType#DEPLOYMENT_COUNT}
     *   <li>{@link IndicateurType#NBR_CONTRIBUTIONS_PROJET}
     * </ul>
     *
     * @param startDate borne de début pour les indicateurs temporels
     * @param endDate borne de fin pour les indicateurs temporels
     */
    public void miseAJourIndicateursDevopsEnBaseDeDonnes(
            LocalDateTime startDate, LocalDateTime endDate) {
        List<Module> modules = oscarService.getModules();
        Map<String, List<ModuleHistorique>> moduleHistoriques = oscarService.getModulesHistorique();
        log.info("****** Début mise à jour NBR_JOUR_MEP *******");
        updateNbrMep.updateDevops(null, null, modules, null);
        log.info("****** Fin mise à jour NBR_JOUR_MEP ********");

        log.info("****** Début mise à jour DEPLOYMENT_COUNT ********");
        updateCountDeploy.updateDevops(startDate, endDate, modules, moduleHistoriques);
        log.info("****** Fin mise à jour DEPLOYMENT_COUNT ********");

        log.info("****** Début mise à jour NBR_CONTRIBUTIONS_PROJET ********");
        updateContributorCount.updateDevops(startDate, endDate, modules, null);
        log.info("****** Fin mise à jour NBR_CONTRIBUTIONS_PROJET ********");
    }
}
