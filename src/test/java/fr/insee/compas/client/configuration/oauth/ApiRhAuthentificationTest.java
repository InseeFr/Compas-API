package fr.insee.compas.client.configuration.oauth;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.tomakehurst.wiremock.WireMockServer;

class ApiRhAuthentificationTest {

    private WireMockServer server;

    @BeforeEach
    void startWiremock() {
        server = new WireMockServer(wireMockConfig().dynamicPort());
        server.start();
    }

    @AfterEach
    void stopWiremock() {
        // Si un test a laissé le thread "interrompu", on nettoie pour que Jetty s'arrête
        // correctement
        if (Thread.currentThread().isInterrupted()) {
            Thread.interrupted();
        }
        if (server != null) server.stop();
    }

    private ApiRhAuthentification newBean() {
        ApiRhAuthentification bean = new ApiRhAuthentification();
        // injecte les @Value via réflexion
        ReflectionTestUtils.setField(bean, "clientId", "cid");
        ReflectionTestUtils.setField(bean, "clientSecret", "sec");
        ReflectionTestUtils.setField(bean, "urlKeycloak", server.baseUrl() + "/token");
        ReflectionTestUtils.setField(bean, "apiRhAgentsUrl", server.baseUrl() + "/agents");
        return bean;
    }

    // ====== TESTS ======

    @Test
    void recupererIdepEtEmails_happyPath_ok() {
        // 1) stub Keycloak /token
        server.stubFor(
                post(urlEqualTo("/token"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {"access_token":"tok123","expires_in":300}
                                                """)));

        // 2) stub API RH /agents (format attendu "content":[...])
        server.stubFor(
                get(urlPathEqualTo("/agents"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {
                                                  "content": [
                                                    {
                                                      "identifiantsAgent": {"idep":"AA0001"},
                                                      "coordonnees": {"email":"aa0001@insee.fr"}
                                                    },
                                                    {
                                                      "identifiantsAgent": {"idep":"BB0002"},
                                                      "coordonnees": {"email":"bb0002@insee.fr"}
                                                    },
                                                    {
                                                      "identifiantsAgent": {"idep":"NOEMAIL"},
                                                      "coordonnees": {}
                                                    }
                                                  ]
                                                }
                                                """)));

        ApiRhAuthentification bean = newBean();
        Map<String, String> map = bean.recupererIdepEtEmails();

        // On garde seulement les 2 valides
        assertThat(map)
                .hasSize(2)
                .containsEntry("AA0001", "aa0001@insee.fr")
                .containsEntry("BB0002", "bb0002@insee.fr");

        // Vérifie que les endpoints ont bien été appelés
        server.verify(1, postRequestedFor(urlEqualTo("/token")));
        server.verify(1, getRequestedFor(urlPathEqualTo("/agents")));
    }

    @Test
    void recupererIdepEtEmails_usesCachedToken_skipsKeycloak() {
        // On ne STUB PAS /token exprès, pour s'assurer qu'il n'est pas appelé.
        // On STUB seulement /agents :
        server.stubFor(
                get(urlPathEqualTo("/agents"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {"content":[
                                                  {"identifiantsAgent":{"idep":"CC0003"},
                                                   "coordonnees":{"email":"cc0003@insee.fr"}}
                                                ]}
                                                """)));

        ApiRhAuthentification bean = newBean();
        // Injecte un token encore valide
        ReflectionTestUtils.setField(bean, "token", "cached-token");
        ReflectionTestUtils.setField(
                bean, "dateToRefreshToken", LocalDateTime.now().plusSeconds(60));

        Map<String, String> map = bean.recupererIdepEtEmails();
        assertThat(map).containsEntry("CC0003", "cc0003@insee.fr");

        // Keycloak n'a pas été appelé
        server.verify(0, postRequestedFor(urlEqualTo("/token")));
        server.verify(1, getRequestedFor(urlPathEqualTo("/agents")));
    }

    @Test
    void recupererIdepEtEmails_keycloak401_returnsEmpty() {
        server.stubFor(post(urlEqualTo("/token")).willReturn(aResponse().withStatus(401)));

        // Même si /agents répondrait 200, on mettra un 401 pour refléter le Bearer null/invalide
        server.stubFor(get(urlPathEqualTo("/agents")).willReturn(aResponse().withStatus(401)));

        ApiRhAuthentification bean = newBean();
        Map<String, String> map = bean.recupererIdepEtEmails();

        assertThat(map).isEmpty();
        server.verify(1, postRequestedFor(urlEqualTo("/token")));
        server.verify(1, getRequestedFor(urlPathEqualTo("/agents")));
    }

    @Test
    void recupererIdepEtEmails_agents500_returnsEmpty() {
        // token OK
        server.stubFor(
                post(urlEqualTo("/token"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {"access_token":"tokXYZ","expires_in":120}
                                                """)));
        // agents KO
        server.stubFor(get(urlPathEqualTo("/agents")).willReturn(aResponse().withStatus(500)));

        ApiRhAuthentification bean = newBean();
        Map<String, String> map = bean.recupererIdepEtEmails();

        assertThat(map).isEmpty();
        server.verify(1, postRequestedFor(urlEqualTo("/token")));
        server.verify(1, getRequestedFor(urlPathEqualTo("/agents")));
    }
}
