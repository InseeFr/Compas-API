package fr.insee.compas.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.insee.compas.service.GitlabService;

@WebMvcTest(AccueilController.class)
@Import(NoSecurityConfig.class)
class AccueilControllerTest {
    @Autowired private MockMvc mockMvc;

    @MockitoBean private GitlabService gitlabService;

    @Test
    void shouldReturnMarkdowns() throws Exception {
        Map<String, String> markdowns =
                Map.of(
                        "indicator1", "# Markdown 1",
                        "indicator2", "# Markdown 2");

        when(gitlabService.getMarkdownIndicators()).thenReturn(markdowns);

        mockMvc.perform(get("/accueil/indicators"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.indicator1").value("# Markdown 1"))
                .andExpect(jsonPath("$.indicator2").value("# Markdown 2"));

        verify(gitlabService).getMarkdownIndicators();
    }
}
