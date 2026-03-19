package fr.insee.compas.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.insee.compas.service.devops.IndicatorDevopsApplicationService;
import fr.insee.compas.service.devops.IndicatorDevopsModuleService;
import fr.insee.compas.service.devops.UpdateIndicatorDevopsService;
import fr.insee.compas.view.IndicateurDevopsView;

@WebMvcTest(DevopsController.class)
@Import(NoSecurityConfig.class)
class DevopsControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private UpdateIndicatorDevopsService updateIndicatorDevopsService;

    @MockitoBean private IndicatorDevopsApplicationService indicatorDevopsApplicationService;

    @MockitoBean private IndicatorDevopsModuleService indicatorDevopsModuleService;

    @Test
    void updateIndicateursDevops_shouldCallServiceWithGivenDates() throws Exception {
        // Given
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 31, 23, 59);

        // When
        mockMvc.perform(
                        put("/devops/indicateurs-devops")
                                .param("startDate", start.toString())
                                .param("endDate", end.toString()))
                .andExpect(status().isOk());

        // Then
        verify(updateIndicatorDevopsService).miseAJourIndicateursDevopsEnBaseDeDonnes(start, end);
    }

    @Test
    void getApplications_shouldReturnListFromService() throws Exception {
        // Given
        List<IndicateurDevopsView> expected =
                List.of(
                        new IndicateurDevopsView(
                                1, 100, "MOD1", "APP1", null, null, null, "1", "2", "3", "A", "B",
                                "C", null, false));

        when(indicatorDevopsApplicationService.getIndicateurNiveauApplication(false))
                .thenReturn(expected);

        // When / Then
        mockMvc.perform(get("/devops/applications").param("archive", "false"))
                .andExpect(status().isOk());
    }

    @Test
    void getModules_shouldReturnListFromService() throws Exception {
        // Given
        when(indicatorDevopsModuleService.getIndicateurNiveauModule(false)).thenReturn(List.of());

        // When / Then
        mockMvc.perform(get("/devops/modules").param("archive", "false"))
                .andExpect(status().isOk());
    }
}
