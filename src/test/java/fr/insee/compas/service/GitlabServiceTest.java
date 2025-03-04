package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import fr.insee.compas.model.oscar.Module;

@ExtendWith(MockitoExtension.class)
class GitlabServiceTest {

    @Mock private HttpClient httpClient;

    @Mock private HttpResponse<String> httpResponse;

    @InjectMocks private GitlabService gitlabService;

    private static final String TOKEN = "fake-token";
    private static final String BASE_URL = "https://gitlab.insee.fr/api/v4";
    private static final String PROJECT_ID = "13644";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(gitlabService, "token", TOKEN);
    }

    @Test
    void testGetJson_Success() throws IOException, InterruptedException {
        // Given
        Module module =
                Module.builder()
                        .id(1)
                        .modName("name1")
                        .domaineSndi("sndi1")
                        .keySonar("keySonar1")
                        .build();
        String encodedFilePath = URLEncoder.encode("rapports/1.json", StandardCharsets.UTF_8);
        String expectedUrl =
                String.format(
                        "%s/projects/%s/repository/files/%s/raw",
                        BASE_URL, PROJECT_ID, encodedFilePath);
        String expectedResponse = "{\"key\": \"value\"}";

        Mockito.<HttpResponse<String>>when(
                        httpClient.send(
                                Mockito.argThat(req -> req.uri().equals(URI.create(expectedUrl))),
                                any()))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(expectedResponse);

        // When
        String jsonResponse = gitlabService.getJson(module);

        // Then
        assertEquals(expectedResponse, jsonResponse);
        verify(httpClient).send(any(HttpRequest.class), any());
    }

    @Test
    void testGetJson_Fail() throws IOException, InterruptedException {
        // Given
        Module module =
                Module.builder()
                        .id(2)
                        .modName("name1")
                        .domaineSndi("sndi1")
                        .keySonar("keySonar1")
                        .build();

        String encodedFilePath = URLEncoder.encode("rapports/2.json", StandardCharsets.UTF_8);
        String expectedUrl =
                String.format(
                        "%s/projects/%s/repository/files/%s/raw",
                        BASE_URL, PROJECT_ID, encodedFilePath);

        Mockito.<HttpResponse<String>>when(
                        httpClient.send(
                                Mockito.argThat(req -> req.uri().equals(URI.create(expectedUrl))),
                                any()))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(404);

        // When
        String jsonResponse = gitlabService.getJson(module);

        // Then
        assertEquals("", jsonResponse);
        verify(httpClient).send(any(HttpRequest.class), any());
    }
}
