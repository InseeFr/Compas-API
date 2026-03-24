package fr.insee.compas.service;

import static fr.insee.compas.util.GitLabConstantes.INDICATEURS_MD;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import fr.insee.compas.dto.gitlab.MarkdownResultGitlabDto;
import fr.insee.compas.dto.gitlab.TagsGitLabDto;
import fr.insee.compas.exception.GitLabException;
import fr.insee.compas.service.gitservice.GitlabService;

class GitlabServiceTest {

    private GitlabService service;
    private RestTemplate restTemplate;

    @BeforeEach
    void setup() throws Exception {
        restTemplate = mock(RestTemplate.class);

        service = new GitlabService(restTemplate);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Private-Token", "dummy-token");
        java.lang.reflect.Field headersField = GitlabService.class.getDeclaredField("headers");
        headersField.setAccessible(true);
        headersField.set(service, headers);
    }

    @Test
    void shouldReturnLatestTags() {
        // GIVEN
        TagsGitLabDto.TagApi apiTag = new TagsGitLabDto.TagApi("v1.0.0", "2024-01-01T10:00:00Z");
        TagsGitLabDto.TagIhm ihmTag = new TagsGitLabDto.TagIhm("v2.0.0", "2024-02-01T10:00:00Z");

        ResponseEntity<List<TagsGitLabDto.TagApi>> apiResponse =
                new ResponseEntity<>(List.of(apiTag), HttpStatus.OK);

        ResponseEntity<List<TagsGitLabDto.TagIhm>> ihmResponse =
                new ResponseEntity<>(List.of(ihmTag), HttpStatus.OK);

        when(restTemplate.exchange(
                        any(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(apiResponse)
                .thenReturn(ihmResponse);

        // WHEN
        TagsGitLabDto result = service.getLatestTags();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getApiTag().name()).isEqualTo("v1.0.0");
        assertThat(result.getIhmTag().name()).isEqualTo("v2.0.0");
    }

    @Test
    void shouldReturnNullWhenNoTags() {
        ResponseEntity<List<TagsGitLabDto.TagApi>> emptyResponse =
                new ResponseEntity<>(List.of(), HttpStatus.OK);

        when(restTemplate.exchange(any(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(emptyResponse)
                .thenReturn(emptyResponse);

        TagsGitLabDto result = service.getLatestTags();

        assertThat(result).isNull();
    }

    @Test
    void shouldThrowExceptionWhenGitlabFails() {
        when(restTemplate.exchange(any(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        org.junit.jupiter.api.Assertions.assertThrows(
                GitLabException.class, () -> service.getLatestTags());
    }

    @Test
    void testGetGitlabAuthorsForProject() throws IOException {
        // GIVEN
        String projectPath = "namespace/projet";
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now();

        // Création d'un JSON simulant la réponse de GitLab
        String jsonResponse =
                "[{\"author_email\":\"dev1@example.com\",\"author_name\":\"Dev One\"},"
                        + "{\"author_email\":\"dev2@example.com\",\"author_name\":\"Dev Two\"}]";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        // WHEN : mock du RestTemplate pour retourner la page
        when(restTemplate.exchange(any(), any(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // WHEN : appel de la méthode
        Set<String> authors = service.getGitlabAuthorsForProject(projectPath, start, end);

        // THEN : vérifie que les auteurs uniques sont récupérés
        assertEquals(2, authors.size());
        assertTrue(authors.contains("dev1@example.com"));
        assertTrue(authors.contains("dev2@example.com"));

        // THEN : capture l'entité envoyée au RestTemplate
        ArgumentCaptor<HttpEntity<Void>> captor =
                (ArgumentCaptor<HttpEntity<Void>>)
                        (ArgumentCaptor<?>) ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).exchange(any(), any(), captor.capture(), eq(String.class));

        HttpEntity<Void> capturedEntity = captor.getValue();
        assertNotNull(capturedEntity.getHeaders().getFirst("Private-Token"));
        assertEquals("dummy-token", capturedEntity.getHeaders().getFirst("Private-Token"));
    }

    @Test
    void shouldReturnMarkdowns() {
        when(restTemplate.exchange(
                        any(URI.class),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(MarkdownResultGitlabDto.class)))
                .thenAnswer(
                        invocation -> {
                            URI uri = invocation.getArgument(0);

                            String indicateur =
                                    INDICATEURS_MD.stream()
                                            .filter(uri.toString()::contains)
                                            .findFirst()
                                            .orElseThrow();

                            MarkdownResultGitlabDto dto =
                                    new MarkdownResultGitlabDto(
                                            BigInteger.ONE,
                                            "markdown",
                                            "slug",
                                            "Titre",
                                            "# Markdown " + indicateur,
                                            "UTF-8");

                            return ResponseEntity.ok(dto);
                        });

        Map<String, String> result = service.getMarkdownIndicators();

        assertThat(result).hasSize(INDICATEURS_MD.size());

        INDICATEURS_MD.forEach(
                indicateur ->
                        assertThat(result).containsEntry(indicateur, "# Markdown " + indicateur));
    }

    @Test
    void shouldThrowGitLabExceptionOnHttpClientError() {
        when(restTemplate.exchange(
                        any(URI.class),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(MarkdownResultGitlabDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> service.getMarkdownIndicators())
                .isInstanceOf(GitLabException.class)
                .hasMessageContaining("Erreur lors de la récupération du markdown");
    }

    @Test
    void shouldIgnoreNotFoundIndicator() {
        when(restTemplate.exchange(
                        any(URI.class),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(MarkdownResultGitlabDto.class)))
                .thenReturn(new ResponseEntity<>((HttpHeaders) null, HttpStatus.NOT_FOUND));

        Map<String, String> result = service.getMarkdownIndicators();
        assertThat(result).isEmpty();
    }
}
