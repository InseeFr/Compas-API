package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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
}
