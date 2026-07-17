package fr.insee.compas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import fr.insee.compas.exception.CompasUploadException;
import fr.insee.compas.model.compas.Periode;
import fr.insee.compas.service.FichierControlService;
import fr.insee.compas.service.greenit.GreenItService;
import fr.insee.compas.util.TendanceUtils;
import fr.insee.compas.util.greenit.GreenITutils;
import fr.insee.compas.view.green.IndicateurAppGreenBaseView;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(GreenItController.class)
@Import(NoSecurityConfig.class)
class GreenItControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private FichierControlService fichierControlService;

    @MockitoBean private GreenItService greenItService;

    @Autowired private WebApplicationContext wac;

    @MockitoBean private TendanceUtils.GreenPeriodeBuilder greenPeriodeBuilder;

    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void printRoutes() {
        RequestMappingHandlerMapping mappings = wac.getBean(RequestMappingHandlerMapping.class);
        mappings.getHandlerMethods()
                .forEach((key, value) -> System.out.println(key + " => " + value));
    }

    @Test
    void testGetValidDates_ReturnsOk() throws Exception {
        // Given
        Set<LocalDate> dates = Set.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1));

        when(greenItService.getValidDates()).thenReturn(dates);

        // When / Then
        mockMvc.perform(get("/kpi-green/valid-dates")).andExpect(status().isOk());

        verify(greenItService).getValidDates();
    }

    @Test
    void testGetApplications_ReturnsOk() throws Exception {
        // Given
        Periode periode =
                new Periode(
                        Date.valueOf(LocalDate.of(2024, 2, 1)),
                        Date.valueOf(LocalDate.of(2024, 1, 1)));

        when(greenPeriodeBuilder.buildPeriodeGreen("2024-02-01", "2024-01-01")).thenReturn(periode);
        when(greenItService.getIndicateursApplicationGreenIT(
                        GreenITutils.ViewGreen.KUBE, periode.origine(), periode.passee()))
                .thenReturn(List.<IndicateurAppGreenBaseView>of());

        // When / Then
        mockMvc.perform(
                        get("/kpi-green/applications/KUBE")
                                .param("origine", "2024-02-01")
                                .param("passee", "2024-01-01"))
                .andExpect(status().isOk());

        verify(greenPeriodeBuilder).buildPeriodeGreen("2024-02-01", "2024-01-01");

        verify(greenItService)
                .getIndicateursApplicationGreenIT(
                        GreenITutils.ViewGreen.KUBE, periode.origine(), periode.passee());
    }

    @Test
    void testUploadCSV_ReturnsOk() throws Exception {
        // Given
        MockMultipartFile mockFile =
                new MockMultipartFile("file", "vm_20240101.csv", "text/csv", "data".getBytes());

        LocalDate date = LocalDate.of(2024, 1, 1);

        when(fichierControlService.controlVmFileName("vm_20240101.csv")).thenReturn(date);

        doNothing().when(greenItService).miseAJourVmMetricsGreenItFromFile(mockFile, date);

        // When / Then
        mockMvc.perform(
                        multipart("/kpi-green/modules/upload")
                                .file(mockFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        verify(fichierControlService).controlVmFileName("vm_20240101.csv");
        verify(greenItService).miseAJourVmMetricsGreenItFromFile(mockFile, date);
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
    void testUploadKubeCSV_ReturnsOk() throws Exception {
        // Given
        MockMultipartFile mockFile =
                new MockMultipartFile("file", "kube_20240101.csv", "text/csv", "data".getBytes());

        LocalDate date = LocalDate.of(2024, 1, 1);

        when(fichierControlService.controlKubeFileName("kube_20240101.csv")).thenReturn(date);

        doNothing().when(greenItService).miseAJourKubeMetricsGreenItFromFile(mockFile, date);

        // When / Then
        mockMvc.perform(
                        multipart("/kpi-green/modules/upload/kube")
                                .file(mockFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        verify(fichierControlService).controlKubeFileName("kube_20240101.csv");
        verify(greenItService).miseAJourKubeMetricsGreenItFromFile(mockFile, date);
    }

    @Test
    void testUploadKubeCSV_WithInvalidFileName_ReturnsBadRequest() throws Exception {
        // Given
        MultipartFile mockFile =
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
    void uploadApplishare_shouldReturnHttp200AndSuccessMessage() throws Exception {
        mockMvc.perform(post("/kpi-green/applications/applishare"))
                .andExpect(status().isOk())
                .andExpect(content().string("Chargement via Hyperx effectué avec succès !"));

        verify(greenItService, times(1)).miseAJourApplishareMetricsGreenItFromApi();

        verifyNoMoreInteractions(greenItService);
    }
}
