package fr.insee.compas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import fr.insee.compas.exception.CompasUploadException;
import fr.insee.compas.mapper.ApplicationConsommationElectriqueViewMapper;
import fr.insee.compas.mapper.IndicateurApplicationGreenITViewMapper;
import fr.insee.compas.mapper.IndicateurModuleGreenITViewMapper;
import fr.insee.compas.mapper.ModuleConsommationElectriqueViewMapper;
import fr.insee.compas.service.FichierControlService;
import fr.insee.compas.service.GreenItService;

@WebMvcTest(GreenItController.class)
@Import(NoSecurityConfig.class)
class GreenItControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private FichierControlService fichierControlService;

    @MockitoBean private GreenItService greenItService;

    @MockitoBean private IndicateurModuleGreenITViewMapper indicateurModuleGreenITViewMapper;

    @MockitoBean
    private IndicateurApplicationGreenITViewMapper indicateurApplicationGreenITViewMapper;

    @MockitoBean
    private ApplicationConsommationElectriqueViewMapper applicationConsommationElectriqueViewMapper;

    @MockitoBean
    private ModuleConsommationElectriqueViewMapper moduleConsommationElectriqueViewMapper;

    @Autowired private WebApplicationContext wac;

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
        final MultipartFile mockFile =
                new MockMultipartFile("file", "badname.csv", "text/csv", "data".getBytes());

        when(fichierControlService.controlFileName("badname.csv"))
                .thenThrow(new CompasUploadException(422, null));

        // When / Then
        mockMvc.perform(
                        multipart("/kpi-green/modules/upload")
                                .file((MockMultipartFile) mockFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnprocessableEntity());

        verify(greenItService, never()).miseAJourIndicateursGreenItFromFile(any(), any());
    }
}
