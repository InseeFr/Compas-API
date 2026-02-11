package fr.insee.compas.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import fr.insee.compas.exception.SonarApiException;
import fr.insee.compas.model.sonar.RecuperationMeasures;

@ExtendWith(MockitoExtension.class)
class SonarServiceTest {

    @Mock private RestTemplate restTemplate;

    @Mock private RestTemplateBuilder restTemplateBuilder;

    private SonarService sonarService;

    private static final String PROJET = "mon-projet";
    private static final String TOKEN_GITLAB = "gitlab-token";
    private static final String TOKEN_GITHUB = "github-token";

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        sonarService = new SonarService(restTemplateBuilder);
        ReflectionTestUtils.setField(sonarService, "tokenGitlab", TOKEN_GITLAB);
        ReflectionTestUtils.setField(sonarService, "tokenGithub", TOKEN_GITHUB);
        ReflectionTestUtils.setField(sonarService, "proxyName", "proxy.test");
        ReflectionTestUtils.setField(sonarService, "proxyPort", 8080);
    }

    @Test
    void getNbIssueSonarAccessibility_whenProjetIsNull_shouldReturnNull() {
        assertThat(sonarService.getNbIssueSonarAccessibility(null, "gitlab", "mon-projet"))
                .isNull();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void getNbIssueSonarAccessibility_whenProjetIsBlank_shouldReturnNull() {
        assertThat(sonarService.getNbIssueSonarAccessibility("   ", "gitlab", "mon-projet"))
                .isNull();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void getNbIssueSonarAccessibility_withGitlab_shouldReturnTotal() {
        String jsonResponse = "{\"total\": 42}";
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

        String result = sonarService.getNbIssueSonarAccessibility(PROJET, "gitlab", "mon-projet");

        assertThat(result).isEqualTo("42");
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate)
                .exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(), eq(String.class));
        assertThat(urlCaptor.getValue())
                .contains("https://sonar.insee.fr")
                .contains(PROJET)
                .contains("accessibility");
    }

    @Test
    void getNbIssueSonarAccessibility_whenResponseBodyIsNull_shouldReturnNull() {
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>((HttpHeaders) null, HttpStatus.OK));

        assertThat(sonarService.getNbIssueSonarAccessibility(PROJET, "gitlab", "mon-projet"))
                .isNull();
    }

    @Test
    void getNbIssueSonarAccessibility_whenNotFound_shouldReturnNull() {
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(
                        HttpClientErrorException.NotFound.create(
                                HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThat(sonarService.getNbIssueSonarAccessibility(PROJET, "gitlab", "mon-projet"))
                .isNull();
    }

    @Test
    void getNbIssueSonarAccessibility_whenHttpError_shouldThrowSonarApiException() {
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(
                        () ->
                                sonarService.getNbIssueSonarAccessibility(
                                        PROJET, "gitlab", "mon-projet"))
                .isInstanceOf(SonarApiException.class)
                .hasMessageContaining("500");
    }

    @Test
    void getNbIssueSonarAccessibility_whenRestClientException_shouldThrowSonarApiException() {
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Network error"));

        assertThatThrownBy(
                        () ->
                                sonarService.getNbIssueSonarAccessibility(
                                        PROJET, "gitlab", "mon-projet"))
                .isInstanceOf(SonarApiException.class)
                .hasMessageContaining("NBISSUE");
    }

    @Test
    void getDataFromSonarAPIMeasures_whenProjetIsNull_shouldReturnNull() {
        assertThat(sonarService.getDataFromSonarAPIMeasures(null, "gitlab", "mon-projet")).isNull();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void getDataFromSonarAPIMeasures_whenProjetIsBlank_shouldReturnNull() {
        assertThat(sonarService.getDataFromSonarAPIMeasures("", "gitlab", "mon-projet")).isNull();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void getDataFromSonarAPIMeasures_withGitlab_shouldReturnMeasures() {
        String jsonResponse = "{\"component\": {\"key\": \"test\", \"measures\": []}}";
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

        RecuperationMeasures result =
                sonarService.getDataFromSonarAPIMeasures(PROJET, "gitlab", "mon-projet");

        assertThat(result).isNotNull();
        assertThat(result.getComponent()).isNotNull();
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate)
                .exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(), eq(String.class));
        assertThat(urlCaptor.getValue()).contains("https://sonar.insee.fr/api/measures/component");
    }

    @Test
    void getDataFromSonarAPIMeasures_whenResponseBodyIsNull_shouldReturnNull() {
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>((HttpHeaders) null, HttpStatus.OK));

        assertThat(sonarService.getDataFromSonarAPIMeasures(PROJET, "gitlab", "mon-projet"))
                .isNull();
    }

    @Test
    void getDataFromSonarAPIMeasures_whenNotFound_shouldReturnNull() {
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(
                        HttpClientErrorException.NotFound.create(
                                HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThat(sonarService.getDataFromSonarAPIMeasures(PROJET, "gitlab", "mon-projet"))
                .isNull();
    }

    @Test
    void getDataFromSonarAPIMeasures_whenHttpError_shouldThrowSonarApiException() {
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(
                        () ->
                                sonarService.getDataFromSonarAPIMeasures(
                                        PROJET, "gitlab", "mon-projet"))
                .isInstanceOf(SonarApiException.class)
                .hasMessageContaining("503");
    }

    @Test
    void getDataFromSonarAPIMeasures_whenRestClientException_shouldThrowSonarApiException() {
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Timeout"));

        assertThatThrownBy(
                        () ->
                                sonarService.getDataFromSonarAPIMeasures(
                                        PROJET, "gitlab", "mon-projet"))
                .isInstanceOf(SonarApiException.class)
                .hasMessageContaining("API measures");
    }

    @Test
    void shouldUseGitlabTokenForGitlabSource() {
        String jsonResponse = "{\"total\": 10}";
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), entityCaptor.capture(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

        sonarService.getNbIssueSonarAccessibility(PROJET, "gitlab", "mon-projet");

        assertThat(entityCaptor.getValue().getHeaders().getFirst("Authorization"))
                .isEqualTo("Bearer " + TOKEN_GITLAB);
    }
}
