package fr.insee.compas.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.insee.compas.service.securite.CveCriticalMonthlyService;
import fr.insee.compas.service.securite.IndicateurSecuriteService;
import fr.insee.compas.service.securite.RecupCveSecuriteService;
import fr.insee.compas.view.IndicateurApplicationSecuriteMonthly;
import fr.insee.compas.view.IndicateurSecuriteApplicationView;
import fr.insee.compas.view.IndicateurSecuriteModuleView;

@WebMvcTest(SecuriteController.class)
@AutoConfigureMockMvc(addFilters = false)
class SecuriteControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean RecupCveSecuriteService cveSecuriteService;

    @MockitoBean CveCriticalMonthlyService cveCriticalMonthlyService;

    @MockitoBean IndicateurSecuriteService indicateurSecuriteService;

    @Test
    @DisplayName("PUT /securite/indicateurs-cve triggers service.recupereCve() and returns 200")
    void updateIndicateurCve_ok() throws Exception {
        mockMvc.perform(put("/securite/indicateurs-cve"))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // void endpoint => empty body
        verify(cveSecuriteService).recupereCve();
    }

    @Test
    @DisplayName("GET /securite/modules returns JSON array (empty list case)")
    void getIndicateurQualiteByModule_ok() throws Exception {
        given(indicateurSecuriteService.getIndicateursModuleView())
                .willReturn(Collections.<IndicateurSecuriteModuleView>emptyList());

        mockMvc.perform(get("/securite/modules"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));

        verify(indicateurSecuriteService).getIndicateursModuleView();
    }

    @Test
    @DisplayName("GET /securite/applications returns JSON array (empty list case)")
    void getIndicateurQualiteByApplication_ok() throws Exception {
        given(indicateurSecuriteService.getIndicateursApplicationView())
                .willReturn(Collections.<IndicateurSecuriteApplicationView>emptyList());

        mockMvc.perform(get("/securite/applications"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));

        verify(indicateurSecuriteService).getIndicateursApplicationView();
    }

    @Test
    @DisplayName(
            "GET /securite/applications/cve-critical/monthly returns JSON array (empty list case)")
    void getCveCriticalMonthly_ok() throws Exception {
        given(cveCriticalMonthlyService.getMonthly())
                .willReturn(List.<IndicateurApplicationSecuriteMonthly>of());

        mockMvc.perform(get("/securite/applications/cve-critical/monthly"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));

        verify(cveCriticalMonthlyService).getMonthly();
    }
}
