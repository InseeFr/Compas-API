package fr.insee.compas.service.devops;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;
import fr.insee.compas.service.devops.update.strat.UpdateDepCountDevops;
import fr.insee.compas.util.DevopsConstantes;
import fr.insee.compas.util.IndicatorSpecialValue;
import fr.insee.compas.util.SaveTFByIndicator;
import fr.insee.compas.util.observer.EventTypeObserver;
import fr.insee.compas.util.observer.IEventManager;

@ExtendWith(MockitoExtension.class)
class UpdateDepCountDevopsTest {

    @Mock private SaveTFByIndicator saveTFByIndicator;
    @Mock private IEventManager eventObserverManager;

    @InjectMocks private UpdateDepCountDevops updateDepCountDevops;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Module buildModule(int id, int idApp) {
        return Module.builder().id(id).idApplication(idApp).build();
    }

    private Module buildModule(String sourceCreation, String statut) {
        Module m = Module.builder().id(1).idApplication(100).build();
        m.setSourceCreation(sourceCreation);
        m.setStatut(statut);
        return m;
    }

    /**
     * Crée un ModuleHistorique dont la date de déploiement est dans la plage [startDate, endDate]
     * ou en dehors selon le paramètre {@code inRange}.
     */
    private ModuleHistorique buildHistorique(boolean inRange) {
        ModuleHistorique h = new ModuleHistorique();
        h.setAuteurOperation("svc_" + DevopsConstantes.SERVICE);
        h.setOperation(DevopsConstantes.MODIFICATION);
        h.setDateOperation(inRange ? startDate.plusHours(1) : startDate.minusDays(10));
        return h;
    }

    private void verifySavedModule(int moduleId, int appId, BigDecimal value) {
        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(moduleId),
                        eq(appId),
                        eq(IndicateurType.DEPLOYMENT_COUNT),
                        eq(value),
                        eq(SourceType.OSCAR));
    }

    private void verifySavedApplication(int appId, BigDecimal value) {
        verify(saveTFByIndicator)
                .saveByIndicator(
                        isNull(),
                        eq(appId),
                        eq(IndicateurType.DEPLOYMENT_COUNT),
                        eq(value),
                        eq(SourceType.OSCAR));
    }

    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.now().minusDays(7);
        endDate = LocalDateTime.now();
    }

    @Test
    void shouldDoNothingWhenModulesNull() {
        updateDepCountDevops.updateDevops(startDate, endDate, null, Map.of());

        verifyNoInteractions(saveTFByIndicator);
    }

    @Test
    void shouldDoNothingWhenModulesEmpty() {
        updateDepCountDevops.updateDevops(startDate, endDate, Collections.emptyList(), Map.of());

        verifyNoInteractions(saveTFByIndicator);
    }

    /** sourceCreation == SAISIE_MANUELLE → NR, indépendamment du reste. */
    @Test
    void shouldSaveNRWhenSourceCreationIsSaisieManuelle() {
        Module mod = buildModule(DevopsConstantes.SAISIE_MANUELLE, null);

        updateDepCountDevops.updateDevops(startDate, endDate, List.of(mod), Map.of());

        verifySavedModule(1, 100, BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode()));
    }

    /** statut == EN_DEVELOPPEMENT → SO (priorité après SAISIE_MANUELLE). */
    @Test
    void shouldSaveSOWhenStatutIsEnDeveloppement() {
        Module mod = buildModule(null, DevopsConstantes.EN_DEVELOPPEMENT);

        updateDepCountDevops.updateDevops(startDate, endDate, List.of(mod), Map.of());

        verifySavedModule(1, 100, BigDecimal.valueOf(IndicatorSpecialValue.SO.getCode()));
    }

    /** Aucun historique pour ce module → NR. */
    @Test
    void shouldSaveNRWhenHistoriqueIsNull() {
        Module mod = buildModule(1, 100);

        updateDepCountDevops.updateDevops(startDate, endDate, List.of(mod), Map.of());

        verifySavedModule(1, 100, BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode()));
    }

    /** Historique présent mais aucun déploiement dans la plage → 0. */
    @Test
    void shouldSaveZeroWhenNoDeploymentInRange() {
        Module mod = buildModule(1, 100);
        ModuleHistorique hHorsPlage = buildHistorique(false);

        // liste présente (non null) mais aucun élément valide
        Map<String, List<ModuleHistorique>> historiques = Map.of("1", List.of(hHorsPlage));

        updateDepCountDevops.updateDevops(startDate, endDate, List.of(mod), historiques);

        verifySavedModule(1, 100, BigDecimal.valueOf(0));
        verifySavedApplication(100, BigDecimal.valueOf(0));
    }

    /** Historique présent avec N déploiements valides → N. */
    @Test
    void shouldCountValidDeploymentsInRange() {
        Module mod = buildModule(1, 100);
        ModuleHistorique h1 = buildHistorique(true);
        ModuleHistorique h2 = buildHistorique(true);
        ModuleHistorique hOut = buildHistorique(false);

        Map<String, List<ModuleHistorique>> historiques = Map.of("1", List.of(h1, h2, hOut));

        updateDepCountDevops.updateDevops(startDate, endDate, List.of(mod), historiques);

        verifySavedModule(1, 100, BigDecimal.valueOf(2));
        verifySavedApplication(100, BigDecimal.valueOf(2));
    }

    /** Exception dans saveModule → notifyObservers, le module n'est pas ajouté à valuesByApp. */
    @Test
    void shouldNotifyObserverAndSkipAppAggregationWhenSaveModuleThrows() {
        Module mod = buildModule(1, 100);

        doThrow(new RuntimeException("DB error"))
                .when(saveTFByIndicator)
                .saveByIndicator(eq(1), eq(100), any(), any(), any());

        updateDepCountDevops.updateDevops(startDate, endDate, List.of(mod), Map.of());

        verify(eventObserverManager, atLeastOnce())
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
        // Aucune sauvegarde pour l'application car valuesByApp est vide
        verify(saveTFByIndicator, never()).saveByIndicator(isNull(), anyInt(), any(), any(), any());
    }

    /** Une exception sur un module ne doit pas bloquer les modules suivants. */
    @Test
    void shouldContinueProcessingOtherModulesAfterException() {
        Module modFail = buildModule(1, 100);
        Module modOk = buildModule(2, 100);

        // module 1 plante à la sauvegarde
        doThrow(new RuntimeException("DB error"))
                .when(saveTFByIndicator)
                .saveByIndicator(eq(1), eq(100), any(), any(), any());

        updateDepCountDevops.updateDevops(startDate, endDate, List.of(modFail, modOk), Map.of());

        // module 2 (NR car pas d'historique) doit quand même être sauvegardé
        verifySavedModule(2, 100, BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode()));
        verify(eventObserverManager, atLeastOnce())
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
    }

    /** Un seul module → la moyenne est égale à sa valeur. */
    @Test
    void shouldSaveModuleValueAsAverageWhenSingleModule() {
        Module mod = buildModule(1, 100);
        ModuleHistorique h = buildHistorique(true);

        updateDepCountDevops.updateDevops(
                startDate, endDate, List.of(mod), Map.of("1", List.of(h)));

        verifySavedModule(1, 100, BigDecimal.valueOf(1));
        verifySavedApplication(100, BigDecimal.valueOf(1));
    }

    /** Deux modules de la même application → moyenne arrondie sauvegardée. */
    @Test
    void shouldSaveRoundedAverageForApplication() {
        Module mod1 = buildModule(1, 200);
        Module mod2 = buildModule(2, 200);

        ModuleHistorique h1 = buildHistorique(true);
        ModuleHistorique h2 = buildHistorique(true);
        ModuleHistorique h3 = buildHistorique(true);
        ModuleHistorique hOut = buildHistorique(false); // hors plage pour mod2

        Map<String, List<ModuleHistorique>> historiques =
                Map.of(
                        "1", List.of(h1, h2, h3),
                        "2", List.of(hOut)); // liste non null, 0 déploiement valide

        updateDepCountDevops.updateDevops(startDate, endDate, List.of(mod1, mod2), historiques);

        verifySavedModule(1, 200, BigDecimal.valueOf(3));
        verifySavedModule(2, 200, BigDecimal.valueOf(0));
        // Math.round((3 + 0) / 2.0) = Math.round(1.5) = 2
        verifySavedApplication(200, BigDecimal.valueOf(2));
    }

    /** Modules de deux applications différentes → une sauvegarde par application. */
    @Test
    void shouldSaveOneAveragePerApplication() {
        Module mod1 = buildModule(1, 300);
        Module mod2 = buildModule(2, 400);

        updateDepCountDevops.updateDevops(startDate, endDate, List.of(mod1, mod2), Map.of());

        verifySavedApplication(300, BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode()));
        verifySavedApplication(400, BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode()));
    }

    /** Exception dans saveApplication → notifyObservers. */
    @Test
    void shouldNotifyObserverWhenSaveApplicationThrows() {
        Module mod = buildModule(1, 100);

        doThrow(new RuntimeException("Contrainte DB"))
                .when(saveTFByIndicator)
                .saveByIndicator(isNull(), eq(100), any(), any(), any());

        updateDepCountDevops.updateDevops(startDate, endDate, List.of(mod), Map.of());

        verify(eventObserverManager, atLeastOnce())
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
    }

    /** saveModule ajoute la valeur dans valuesByApp quand la sauvegarde réussit. */
    @Test
    void shouldPopulateValuesByAppWhenSaveModuleSucceeds() {
        Module mod = buildModule(1, 100);
        Map<Integer, List<Integer>> valuesByApp = new HashMap<>();

        updateDepCountDevops.saveModule(mod, 5, valuesByApp);

        verifySavedModule(1, 100, BigDecimal.valueOf(5));
        assert valuesByApp.get(100).contains(5);
    }

    /** saveModule n'ajoute rien dans valuesByApp quand la sauvegarde échoue. */
    @Test
    void shouldNotPopulateValuesByAppWhenSaveModuleFails() {
        Module mod = buildModule(1, 100);
        Map<Integer, List<Integer>> valuesByApp = new HashMap<>();

        doThrow(new RuntimeException("erreur"))
                .when(saveTFByIndicator)
                .saveByIndicator(any(), any(), any(), any(), any());

        updateDepCountDevops.saveModule(mod, 5, valuesByApp);

        assert valuesByApp.isEmpty();
        verify(eventObserverManager)
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
    }

    /** saveApplication avec une map vide → aucune sauvegarde. */
    @Test
    void shouldDoNothingWhenValuesByAppIsEmpty() {
        updateDepCountDevops.saveApplication(Map.of());

        verifyNoInteractions(saveTFByIndicator);
    }
}
