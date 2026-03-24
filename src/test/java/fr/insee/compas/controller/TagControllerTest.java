package fr.insee.compas.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.insee.compas.dto.gitlab.TagsGitLabDto;
import fr.insee.compas.service.gitservice.GitlabService;

@WebMvcTest(TagsController.class)
@AutoConfigureMockMvc(addFilters = false)
class TagControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private GitlabService gitlabService;

    @Test
    void shouldReturnTags() throws Exception {
        TagsGitLabDto.TagApi apiTag = new TagsGitLabDto.TagApi("v1.0.0", "2024-01-01T10:00:00Z");
        TagsGitLabDto.TagIhm ihmTag = new TagsGitLabDto.TagIhm("v2.0.0", "2024-02-01T10:00:00Z");

        TagsGitLabDto dto = new TagsGitLabDto(apiTag, ihmTag);

        when(gitlabService.getLatestTags()).thenReturn(dto);

        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiTagView.tag").value("v1.0.0"))
                .andExpect(jsonPath("$.ihmTagView.tag").value("v2.0.0"));
    }

    @Test
    void shouldHandleNullTags() throws Exception {
        // GIVEN
        TagsGitLabDto dto = new TagsGitLabDto(null, null);

        when(gitlabService.getLatestTags()).thenReturn(dto);

        // WHEN & THEN
        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiTagView").doesNotExist())
                .andExpect(jsonPath("$.ihmTagView").doesNotExist());
    }
}
