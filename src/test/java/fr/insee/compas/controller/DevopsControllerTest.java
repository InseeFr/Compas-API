package fr.insee.compas.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import fr.insee.compas.service.devops.IndicatorDevopsApplicationService;
import fr.insee.compas.service.devops.IndicatorDevopsModuleService;
import fr.insee.compas.service.devops.UpdateIndicatorDevopsService;
import fr.insee.compas.view.IndicateurDevopsView;

@WebMvcTest(DevopsControllerTest.class)
@Import(NoSecurityConfig.class)
class DevopsControllerTest {

    private UpdateIndicatorDevopsService updateIndicatorDevopsService;
    private IndicatorDevopsApplicationService indicatorDevopsApplicationService;
    private IndicatorDevopsModuleService indicatorDevopsModuleService;
    private DevopsController controller;

    @BeforeEach
    void setUp() {
        // Given : des services mockés
        updateIndicatorDevopsService = mock(UpdateIndicatorDevopsService.class);
        indicatorDevopsApplicationService = mock(IndicatorDevopsApplicationService.class);
        indicatorDevopsModuleService = mock(IndicatorDevopsModuleService.class);

        controller =
                new DevopsController(
                        updateIndicatorDevopsService,
                        indicatorDevopsModuleService,
                        indicatorDevopsApplicationService);
    }

    @Test
    void updateIndicateursDevops_shouldCallServiceWithGivenDates() {
        // Given
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 31, 23, 59);

        // When
        controller.updateIndicateursDevops(start, end);

        // Then
        verify(updateIndicatorDevopsService, times(1))
                .miseAJourIndicateursDevopsEnBaseDeDonnes(start, end);
    }

    @Test
    void getApplications_shouldReturnListFromService() {
        // Given
        List<IndicateurDevopsView> expected =
                List.of(
                        new IndicateurDevopsView(
                                1, 100, "MOD1", "APP1", null, null, null, "1", "2", "3", "A", "B",
                                "C", null, false),
                        new IndicateurDevopsView(
                                2, 101, "MOD2", "APP2", null, null, null, "4", "5", "6", "D", "E",
                                "X", null, false));
        when(indicatorDevopsApplicationService.getIndicateurNiveauApplication(false))
                .thenReturn(expected);

        // When
        List<IndicateurDevopsView> result = controller.getApplications(false);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(indicatorDevopsApplicationService, times(1)).getIndicateurNiveauApplication(false);
    }

    @Test
    void getModules_shouldReturnListFromService() {
        // Given
        List<IndicateurDevopsView> expected =
                List.of(
                        new IndicateurDevopsView(
                                1, 100, "MOD1", "APP1", null, null, null, "1", "2", "3", "A", "B",
                                "C", null, false),
                        new IndicateurDevopsView(
                                2, 101, "MOD2", "APP2", null, null, null, "4", "5", "6", "D", "E",
                                "X", null, false));
        when(indicatorDevopsModuleService.getIndicateurNiveauModule(false)).thenReturn(expected);

        // When
        List<IndicateurDevopsView> result = controller.getModules(false);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(indicatorDevopsModuleService, times(1)).getIndicateurNiveauModule(false);
    }
}
