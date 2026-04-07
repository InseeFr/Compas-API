package fr.insee.compas.service.devops;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.devops.update.UpdateIndicatorDevopsService;
import fr.insee.compas.service.devops.update.strat.IUpdateDevopsStrategy;
import fr.insee.compas.util.DevopsConstantes;

class UpdateIndicatorDevopsServiceTest {

    @Mock private IUpdateDevopsStrategy updateCountDeploy;

    @Mock private IUpdateDevopsStrategy updateNbrMep;

    @Mock private IUpdateDevopsStrategy updateContributorCount;

    @Mock private OscarService oscarService;

    private UpdateIndicatorDevopsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service =
                new UpdateIndicatorDevopsService(
                        updateCountDeploy, updateNbrMep, updateContributorCount, oscarService);
    }

    // -------------------------------------------------------------------------
    // miseAJourIndicateursDevopsEnBaseDeDonnes — orchestration
    // -------------------------------------------------------------------------

    @Test
    void testMiseAJourIndicateurs_callsAllThreeStrategies() {
        // GIVEN
        Module module = mock(Module.class);
        when(module.getId()).thenReturn(1);
        when(module.getIdApplication()).thenReturn(100);
        when(module.getStatut()).thenReturn("EN_PRODUCTION");
        when(module.getDateDerniereLivraisonEnProduction())
                .thenReturn(LocalDate.now().minusDays(5));
        when(module.getUrlCodeSource()).thenReturn("https://gitlab.insee.fr/project/repo");

        Map<String, List<ModuleHistorique>> historiqueMap = new HashMap<>();
        when(oscarService.getModules()).thenReturn(List.of(module));
        when(oscarService.getModulesHistorique()).thenReturn(historiqueMap);

        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now();

        // WHEN
        service.miseAJourIndicateursDevopsEnBaseDeDonnes(start, end);

        // THEN — each strategy must be called exactly once with the right arguments
        verify(updateNbrMep, times(1)).updateDevops(null, null, List.of(module), null);

        verify(updateCountDeploy, times(1))
                .updateDevops(start, end, List.of(module), historiqueMap);

        verify(updateContributorCount, times(1)).updateDevops(start, end, List.of(module), null);
    }

    @Test
    void testMiseAJourIndicateurs_withEmptyModuleList_doesNotThrow() {
        // GIVEN
        when(oscarService.getModules()).thenReturn(List.of());
        when(oscarService.getModulesHistorique()).thenReturn(new HashMap<>());

        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now();

        // WHEN / THEN — no exception expected
        service.miseAJourIndicateursDevopsEnBaseDeDonnes(start, end);

        verify(updateNbrMep).updateDevops(null, null, List.of(), null);
        verify(updateCountDeploy).updateDevops(start, end, List.of(), new HashMap<>());
        verify(updateContributorCount).updateDevops(start, end, List.of(), null);
    }

    @Test
    void testMiseAJourIndicateurs_withNullDates_passesNullsToStrategiesThatExpectThem() {
        // GIVEN
        when(oscarService.getModules()).thenReturn(List.of());
        when(oscarService.getModulesHistorique()).thenReturn(new HashMap<>());

        // WHEN
        service.miseAJourIndicateursDevopsEnBaseDeDonnes(null, null);

        // THEN — the service should forward whatever it receives without crashing
        verify(updateCountDeploy).updateDevops(null, null, List.of(), new HashMap<>());
        verify(updateContributorCount).updateDevops(null, null, List.of(), null);
    }

    @Test
    void testMiseAJourIndicateurs_nbrMepAlwaysReceivesNullDates() {
        // GIVEN — dates are provided, but NBR_MEP must still receive (null, null)
        when(oscarService.getModules()).thenReturn(List.of());
        when(oscarService.getModulesHistorique()).thenReturn(new HashMap<>());

        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        // WHEN
        service.miseAJourIndicateursDevopsEnBaseDeDonnes(start, end);

        // THEN
        verify(updateNbrMep).updateDevops(null, null, List.of(), null);
        // and the other two receive actual dates
        verify(updateCountDeploy).updateDevops(start, end, List.of(), new HashMap<>());
        verify(updateContributorCount).updateDevops(start, end, List.of(), null);
    }

    @Test
    void testMiseAJourIndicateurs_moduleHistoriqueIsForwardedOnlyToCountDeploy() {
        // GIVEN
        Module module = mock(Module.class);
        when(module.getId()).thenReturn(42);

        ModuleHistorique hist = new ModuleHistorique();
        hist.setAuteurOperation(DevopsConstantes.SERVICE);
        hist.setDateOperation(LocalDateTime.now().minusDays(3));
        hist.setOperation(DevopsConstantes.MODIFICATION);

        Map<String, List<ModuleHistorique>> historiqueMap = new HashMap<>();
        historiqueMap.put("42", List.of(hist));

        when(oscarService.getModules()).thenReturn(List.of(module));
        when(oscarService.getModulesHistorique()).thenReturn(historiqueMap);

        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now();

        // WHEN
        service.miseAJourIndicateursDevopsEnBaseDeDonnes(start, end);

        // THEN — historique passed to countDeploy, null to the others
        verify(updateCountDeploy).updateDevops(start, end, List.of(module), historiqueMap);
        verify(updateNbrMep).updateDevops(null, null, List.of(module), null);
        verify(updateContributorCount).updateDevops(start, end, List.of(module), null);
    }
}
