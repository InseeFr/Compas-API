package fr.insee.compas.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import fr.insee.compas.model.hyperx.IndicateurRecuperationSecuriteVM;

import tools.jackson.databind.ObjectMapper;

@RestClientTest(HyperxService.class)
class HyperxServiceTest {

    private HyperxService hyperxService;

    private MockRestServiceServer mockServer;

    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();

        hyperxService =
                new HyperxService(
                        new RestTemplateBuilder()
                                .requestFactory(() -> restTemplate.getRequestFactory()),
                        "http://fake-url", // URL factice
                        "fake-key");
    }

    @Test
    void testMaxMajVm_ShouldReturnMaxValue() {
        // GIVEN
        String application = "app1";
        String jsonResponse = "[{\"delai_majlin\":10},{\"delai_majlin\":25},{\"delai_majlin\":5}]";

        mockServer
                .expect(
                        once(),
                        requestTo(
                                "http://fake-url/vms?application="
                                        + application
                                        + "&list_colonnes=delai_majlin"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("x-api-key", "fake-key"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // WHEN
        IndicateurRecuperationSecuriteVM result = hyperxService.maxMajVm(application);

        // THEN
        assertThat(result.getMax()).isEqualTo(25);
    }

    @Test
    void testMaxMajVm_ShouldHandleMissingField() {
        String application = "app2";
        String jsonResponse = "[{\"delai_majlin\":null},{}, {\"delai_majlin\":15}]";

        mockServer
                .expect(
                        once(),
                        requestTo(
                                "http://fake-url/vms?application="
                                        + application
                                        + "&list_colonnes=delai_majlin"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        IndicateurRecuperationSecuriteVM result = hyperxService.maxMajVm(application);

        assertThat(result.getMax()).isEqualTo(15);
    }

    @Test
    void testMaxMajVm_ShouldThrowException_WhenJsonInvalid() {
        String application = "app4";
        String invalidJson = "INVALID_JSON";

        mockServer
                .expect(
                        once(),
                        requestTo(
                                "http://fake-url/vms?application="
                                        + application
                                        + "&list_colonnes=delai_majlin"))
                .andRespond(withSuccess(invalidJson, MediaType.APPLICATION_JSON));

        assertThrows(RuntimeException.class, () -> hyperxService.maxMajVm(application));
    }
}
