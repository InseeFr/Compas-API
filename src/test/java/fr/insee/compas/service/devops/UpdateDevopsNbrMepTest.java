package fr.insee.compas.service.devops;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import fr.insee.compas.service.devops.update.strat.UpdateDevopsNbrMep;
import fr.insee.compas.util.DevopsConstantes;
import fr.insee.compas.util.IndicatorSpecialValue;
import fr.insee.compas.util.SaveTFByIndicator;
import fr.insee.compas.util.observer.EventTypeObserver;
import fr.insee.compas.util.observer.IEventManager;

@ExtendWith(MockitoExtension.class)
class UpdateDevopsNbrMepTest {

    @Mock private SaveTFByIndicator saveTFByIndicator;
    @Mock private IEventManager eventObserverManager;

    @InjectMocks private UpdateDevopsNbrMep updateDevopsNbrMep;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Module buildModule() {
        return Module.builder().id(1).idApplication(100).build();
    }

    private Module buildModule(
            int id, int idApp, String sourceCreation, String statut, LocalDate dateLivraison) {
        Module m = Module.builder().id(id).idApplication(idApp).build();
        m.setSourceCreation(sourceCreation);
        m.setStatut(statut);
        m.setDateDerniereLivraisonEnProduction(dateLivraison);
        return m;
    }

    private void verifySavedModule(int moduleId, int appId, BigDecimal value) {
        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(moduleId),
                        eq(appId),
                        eq(IndicateurType.NBR_JOUR_MEP),
                        eq(value),
                        eq(SourceType.OSCAR));
    }

    private void verifySavedApplication(int appId, BigDecimal value) {
        verify(saveTFByIndicator)
                .saveByIndicator(
                        isNull(),
                        eq(appId),
                        eq(IndicateurType.NBR_JOUR_MEP),
                        eq(value),
                        eq(SourceType.OSCAR));
    }

    // ─── setUp ──────────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.now().minusDays(7);
        endDate = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Cas limites sur la liste de modules
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    void shouldDoNothingWhenModulesNull() {
        updateDevopsNbrMep.updateDevops(startDate, endDate, null, Map.of());

        verifyNoInteractions(saveTFByIndicator);
    }

    @Test
    void shouldDoNothingWhenModulesEmpty() {
        updateDevopsNbrMep.updateDevops(startDate, endDate, Collections.emptyList(), Map.of());

        verifyNoInteractions(saveTFByIndicator);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getValue — branches métier
    // ═══════════════════════════════════════════════════════════════════════════

    /** sourceCreation == SAISIE_MANUELLE → SO. */
    @Test
    void shouldSaveSOWhenSourceCreationIsSaisieManuelle() {
        Module mod = buildModule(1, 100, DevopsConstantes.SAISIE_MANUELLE, null, null);

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(mod), Map.of());

        verifySavedModule(1, 100, BigDecimal.valueOf(IndicatorSpecialValue.SO.getCode()));
    }

    /** statut == EN_DEVELOPPEMENT → SO (même si dateLivraison présente). */
    @Test
    void shouldSaveSOWhenStatutIsEnDeveloppement() {
        Module mod =
                buildModule(
                        1,
                        100,
                        null,
                        DevopsConstantes.EN_DEVELOPPEMENT,
                        LocalDate.now().minusDays(10));

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(mod), Map.of());

        verifySavedModule(1, 100, BigDecimal.valueOf(IndicatorSpecialValue.SO.getCode()));
    }

    /** dateLivraison == null → NR. */
    @Test
    void shouldSaveNRWhenDateLivraisonIsNull() {
        Module mod = buildModule(1, 100, null, null, null);

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(mod), Map.of());

        verifySavedModule(1, 100, BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode()));
    }

    /** dateLivraison présente → nombre de jours depuis cette date jusqu'à aujourd'hui. */
    @Test
    void shouldSaveNbrJoursSinceDateLivraison() {
        LocalDate dateLivraison = LocalDate.now().minusDays(30);
        Module mod = buildModule(1, 100, null, null, dateLivraison);

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(mod), Map.of());

        verifySavedModule(1, 100, BigDecimal.valueOf(30));
    }

    /** dateLivraison = aujourd'hui → 0 jours. */
    @Test
    void shouldSaveZeroWhenDateLivraisonIsToday() {
        Module mod = buildModule(1, 100, null, null, LocalDate.now());

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(mod), Map.of());

        verifySavedModule(1, 100, BigDecimal.valueOf(0));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Agrégation par application (calculateRoundedAverage)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Un seul module → la moyenne est égale à sa valeur. */
    @Test
    void shouldSaveModuleValueAsAverageWhenSingleModule() {
        Module mod = buildModule(1, 100, null, null, LocalDate.now().minusDays(10));

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(mod), Map.of());

        verifySavedModule(1, 100, BigDecimal.valueOf(10));
        verifySavedApplication(100, BigDecimal.valueOf(10));
    }

    /** Deux modules même application → moyenne arrondie. */
    @Test
    void shouldSaveRoundedAverageForApplication() {
        // mod1 = 10 jours, mod2 = 20 jours → moyenne = 15
        Module mod1 = buildModule(1, 200, null, null, LocalDate.now().minusDays(10));
        Module mod2 = buildModule(2, 200, null, null, LocalDate.now().minusDays(20));

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(mod1, mod2), Map.of());

        verifySavedModule(1, 200, BigDecimal.valueOf(10));
        verifySavedModule(2, 200, BigDecimal.valueOf(20));
        verifySavedApplication(200, BigDecimal.valueOf(15));
    }

    /** Deux applications distinctes → une moyenne par application. */
    @Test
    void shouldSaveOneAveragePerApplication() {
        Module mod1 = buildModule(1, 300, null, null, LocalDate.now().minusDays(5));
        Module mod2 = buildModule(2, 400, null, null, LocalDate.now().minusDays(8));

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(mod1, mod2), Map.of());

        verifySavedApplication(300, BigDecimal.valueOf(5));
        verifySavedApplication(400, BigDecimal.valueOf(8));
    }

    /**
     * Tous les modules d'une application ont SO → calculateRoundedAverage([SO, SO]). allNRorSO &&
     * containsSO → retourne SO.
     */
    @Test
    void shouldSaveSOForApplicationWhenAllModulesAreSO() {
        Module mod1 = buildModule(1, 500, DevopsConstantes.SAISIE_MANUELLE, null, null);
        Module mod2 = buildModule(2, 500, null, DevopsConstantes.EN_DEVELOPPEMENT, null);

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(mod1, mod2), Map.of());

        verifySavedApplication(500, BigDecimal.valueOf(IndicatorSpecialValue.SO.getCode()));
    }

    /**
     * Tous les modules d'une application ont NR → calculateRoundedAverage([NR, NR]). allNR →
     * retourne NR.
     */
    @Test
    void shouldSaveNRForApplicationWhenAllModulesAreNR() {
        Module mod1 = buildModule(1, 600, null, null, null);
        Module mod2 = buildModule(2, 600, null, null, null);

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(mod1, mod2), Map.of());

        verifySavedApplication(600, BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode()));
    }

    /**
     * Mélange : un module SO et un module avec valeur réelle → SO ignoré dans la moyenne, seule la
     * valeur réelle compte.
     */
    @Test
    void shouldIgnoreSOWhenComputingAverageIfRealValuePresent() {
        // mod1 = SO, mod2 = 10 jours → filtered = [10] → moyenne = 10
        Module mod1 = buildModule(1, 700, DevopsConstantes.SAISIE_MANUELLE, null, null);
        Module mod2 = buildModule(2, 700, null, null, LocalDate.now().minusDays(10));

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(mod1, mod2), Map.of());

        verifySavedApplication(700, BigDecimal.valueOf(10));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Résilience
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Exception dans getValue (via exception dans saveModule) → notifyObservers, les autres modules
     * continuent.
     */
    @Test
    void shouldContinueProcessingOtherModulesAfterException() {
        Module modFail = buildModule(1, 100, null, null, LocalDate.now().minusDays(5));
        Module modOk = buildModule(2, 100, null, null, LocalDate.now().minusDays(3));

        doThrow(new RuntimeException("DB error"))
                .when(saveTFByIndicator)
                .saveByIndicator(eq(1), eq(100), any(), any(), any());

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(modFail, modOk), Map.of());

        verifySavedModule(2, 100, BigDecimal.valueOf(3));
        verify(eventObserverManager, atLeastOnce())
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
    }

    /** Exception dans saveModule → le module n'est pas ajouté à valuesByApp. */
    @Test
    void shouldNotAddToValuesByAppWhenSaveModuleFails() {
        Module mod = buildModule(1, 100, null, null, LocalDate.now().minusDays(5));

        doThrow(new RuntimeException("DB error"))
                .when(saveTFByIndicator)
                .saveByIndicator(eq(1), eq(100), any(), any(), any());

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(mod), Map.of());

        // Aucune sauvegarde pour l'application car valuesByApp est vide
        verify(saveTFByIndicator, never()).saveByIndicator(isNull(), anyInt(), any(), any(), any());
        verify(eventObserverManager, atLeastOnce())
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
    }

    /** Exception dans saveApplication → notifyObservers. */
    @Test
    void shouldNotifyObserverWhenSaveApplicationThrows() {
        Module mod = buildModule(1, 100, null, null, LocalDate.now().minusDays(5));

        doThrow(new RuntimeException("Contrainte DB"))
                .when(saveTFByIndicator)
                .saveByIndicator(isNull(), eq(100), any(), any(), any());

        updateDevopsNbrMep.updateDevops(startDate, endDate, List.of(mod), Map.of());

        verify(eventObserverManager, atLeastOnce())
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Méthodes publiques — appels directs
    // ═══════════════════════════════════════════════════════════════════════════

    /** saveModule alimente valuesByApp quand la sauvegarde réussit. */
    @Test
    void shouldPopulateValuesByAppWhenSaveModuleSucceeds() {
        Module mod = buildModule();
        Map<Integer, List<Integer>> valuesByApp = new HashMap<>();

        updateDevopsNbrMep.saveModule(mod, 15, valuesByApp);

        verifySavedModule(1, 100, BigDecimal.valueOf(15));
        assert valuesByApp.get(100).contains(15);
    }

    /** saveModule n'alimente pas valuesByApp quand la sauvegarde échoue. */
    @Test
    void shouldNotPopulateValuesByAppWhenSaveModuleFails() {
        Module mod = buildModule();
        Map<Integer, List<Integer>> valuesByApp = new HashMap<>();

        doThrow(new RuntimeException("erreur"))
                .when(saveTFByIndicator)
                .saveByIndicator(any(), any(), any(), any(), any());

        updateDevopsNbrMep.saveModule(mod, 15, valuesByApp);

        assert valuesByApp.isEmpty();
        verify(eventObserverManager)
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
    }

    /** saveApplication avec map vide → aucune sauvegarde. */
    @Test
    void shouldDoNothingWhenValuesByAppIsEmpty() {
        updateDevopsNbrMep.saveApplication(Map.of());

        verifyNoInteractions(saveTFByIndicator);
    }
}
