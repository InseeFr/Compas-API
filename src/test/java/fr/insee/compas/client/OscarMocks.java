package fr.insee.compas.client;

import org.springframework.http.HttpStatus;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

public class OscarMocks {

    public static void setUpMockOscarResponse(WireMockServer mockServer) {
        mockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/applications/123"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{\"id\":\"0\",\"nom\":\"sirene4\"}")));
    }
}
