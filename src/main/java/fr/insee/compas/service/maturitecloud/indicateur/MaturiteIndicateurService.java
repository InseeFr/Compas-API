package fr.insee.compas.service.maturitecloud.indicateur;

import static fr.insee.compas.util.MaturiteConstantes.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import fr.insee.compas.dto.maturite.DerniereStrategieCloudDto;
import fr.insee.compas.repository.projection.maturite.DerniereMaturiteProjection;
import fr.insee.compas.service.conversion.strategie.IConversionStrategie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.insee.compas.dto.maturite.MaturiteIndicateurDto;
import fr.insee.compas.exception.MaturiteIndicateurException;
import fr.insee.compas.repository.projection.maturite.MaturiteIndicateurTableProjection;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.util.MaturiteConstantes;
import fr.insee.compas.view.IndicateurMaturiteView;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MaturiteIndicateurService implements IMaturiteIndicateur {

    @Autowired
    private OscarService oscarService;

    @Autowired
    private TableFaitsRepository tableFaitsRepository;

    @Autowired
    private MaturiteCalculatorService maturiteCalculatorService;

    @Autowired
    private MaturiteMapperIndicateur maturiteMapperIndicateur;

    @Autowired
    @Qualifier("strategieCloudConversion")
    private IConversionStrategie<String, Integer> strategieCloudConversion;

    @Autowired
    @Qualifier("maturiteEnvCibleConversion")
    private IConversionStrategie<String, Integer> envCibleConversion;

    @Override
    public List<IndicateurMaturiteView> getIndicateurMaturite() {
        try {
            log.info("Récupération des résultats SQL et de la liste des modules depuis Oscar");

            CompletableFuture<List<MaturiteIndicateurTableProjection>> resultFuture =
                    CompletableFuture.supplyAsync(
                            () ->
                                    tableFaitsRepository.getValuesByMaturiteIndicateur(
                                            MATURITEINDICATEURSLIST));

            CompletableFuture<List<Module>> modulesFuture =
                    CompletableFuture.supplyAsync(() -> oscarService.getModules());

            CompletableFuture<Map<Integer, String>> maturiteByAppFuture =
                    CompletableFuture.supplyAsync(
                            () ->
                                    tableFaitsRepository
                                            .getMaturitesByIdIndicateur(ID_INDICATEUR_MATURITE)
                                            .stream()
                                            .collect(
                                                    Collectors.toMap(
                                                            MaturiteIndicateurTableProjection
                                                                    ::getIdApplication,
                                                            v ->
                                                                    maturiteCalculatorService
                                                                            .calculateMaturiteCloud(
                                                                                    v
                                                                                            .getValeur()))));

            CompletableFuture.allOf(resultFuture, modulesFuture, maturiteByAppFuture).join();

            List<MaturiteIndicateurTableProjection> result = resultFuture.get();
            List<Module> modules = modulesFuture.get();
            Map<Integer, String> maturiteByApp = maturiteByAppFuture.get();
            log.info(
                    "Récupération terminée: {} résultats sql et {} modules oscar",
                    result.size(),
                    modules.size());

            if (result.isEmpty()) {
                log.warn(
                        "Aucun résultat trouvé pour les indicateurs de maturité : {}(env cible) et"
                                + " {}(stratégie cloud)",
                        ID_INDICATEUR_ENV_CIBLE,
                        ID_INDICATEUR_STRAT_CLOUD);
                return Collections.emptyList();
            }

            Map<Integer, MaturiteConstantes.ModuleInfo> moduleMapped =
                    maturiteMapperIndicateur.getModulesMapped(modules);
            Map<Integer, List<MaturiteIndicateurDto>> maturiteIndicateurModuleByApp =
                    maturiteMapperIndicateur.resultatMaturiteIndicateurToMapByApp(
                            result, moduleMapped);

            Set<Integer> idAppsOscar = maturiteMapperIndicateur.verifyIdAppsOscar(modules);

            List<IndicateurMaturiteView> indicateurMaturiteViewList =
                    maturiteMapperIndicateur.maturiteMapToListIndicateurMaturiteView(
                            maturiteByApp, maturiteIndicateurModuleByApp, idAppsOscar);

            log.info("Résultat final : {} views traitées", indicateurMaturiteViewList.size());
            return indicateurMaturiteViewList;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrompu lors de l'attente des futures", e);
            throw new MaturiteIndicateurException("Thread interrompu", e);
        } catch (CompletionException | ExecutionException e) {
            log.error(
                    "Erreur de completion des futures durant la récupération des requêtes sql et"
                            + " des modules oscar",
                    e);
            throw new MaturiteIndicateurException("Erreur completion des futures", e);
        }
    }

    @Override
    public DerniereStrategieCloudDto getDerniereStrategieCloud(Integer idModule) {
        try {
            List<DerniereMaturiteProjection> results =
                    tableFaitsRepository.findDerniereStrategieCloud(idModule);

            if (results.isEmpty()) {
                throw new MaturiteIndicateurException("Aucune valeur n'a été trouvée pour ce module");
            }

            Integer strategieCloud = getValeurByIndicateur(results, ID_INDICATEUR_STRAT_CLOUD);

            Integer envCible = getValeurByIndicateur(results, ID_INDICATEUR_ENV_CIBLE);

            String commentaire = results.stream()
                    .filter(r -> Objects.equals(
                            r.getIdIndicateur(),
                            ID_INDICATEUR_STRAT_CLOUD))
                    .map(DerniereMaturiteProjection::getCommentaire)
                    .findFirst()
                    .orElse(null);

            String strategieCloudLibelle = strategieCloud != null ? strategieCloudConversion.conversion(strategieCloud) : null;

            String envCibleLibelle = envCible != null ? envCibleConversion.conversion(envCible) : null;

            return DerniereStrategieCloudDto.builder()
                    .idMod(idModule)
                    .strategieCloud(strategieCloudLibelle)
                    .envCibleProd(envCibleLibelle)
                    .commentaire(commentaire)
                    .build();

        } catch (Exception e) {
            log.error(
                    "Erreur lors de la récupération de la stratégie cloud pour le module {}", idModule, e);

            return null;
        }
    }
    private Integer getValeurByIndicateur(List<DerniereMaturiteProjection> derniereMaturiteProjections, Integer idIndicateur){
        return derniereMaturiteProjections.stream().filter(derniereMaturiteProjection -> Objects.equals(derniereMaturiteProjection.getIdIndicateur(), idIndicateur)).mapToInt(DerniereMaturiteProjection::getValeur).sum();
    }
}
