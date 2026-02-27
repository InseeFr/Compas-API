package fr.insee.compas.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.insee.compas.model.compas.ApplicationTip;
import fr.insee.compas.repository.ApplicationTipsRepository;
import fr.insee.compas.repository.MaturiteCloudRepository;
import fr.insee.compas.service.maturitecloud.ApplicationTipsService;
import fr.insee.compas.service.maturitecloud.MaturiteCloudCsvService;
import fr.insee.compas.service.maturitecloud.indicateur.CloudCreationService;
import fr.insee.compas.service.maturitecloud.indicateur.MaturiteIndicateurService;
import fr.insee.compas.view.IndicateurMaturiteView;

// mock
@WebMvcTest(MaturiteCloudController.class)
@AutoConfigureMockMvc(addFilters = false)
class MaturiteCloudControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean MaturiteCloudRepository repo;

    @MockitoBean MaturiteCloudCsvService service;

    @MockitoBean CloudCreationService cloudCreationService;

    @MockitoBean MaturiteIndicateurService maturiteIndicateurService;

    @MockitoBean ApplicationTipsRepository tipsRepo;

    @MockitoBean ApplicationTipsService tipsCsvService;

    @Test
    @DisplayName(
            "GET /cloud/applications maps DB rows -> view objects with rounding and null handling")
    void getMaturiteCloud_ok() throws Exception {
        List<Object[]> rows = new ArrayList<>();
        // Row 1: all values present, including rounding HALF_UP (1.235 -> 1.24)
        rows.add(
                new Object[] {
                    42, // idApp
                    "Mature", // maturite
                    new BigDecimal("5.0"), // rob
                    new BigDecimal("1.235"), // benef -> 1.24
                    new BigDecimal("2.2"), // orga -> 2.20
                    new BigDecimal("3.999"), // complex -> 4.00
                    new BigDecimal("4.5"), // tech -> 4.50
                    new BigDecimal("10.0"), // progDep -> 10.00
                    new BigDecimal("20.1"), // progTechs -> 20.10
                    new BigDecimal("30.345"), // progArchi -> 30.35
                    new BigDecimal("40.0"), // progMatEq -> 40.00
                    new BigDecimal("50.555"), // progDev -> 50.56
                    new BigDecimal("60.666") // progCloud -> 60.67
                });
        // Row 2: null rob and some null scores to exercise null code paths
        rows.add(
                new Object[] {
                    7,
                    "En_Démarrage",
                    null, // rob -> null string
                    null, // benef -> null
                    new BigDecimal("0"), // orga -> 0.00
                    null, // complex -> null
                    new BigDecimal("4"), // tech -> 4.00
                    null, // progDep -> null
                    null, // progTechs -> null
                    null, // progArchi -> null
                    null, // progMatEq -> null
                    null, // progDev -> null
                    null // progCloud -> null
                });

        given(repo.findAllLatestMatRobAndScores()).willReturn(rows);

        mockMvc.perform(get("/cloud/applications"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                // First element assertions
                .andExpect(jsonPath("$[0].applicationId").value(42))
                .andExpect(jsonPath("$[0].maturite").value("Mature"))
                .andExpect(jsonPath("$[0].robustesse").value("5"))
                .andExpect(jsonPath("$[0].scoreBenefice").value("1.24"))
                .andExpect(jsonPath("$[0].scoreOrga").value("2.20"))
                .andExpect(jsonPath("$[0].scoreComplexite").value("4.00"))
                .andExpect(jsonPath("$[0].scoreTechnique").value("4.50"))
                .andExpect(jsonPath("$[0].progressionDeploy").value("10.00"))
                .andExpect(jsonPath("$[0].progressionTechnos").value("20.10"))
                .andExpect(jsonPath("$[0].progressionArchi").value("30.35"))
                .andExpect(jsonPath("$[0].progressionMateqip").value("40.00"))
                .andExpect(jsonPath("$[0].progressionDevops").value("50.56"))
                .andExpect(jsonPath("$[0].progressionCloud").value("60.67"))
                // Second element assertions (null handling)
                .andExpect(jsonPath("$[1].applicationId").value(7))
                .andExpect(jsonPath("$[1].maturite").value("En_Démarrage"))
                .andExpect(jsonPath("$[1].robustesse").doesNotExist())
                .andExpect(jsonPath("$[1].scoreBenefice").doesNotExist())
                .andExpect(jsonPath("$[1].scoreOrga").value("0.00"))
                .andExpect(jsonPath("$[1].scoreComplexite").doesNotExist())
                .andExpect(jsonPath("$[1].scoreTechnique").value("4.00"))
                .andExpect(jsonPath("$[1].progressionDeploy").doesNotExist())
                .andExpect(jsonPath("$[1].progressionTechnos").doesNotExist())
                .andExpect(jsonPath("$[1].progressionArchi").doesNotExist())
                .andExpect(jsonPath("$[1].progressionMateqip").doesNotExist())
                .andExpect(jsonPath("$[1].progressionDevops").doesNotExist())
                .andExpect(jsonPath("$[1].progressionCloud").doesNotExist());

        verify(repo).findAllLatestMatRobAndScores();
    }

    @Test
    @DisplayName("POST /cloud/maturite-cloud/import imports CSV and returns insertedCount")
    void importFaitsMaturiteCloud_ok() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "maturite.csv",
                        MediaType.TEXT_PLAIN_VALUE,
                        ("id;maturite\n42;Mature\n").getBytes());
        given(service.importCsv(any())).willReturn(3);

        mockMvc.perform(multipart("/cloud/maturite-cloud/import").file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.insertedCount").value(3));

        verify(service).importCsv(any());
    }

    @Test
    @DisplayName("GET /cloud/conseils returns tips for given nom_oscar (case-insensitive)")
    void getApplicationConseils_ok() throws Exception {
        // We don't need to craft a full ApplicationTip object for this test; returning an empty
        // list
        // is enough to execute the code path and assert the query parameter is used.
        given(tipsRepo.findAllByNomOscarIgnoreCaseOrderByDateDescIdDesc("MyApp"))
                .willReturn(Collections.<ApplicationTip>emptyList());

        mockMvc.perform(get("/cloud/conseils").param("nom_oscar", "MyApp"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(tipsRepo).findAllByNomOscarIgnoreCaseOrderByDateDescIdDesc("MyApp");
    }

    @Test
    @DisplayName("POST /cloud/conseils/import imports tips CSV with sourceId and echoes it back")
    void importApplicationTips_ok() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "tips.csv",
                        MediaType.TEXT_PLAIN_VALUE,
                        ("nom_oscar;tip\nMyApp;Do X\n").getBytes());

        given(tipsCsvService.importCsv(any(), eq(2))).willReturn(7);

        mockMvc.perform(multipart("/cloud/conseils/import").file(file).param("sourceId", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.insertedCount").value(7))
                .andExpect(jsonPath("$.sourceId").value(2));

        verify(tipsCsvService).importCsv(any(), eq(2));
    }

    @Test
    void getIndicateur_shouldReturn200_whenIndicateursFound() throws Exception {
        List<IndicateurMaturiteView> mockResult =
                List.of(
                        IndicateurMaturiteView.builder()
                                .idApp(1)
                                .appName("App Test")
                                .isModule(false)
                                .build());
        when(maturiteIndicateurService.getIndicateurMaturite()).thenReturn(mockResult);

        mockMvc.perform(get("/cloud/indicateur").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].appName").value("App Test"));
    }

    @Test
    void getIndicateur_shouldReturn204_whenNoIndicateursFound() throws Exception {
        when(maturiteIndicateurService.getIndicateurMaturite()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/cloud/indicateur").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getIndicateur_shouldReturn406_whenAcceptHeaderIsNotJson() throws Exception {
        mockMvc.perform(get("/cloud/indicateur").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void saisirStrategieCloud_shouldReturn415_whenContentTypeIsNotJson() throws Exception {
        mockMvc.perform(
                        post("/cloud/strategie")
                                .contentType(MediaType.APPLICATION_XML)
                                .content("<demande></demande>"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
