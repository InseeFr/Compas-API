package fr.insee.compas.service.maturitecloud.indicateur;

import static fr.insee.compas.util.MaturiteConstantes.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.compas.dto.MaturiteIndicateurDto;
import fr.insee.compas.exception.MaturiteIndicateurException;
import fr.insee.compas.model.maturite.MaturiteIndicateurTableProjection;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.util.MaturiteConstantes;
import fr.insee.compas.view.IndicateurMaturiteView;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class MaturiteIndicateurService implements IMaturiteIndicateur {

    private OscarService oscarService;
    private TableFaitsRepository tableFaitsRepository;

    private MaturiteCalculatorService maturiteCalculatorService;
    private MaturiteMapperIndicateur maturiteMapperIndicateur;

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

            List<IndicateurMaturiteView> indicateurMaturiteViewList =
                    maturiteMapperIndicateur.maturiteMapToListIndicateurMaturiteView(
                            maturiteByApp, maturiteIndicateurModuleByApp);

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
}
