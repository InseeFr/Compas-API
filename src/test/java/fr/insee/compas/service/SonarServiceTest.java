package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import fr.insee.compas.model.sonar.RecuperationMeasures;

import okhttp3.*;

@ExtendWith(MockitoExtension.class)
class SonarServiceTest {

    @Mock private OkHttpClient mockClient;

    @Mock private Call mockCall;

    @Mock private Response mockResponse;

    @Mock private ResponseBody mockResponseBody;

    @InjectMocks private SonarService sonarService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sonarService, "token", "mock-token");
    }

    @Test
    void testGetDataFromSonarAPIMeasures_Success() throws IOException {
        String projetSonar = "test-project";
        String jsonResponse = "{\"component\":{\"measures\":[{\"value\":\"85.0\"}]}}";

        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponseBody.string()).thenReturn(jsonResponse);

        RecuperationMeasures result = sonarService.getDataFromSonarAPIMeasures(projetSonar);
        assertNotNull(result);
        assertEquals("85.0", result.getComponent().getMeasures().getFirst().getValue());
    }

    @Test
    void testGetDataFromSonarAPIMeasures_FailedResponse() throws IOException {
        String projetSonar = "test-project";

        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        // 🚨 Simuler une réponse échouée (exemple : HTTP 500)
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.body()).thenReturn(mockResponseBody);

        RecuperationMeasures result = sonarService.getDataFromSonarAPIMeasures(projetSonar);

        // 🔥 Vérifier que la méthode retourne `null`
        assertNull(result);
    }
}
