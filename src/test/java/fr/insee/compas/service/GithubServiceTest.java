package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import fr.insee.compas.dto.devops.AuthorsDto;
import fr.insee.compas.service.gitservice.GithubService;

class GithubServiceTest {

    @Mock private RestTemplate restTemplate;

    private GithubService githubService;

    @BeforeEach
    void setUp() throws Exception {
        // GIVEN : RestTemplate mocké
        restTemplate = mock(RestTemplate.class);
        githubService = new GithubService(restTemplate);

        // Injection du token codé en dur via réflexion
        Field tokenField = GithubService.class.getDeclaredField("githubToken");
        tokenField.setAccessible(true);
        tokenField.set(githubService, "dummyToken");

        // Appel de initHeaders() via réflexion
        Method initMethod = GithubService.class.getDeclaredMethod("initHeaders");
        initMethod.setAccessible(true);
        initMethod.invoke(githubService);
    }

    @Test
    void testGetGithubAuthorsForRepo_returnsUniqueAuthors() throws IOException {
        // GIVEN: JSON de réponse simulée
        String jsonResponse =
                """
                {
                  "data": {
                    "repository": {
                      "refs": {
                        "nodes": [
                          {
                            "target": {
                              "history": {
                                "nodes": [
                                  { "author": { "email": "author1@example.com", "name": "Author One" } },
                                  { "author": { "email": "author2@example.com", "name": "Author Two" } },
                                  { "author": { "email": "noreply@example.com", "name": "Bot" } }
                                ]
                              }
                            }
                          }
                        ]
                      }
                    }
                  }
                }
                """;

        // Mock du RestTemplate pour retourner la réponse simulée
        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

        String owner = "inseeFr";
        String repo = "repo";
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        // WHEN: appel de la méthode
        Set<AuthorsDto> authors = githubService.getGithubAuthorsForRepo(owner, repo, start, end);

        // THEN: vérifie que seuls les auteurs valides sont conservés
        assertNotNull(authors);
        assertEquals(2, authors.size());
        assertTrue(authors.stream().anyMatch(l -> l.email().contains("author2@example.com")));
        assertFalse(authors.stream().anyMatch(l -> l.email().contains("noreply@example.com")));

        // Vérifie que RestTemplate a été appelé exactement 1 fois
        ArgumentCaptor<HttpEntity<String>> captor =
                ArgumentCaptor.forClass((Class) HttpEntity.class);
        verify(restTemplate, times(1))
                .postForEntity(any(String.class), captor.capture(), eq(String.class));

        // Vérifie que le header Authorization contient le token
        HttpHeaders headers = captor.getValue().getHeaders();
        assertEquals("Bearer dummyToken", headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testGetGithubAuthorsForRepo_throwsIOExceptionOnErrorStatus() {
        // GIVEN
        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("error", HttpStatus.INTERNAL_SERVER_ERROR));

        String owner = "inseeFr";
        String repo = "repo";
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        // WHEN / THEN
        IOException exception =
                assertThrows(
                        IOException.class,
                        () -> githubService.getGithubAuthorsForRepo(owner, repo, start, end));
        assertTrue(exception.getMessage().contains("Erreur GitHub GraphQL"));
    }
}
