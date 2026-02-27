package fr.insee.compas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.insee.compas.repository.ApplicationTipsRepository;
import fr.insee.compas.repository.MaturiteCloudRepository;
import fr.insee.compas.service.maturitecloud.ApplicationTipsService;
import fr.insee.compas.service.maturitecloud.MaturiteCloudCsvService;
import fr.insee.compas.service.maturitecloud.indicateur.CloudCreationService;
import fr.insee.compas.service.maturitecloud.indicateur.MaturiteIndicateurService;

@WebMvcTest(MaturiteCloudController.class)
@Import(NoSecurityConfig.class)
class MaturiteCloudControllerStrategieTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private CloudCreationService cloudCreationService;

    @MockitoBean private MaturiteCloudRepository repo;

    @MockitoBean private MaturiteCloudCsvService service;

    @MockitoBean private ApplicationTipsRepository tipsRepo;

    @MockitoBean private ApplicationTipsService tipsCsvService;

    @MockitoBean private MaturiteIndicateurService maturiteIndicateurService;

    @Test
    void saisirStrategieCloud_retourne_200_avec_ids() throws Exception {

        when(cloudCreationService.creerStrategieCloud(any())).thenReturn(List.of(1L, 2L, 3L, 4L));

        String body =
                """
                {
                  "idsModule": [10, 20],
                  "avancement": 2,
                  "envCibleProd": 1,
                  "commentaire": "Test",
                  "date": "2026-02-25"
                }
                """;

        mockMvc.perform(
                        post("/cloud/strategie")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[1]").value(2))
                .andExpect(jsonPath("$[2]").value(3))
                .andExpect(jsonPath("$[3]").value(4));
    }

    @Test
    void saisirStrategieCloud_retourne_400_si_json_invalide() throws Exception {

        String body = "{ json invalide }";

        mockMvc.perform(
                        post("/cloud/strategie")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isBadRequest());

        verify(cloudCreationService, never()).creerStrategieCloud(any());
    }
}
