package fr.insee.compas.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import fr.insee.compas.exception.CompasUploadException;
import fr.insee.compas.mapper.IndicateurApplicationGreenITViewMapper;
import fr.insee.compas.mapper.IndicateurModuleGreenITViewMapper;
import fr.insee.compas.model.compas.dto.MetriqueApplicationDTO;
import fr.insee.compas.model.compas.dto.MetriqueModuleDTO;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.service.FichierControlService;
import fr.insee.compas.service.greenit.GreenItService;
import fr.insee.compas.view.IndicateurApplicationGreenITView;
import fr.insee.compas.view.IndicateurModuleGreenITView;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(GreenItController.class)
@Import(NoSecurityConfig.class)
class GreenItControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private FichierControlService fichierControlService;

    @MockitoBean private GreenItService greenItService;

    @MockitoBean private IndicateurModuleGreenITViewMapper indicateurModuleGreenITViewMapper;

    @MockitoBean
    private IndicateurApplicationGreenITViewMapper indicateurApplicationGreenITViewMapper;

    @Autowired private WebApplicationContext wac;

    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void printRoutes() {
        final RequestMappingHandlerMapping mappings =
                wac.getBean(RequestMappingHandlerMapping.class);
        mappings.getHandlerMethods()
                .forEach((key, value) -> System.out.println(key + " => " + value));
    }

    @Test
    void testUploadCSV_WithInvalidFileName_ReturnsBadRequest() throws Exception {
        // Given
        final MockMultipartFile mockFile =
                new MockMultipartFile("file", "badname.csv", "text/csv", "data".getBytes());

        when(fichierControlService.controlVmFileName("badname.csv"))
                .thenThrow(new CompasUploadException(422, null));

        // When / Then
        mockMvc.perform(
                        multipart("/kpi-green/modules/upload")
                                .file(mockFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is(HttpStatusCode.valueOf(422).value()));

        verify(greenItService, never()).miseAJourVmMetricsGreenItFromFile(any(), any());
    }

    @Test
    void testUploadKubeCSV_WithInvalidFileName_ReturnsBadRequest() throws Exception {
        // Given
        final MultipartFile mockFile =
                new MockMultipartFile("file", "badname.csv", "text/csv", "data".getBytes());

        when(fichierControlService.controlKubeFileName("badname.csv"))
                .thenThrow(new CompasUploadException(422, null));

        // When / Then
        mockMvc.perform(
                        multipart("/kpi-green/modules/upload/kube")
                                .file((MockMultipartFile) mockFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is(HttpStatusCode.valueOf(422).value()));

        verify(greenItService, never()).miseAJourKubeMetricsGreenItFromFile(any(), any());
    }

    @Test
    void getIndicateursApplicationGreenIT_shouldReturnOk_whenViewIsPresent() throws Exception {
        final Integer appId = 1;

        final IndicateurApplicationGreenIT kpi = new IndicateurApplicationGreenIT();
        kpi.setApplicationId(appId);
        final IndicateurApplicationGreenITView view = new IndicateurApplicationGreenITView();

        when(greenItService.getIndicateursApplicationGreenIT(appId)).thenReturn(kpi);
        when(indicateurApplicationGreenITViewMapper.toView(kpi)).thenReturn(Optional.of(view));

        mockMvc.perform(
                        get("/kpi-green/applications/{applicationId}", appId)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getIndicateursApplicationGreenIT_shouldReturnNotFound_whenViewIsEmpty() throws Exception {
        final Integer appId = 1;

        final IndicateurApplicationGreenIT kpi = new IndicateurApplicationGreenIT();

        when(greenItService.getIndicateursApplicationGreenIT(appId)).thenReturn(kpi);
        when(indicateurApplicationGreenITViewMapper.toView(kpi)).thenReturn(Optional.empty());

        mockMvc.perform(get("/kpi-green/applications/{applicationId}", appId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getIndicateursGreenIT_shouldReturnOk_whenViewIsPresent() throws Exception {
        final Integer moduleId = 1;

        final IndicateurModuleGreenIT kpi = new IndicateurModuleGreenIT();
        kpi.setModuleId(moduleId);
        final IndicateurModuleGreenITView view = new IndicateurModuleGreenITView();

        when(greenItService.getIndicateursModuleGreenIT(moduleId)).thenReturn(kpi);
        when(indicateurModuleGreenITViewMapper.toView(kpi)).thenReturn(Optional.of(view));

        mockMvc.perform(
                        get("/kpi-green/modules/{moduleId}", moduleId)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getIndicateursGreenIT_shouldReturnNotFound_whenViewIsEmpty() throws Exception {
        final Integer moduleId = 1;

        final IndicateurModuleGreenIT kpi = new IndicateurModuleGreenIT();

        when(greenItService.getIndicateursModuleGreenIT(moduleId)).thenReturn(kpi);
        when(indicateurModuleGreenITViewMapper.toView(kpi)).thenReturn(Optional.empty());

        mockMvc.perform(get("/kpi-green/modules/{moduleId}", moduleId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getModules_shouldReturnListOfViews() throws Exception {
        // --- Arrange
        final MetriqueModuleDTO dto1 = new MetriqueModuleDTO();
        dto1.setIdModule(1);
        dto1.setDate(LocalDate.of(2024, 1, 1));

        final IndicateurModuleGreenIT kpi = new IndicateurModuleGreenIT();
        final IndicateurModuleGreenITView view = new IndicateurModuleGreenITView();
        view.setImpactScore("0,911");
        // Mocks
        when(greenItService.getModuleMetriques()).thenReturn(List.of(dto1));
        when(greenItService.getIndicateursModuleGreenIT(dto1.getIdModule(), dto1.getDate()))
                .thenReturn(kpi);
        when(indicateurModuleGreenITViewMapper.toView(kpi)).thenReturn(Optional.of(view));

        final MvcResult mvcResult =
                mockMvc.perform(get("/kpi-green/modules").accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();
        final String json = mvcResult.getResponse().getContentAsString();
        assertThat(json).isEqualTo(objectMapper.writeValueAsString(List.of(view)));
    }

    @Test
    void getModules_shouldReturnEmptyList_whenNoViewPresent() throws Exception {
        final MetriqueModuleDTO dto = new MetriqueModuleDTO();
        dto.setIdModule(1);
        dto.setDate(LocalDate.of(2024, 1, 1));

        final IndicateurModuleGreenIT indicateur = new IndicateurModuleGreenIT();
        when(greenItService.getIndicateursModuleGreenIT(dto.getIdModule(), dto.getDate()))
                .thenReturn(indicateur);
        when(indicateurModuleGreenITViewMapper.toView(indicateur)).thenReturn(Optional.empty());

        final MvcResult mvcResult =
                mockMvc.perform(get("/kpi-green/modules").accept(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();
        final String json = mvcResult.getResponse().getContentAsString();
        assertThat(json).isEqualTo("[]");
    }

    @Test
    void getApplications_shouldReturnListOfViews() throws Exception {
        // --- Arrange
        final MetriqueApplicationDTO dto1 = new MetriqueApplicationDTO();
        dto1.setIdApplication(1);
        dto1.setDate(LocalDate.of(2024, 1, 1));

        final IndicateurApplicationGreenIT kpi = new IndicateurApplicationGreenIT();
        kpi.setApplicationId(1);
        final IndicateurApplicationGreenITView view = new IndicateurApplicationGreenITView();
        view.setImpactScore("0,911");
        when(greenItService.getApplicationMetriques()).thenReturn(List.of(dto1));
        when(greenItService.getIndicateursApplicationGreenIT(
                        dto1.getIdApplication(), dto1.getDate()))
                .thenReturn(kpi);
        when(greenItService.getIndicateursApplicationGreenIT(dto1.getIdApplication()))
                .thenReturn(kpi);
        when(indicateurApplicationGreenITViewMapper.toView(kpi)).thenReturn(Optional.of(view));

        final MvcResult mvcResult =
                mockMvc.perform(get("/kpi-green/applications").accept(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();
        System.out.println("mcv" + mvcResult);
        final String json = mvcResult.getResponse().getContentAsString();
        assertThat(json).isEqualTo(objectMapper.writeValueAsString(List.of(view)));
    }

    @Test
    void getApplications_shouldReturnEmptyList_whenNoViewPresent() throws Exception {
        final MetriqueApplicationDTO dto = new MetriqueApplicationDTO();
        dto.setIdApplication(1);
        dto.setDate(LocalDate.of(2024, 1, 1));

        final IndicateurApplicationGreenIT indicateur = new IndicateurApplicationGreenIT();
        when(greenItService.getIndicateursApplicationGreenIT(dto.getIdApplication(), dto.getDate()))
                .thenReturn(indicateur);
        when(indicateurApplicationGreenITViewMapper.toView(indicateur))
                .thenReturn(Optional.empty());

        final MvcResult mvcResult =
                mockMvc.perform(get("/kpi-green/applications").accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();
        final String json = mvcResult.getResponse().getContentAsString();
        assertThat(json).isEqualTo("[]");
    }
}
