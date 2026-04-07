package fr.insee.compas.service;

import static fr.insee.compas.util.GitLabConstantes.INDICATEURS_MD;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import fr.insee.compas.dto.devops.AuthorsDto;
import fr.insee.compas.dto.gitlab.MarkdownResultGitlabDto;
import fr.insee.compas.dto.gitlab.TagsGitLabDto;
import fr.insee.compas.exception.GitLabException;
import fr.insee.compas.service.gitservice.GitlabService;
import fr.insee.compas.util.DevopsConstantes;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

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
    void testGetGitlabAuthorsForProject_returnsUniqueValidAuthors() {
        // GIVEN
        String projectPath = "namespace/projet";
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now();

        // JSON simulant une page de commits GitLab — champs committer_* attendus par
        // l'implémentation
        String jsonBody =
                """
                [
                  { "committer_email": "dev1@example.com", "committer_name": "Dev One" },
                  { "committer_email": "dev2@example.com", "committer_name": "Dev Two" },
                  { "committer_email": "noreply@github.com", "committer_name": "Bot" }
                ]
                """;

        List<JsonNode> nodes = parseJsonNodes(jsonBody);

        // En-tête de pagination : une seule page
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(DevopsConstantes.FIELD_X_TOTAL_PAGE, "1");

        ResponseEntity<List<JsonNode>> responseEntity =
                new ResponseEntity<>(nodes, responseHeaders, HttpStatus.OK);

        when(restTemplate.exchange(
                        any(URI.class),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // WHEN
        Set<AuthorsDto> authors = service.getGitlabAuthorsForProject(projectPath, start, end);

        // THEN — le bot noreply doit être filtré, les deux humains conservés
        assertNotNull(authors);
        assertEquals(2, authors.size());
        assertTrue(
                authors.stream().anyMatch(a -> a.email().equals("dev1@example.com")),
                "dev1 devrait être présent");
        assertTrue(
                authors.stream().anyMatch(a -> a.email().equals("dev2@example.com")),
                "dev2 devrait être présent");
        assertFalse(
                authors.stream().anyMatch(a -> a.email().contains("noreply")),
                "les adresses noreply doivent être exclues");
    }

    @Test
    void testGetGitlabAuthorsForProject_withMultiplePages_fetchesAllPages() {
        // GIVEN
        String projectPath = "namespace/projet";
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now();

        String page1Json =
                """
                [{ "committer_email": "dev1@example.com", "committer_name": "Dev One" }]
                """;
        String page2Json =
                """
                [{ "committer_email": "dev2@example.com", "committer_name": "Dev Two" }]
                """;

        HttpHeaders headersPage1 = new HttpHeaders();
        headersPage1.set(DevopsConstantes.FIELD_X_TOTAL_PAGE, "2");

        HttpHeaders headersPage2 = new HttpHeaders();
        headersPage2.set(DevopsConstantes.FIELD_X_TOTAL_PAGE, "2");

        // Guard against null URI before calling toString()
        when(restTemplate.exchange(
                        argThat(uri -> uri != null && uri.toString().contains("page=1")),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(
                        new ResponseEntity<>(
                                parseJsonNodes(page1Json), headersPage1, HttpStatus.OK));

        when(restTemplate.exchange(
                        argThat(uri -> uri != null && uri.toString().contains("page=2")),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(
                        new ResponseEntity<>(
                                parseJsonNodes(page2Json), headersPage2, HttpStatus.OK));

        // WHEN
        Set<AuthorsDto> authors = service.getGitlabAuthorsForProject(projectPath, start, end);

        // THEN
        assertEquals(2, authors.size());
        assertTrue(authors.stream().anyMatch(a -> a.email().equals("dev1@example.com")));
        assertTrue(authors.stream().anyMatch(a -> a.email().equals("dev2@example.com")));

        // Vérifie que les deux pages ont bien été appelées
        verify(restTemplate, times(2))
                .exchange(
                        argThat(uri -> uri != null && uri.toString().contains("page=1")),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class));

        verify(restTemplate, times(1))
                .exchange(
                        argThat(uri -> uri != null && uri.toString().contains("page=2")),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class));
    }

    @Test
    void testGetGitlabAuthorsForProject_returns404_givesEmptySet() {
        // GIVEN
        String projectPath = "namespace/introuvable";
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now();

        when(restTemplate.exchange(
                        any(URI.class),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(
                        HttpClientErrorException.NotFound.create(
                                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        // WHEN
        Set<AuthorsDto> authors = service.getGitlabAuthorsForProject(projectPath, start, end);

        // THEN — 404 doit retourner un ensemble vide sans lever d'exception
        assertNotNull(authors);
        assertTrue(authors.isEmpty(), "Un projet 404 doit retourner un ensemble vide");
    }

    @Test
    void testGetGitlabAuthorsForProject_privateTokenHeaderIsSent() {
        // GIVEN
        String projectPath = "namespace/projet";
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(DevopsConstantes.FIELD_X_TOTAL_PAGE, "1");

        when(restTemplate.exchange(
                        any(URI.class),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(List.of(), responseHeaders, HttpStatus.OK));

        // WHEN
        service.getGitlabAuthorsForProject(projectPath, start, end);

        // THEN — capture et vérifie le header Private-Token
        ArgumentCaptor<HttpEntity<Void>> captor = ArgumentCaptor.forClass((Class) HttpEntity.class);
        verify(restTemplate)
                .exchange(
                        any(URI.class),
                        eq(HttpMethod.GET),
                        captor.capture(),
                        any(ParameterizedTypeReference.class));

        HttpHeaders sentHeaders = captor.getValue().getHeaders();
        assertNotNull(
                sentHeaders.getFirst("Private-Token"), "Le header Private-Token doit être présent");
        assertEquals("dummy-token", sentHeaders.getFirst("Private-Token"));
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

    // --- Helper ----------------------------------------------------------------

    private List<JsonNode> parseJsonNodes(String json) {
        return new ObjectMapper().readValue(json, new TypeReference<>() {});
    }
}
