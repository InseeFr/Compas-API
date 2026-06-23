package fr.insee.compas.controller;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.insee.compas.service.devops.IndicatorDevopsApplicationService;
import fr.insee.compas.service.devops.IndicatorDevopsModuleService;
import fr.insee.compas.service.devops.update.UpdateIndicatorDevopsService;
import fr.insee.compas.view.IndicateurDevopsView;

@WebMvcTest(DevopsController.class)
@Import(NoSecurityConfig.class)
class DevopsControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private UpdateIndicatorDevopsService updateIndicatorDevopsService;

    @MockitoBean private IndicatorDevopsModuleService indicatorDevopsModuleService;

    @MockitoBean private IndicatorDevopsApplicationService indicatorDevopsApplicationService;

    @Test
    @DisplayName("PUT /indicateurs-devops sans paramètres")
    void shouldUpdateIndicatorsWithoutDates() throws Exception {

        mockMvc.perform(put("/devops/indicateurs-devops")).andExpect(status().isOk());

        verify(updateIndicatorDevopsService)
                .miseAJourIndicateursDevopsEnBaseDeDonnes(isNull(), isNull());
    }

    @Test
    @DisplayName("PUT /indicateurs-devops avec dates")
    void shouldUpdateIndicatorsWithDates() throws Exception {

        String startDate = "2025-01-01T00:00:00";
        String endDate = "2025-01-31T23:59:59";

        mockMvc.perform(
                        put("/devops/indicateurs-devops")
                                .param("startDate", startDate)
                                .param("endDate", endDate))
                .andExpect(status().isOk());

        verify(updateIndicatorDevopsService)
                .miseAJourIndicateursDevopsEnBaseDeDonnes(
                        eq(LocalDateTime.parse(startDate)), eq(LocalDateTime.parse(endDate)));
    }

    @Test
    @DisplayName("GET /applications")
    void shouldReturnApplications() throws Exception {

        IndicateurDevopsView view =
                IndicateurDevopsView.builder()
                        .applicationId(1)
                        .applicationName("COMPAS")
                        .lettreGlobalDevops("A")
                        .build();

        when(indicatorDevopsApplicationService.getIndicateurNiveauApplication(
                        ArgumentMatchers.any(), ArgumentMatchers.any(), eq(false)))
                .thenReturn(List.of(view));

        mockMvc.perform(get("/devops/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(1))
                .andExpect(jsonPath("$[0].applicationName").value("COMPAS"))
                .andExpect(jsonPath("$[0].lettreGlobalDevops").value("A"));

        verify(indicatorDevopsApplicationService)
                .getIndicateurNiveauApplication(
                        ArgumentMatchers.any(), ArgumentMatchers.any(), eq(false));
    }

    @Test
    @DisplayName("GET /applications en mode synthétique")
    void shouldReturnApplicationsInSyntheticMode() throws Exception {

        when(indicatorDevopsApplicationService.getIndicateurNiveauApplication(
                        ArgumentMatchers.any(), ArgumentMatchers.any(), eq(true)))
                .thenReturn(List.of());

        mockMvc.perform(get("/devops/applications").param("isSynthetique", "true"))
                .andExpect(status().isOk());

        verify(indicatorDevopsApplicationService)
                .getIndicateurNiveauApplication(
                        ArgumentMatchers.any(), ArgumentMatchers.any(), eq(true));
    }

    @Test
    @DisplayName("GET /modules")
    void shouldReturnModules() throws Exception {

        IndicateurDevopsView view =
                IndicateurDevopsView.builder()
                        .moduleId(10)
                        .moduleName("Module A")
                        .lettreGlobalDevops("B")
                        .build();

        when(indicatorDevopsModuleService.getIndicateurNiveauModule(
                        ArgumentMatchers.any(), ArgumentMatchers.any(), eq(false)))
                .thenReturn(List.of(view));

        mockMvc.perform(get("/devops/modules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].moduleId").value(10))
                .andExpect(jsonPath("$[0].moduleName").value("Module A"))
                .andExpect(jsonPath("$[0].lettreGlobalDevops").value("B"));

        verify(indicatorDevopsModuleService)
                .getIndicateurNiveauModule(
                        ArgumentMatchers.any(), ArgumentMatchers.any(), eq(false));
    }

    @Test
    @DisplayName("GET /modules en mode synthétique")
    void shouldReturnModulesInSyntheticMode() throws Exception {

        when(indicatorDevopsModuleService.getIndicateurNiveauModule(
                        ArgumentMatchers.any(), ArgumentMatchers.any(), eq(true)))
                .thenReturn(List.of());

        mockMvc.perform(get("/devops/modules").param("isSynthetique", "true"))
                .andExpect(status().isOk());

        verify(indicatorDevopsModuleService)
                .getIndicateurNiveauModule(
                        ArgumentMatchers.any(), ArgumentMatchers.any(), eq(true));
    }
}
