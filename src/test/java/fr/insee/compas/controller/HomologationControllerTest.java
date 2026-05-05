package fr.insee.compas.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.insee.compas.dto.HomologationDoublonsGristDto;
import fr.insee.compas.dto.HomologationDto;
import fr.insee.compas.service.homologation.IHomologationService;

@WebMvcTest(HomologationController.class)
@Import(NoSecurityConfig.class)
class HomologationControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private IHomologationService homologationService;

    @Test
    void Homologationstest() throws Exception {

        HomologationDto dto1 = new HomologationDto(1, "app1", "", "", "", "", "", "");
        HomologationDto dto2 = new HomologationDto(2, "app2", "", "", "", "", "", "");

        when(homologationService.getAllHomologation()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(
                        get("/homologations/application-homologation")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void ApplicationsAbsentesOscartest() throws Exception {

        List<String> apps = List.of("APP1", "APP2");

        when(homologationService.getAppliAbsentesOscar()).thenReturn(apps);

        mockMvc.perform(get("/homologations/homologation/applications-absentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("APP1"))
                .andExpect(jsonPath("$[1]").value("APP2"));
    }

    @Test
    void getApplicationGrist() throws Exception {
        when(homologationService.getApplicationGrist()).thenReturn(List.of("app1", "app2", "app3"));

        mockMvc.perform(get("/homologations/applications-grist"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("app1, \napp2, \napp3"));
    }

    @Test
    void getDoublonsGrist() throws Exception {
        // Given
        HomologationDoublonsGristDto dto =
                HomologationDoublonsGristDto.builder()
                        .nomApplication("arc")
                        .nombreOccurrences(2)
                        .listeSI(List.of("SI A", "SI B"))
                        .build();

        when(homologationService.getDoublonsGrist()).thenReturn(List.of(dto));

        mockMvc.perform(get("/homologations/doublons-grist"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nomApplication").value("arc"))
                .andExpect(jsonPath("$[0].nombreOccurrences").value(2))
                .andExpect(jsonPath("$[0].listeSI[0]").value("SI A"));
    }
}
