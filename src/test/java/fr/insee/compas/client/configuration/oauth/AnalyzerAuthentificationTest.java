package fr.insee.compas.client.configuration.oauth;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.tomakehurst.wiremock.WireMockServer;

class AnalyzerAuthentificationTest {

    private WireMockServer server;

    @BeforeEach
    void startWiremock() {
        server = new WireMockServer(wireMockConfig().dynamicPort());
        server.start();
    }

    @AfterEach
    void stopWiremock() {
        // Nettoie un éventuel flag d’interruption pour que WireMock/Jetty puisse bien s'arrêter
        if (Thread.currentThread().isInterrupted()) {
            Thread.interrupted();
        }
        if (server != null) server.stop();
    }

    private AnalyzerAuthentification newBean() {
        AnalyzerAuthentification bean = new AnalyzerAuthentification();
        ReflectionTestUtils.setField(bean, "clientId", "cid");
        ReflectionTestUtils.setField(bean, "clientSecret", "sec");
        ReflectionTestUtils.setField(bean, "urlKeycloak", server.baseUrl() + "/token");
        return bean;
    }

    @Test
    void execute_fetchesTokenAndCaches_ok() {
        // Keycloak simule un 200 avec access_token/expires_in
        server.stubFor(
                post(urlEqualTo("/token"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                    {"access_token":"t1","expires_in":300}
                                                """)));

        AnalyzerAuthentification bean = newBean();

        String token = bean.execute();
        assertThat(token).isEqualTo("t1");

        // Vérifie le POST form + headers
        server.verify(
                1,
                postRequestedFor(urlEqualTo("/token"))
                        .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                        .withRequestBody(containing("client_id=cid"))
                        .withRequestBody(containing("client_secret=sec"))
                        .withRequestBody(containing("grant_type=client_credentials"))
                        .withRequestBody(containing("scope=role-as-group")));

        // Le cache (dateToRefreshToken) a été posé (on vérifie juste non-null et futur)
        LocalDateTime dtr =
                (LocalDateTime) ReflectionTestUtils.getField(bean, "dateToRefreshToken");
        assertThat(dtr).isNotNull().isAfter(LocalDateTime.now());
    }

    @Test
    void execute_usesCachedToken_whenStillValid_noCallToKeycloak() {
        AnalyzerAuthentification bean = newBean();
        // Injecte un token encore valide dans le cache
        ReflectionTestUtils.setField(bean, "token", "cached");
        ReflectionTestUtils.setField(
                bean, "dateToRefreshToken", LocalDateTime.now().plusSeconds(90));

        String token = bean.execute();
        assertThat(token).isEqualTo("cached");

        // Aucune requête envoyée à Keycloak
        server.verify(0, postRequestedFor(urlEqualTo("/token")));
    }

    @Test
    void execute_keycloak500_orBrokenJson_returnsNull() {
        // Renvoie 500 + body non JSON -> provoque IOException au parse
        server.stubFor(
                post(urlEqualTo("/token"))
                        .willReturn(
                                aResponse()
                                        .withStatus(500)
                                        .withHeader("Content-Type", "text/plain")
                                        .withBody("oops")));

        AnalyzerAuthentification bean = newBean();
        String token = bean.execute();

        assertThat(token).isNull();
        server.verify(1, postRequestedFor(urlEqualTo("/token")));
    }

    @Test
    void execute_interrupted_setsInterruptFlag() throws Exception {
        // Ici on n'utilise pas WireMock : on mocke HttpClient.newHttpClient() pour forcer
        // InterruptedException
        try (MockedStatic<HttpClient> mocked = Mockito.mockStatic(HttpClient.class)) {
            HttpClient http = Mockito.mock(HttpClient.class);
            mocked.when(HttpClient::newHttpClient).thenReturn(http);

            // send(...) lève InterruptedException
            Mockito.when(
                            http.send(
                                    Mockito.any(HttpRequest.class),
                                    Mockito.<HttpResponse.BodyHandler<String>>any()))
                    .thenThrow(new InterruptedException("boom"));

            AnalyzerAuthentification bean = new AnalyzerAuthentification();
            ReflectionTestUtils.setField(bean, "clientId", "cid");
            ReflectionTestUtils.setField(bean, "clientSecret", "sec");
            ReflectionTestUtils.setField(bean, "urlKeycloak", "http://does-not-matter/token");

            String token = bean.execute();

            assertThat(token).isNull();
            // Le flag d'interruption est bien remonté
            assertThat(Thread.currentThread().isInterrupted()).isTrue();

            // On nettoie pour ne pas polluer les autres tests
            Thread.interrupted();
        }
    }
}
