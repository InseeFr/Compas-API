package fr.insee.compas.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import fr.insee.compas.builder.OscarBuilder;
import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.ApplicationTechnique;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OscarServiceTest {

    @InjectMocks private OscarService oscarService;

    @Mock private RestTemplate restTemplate; // ✅ Correctement mocké

    @Mock private OscarBuilder oscarBuilder;

    @Mock private ObjectMapper objectMapper;

    @Mock private OscarClient oscarClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(oscarService, "urlOscar", "http://fake-oscar-service/");
    }

    @Test
    void testGetModules() throws Exception {
        // Simule une réponse JSON valide
        String jsonResponse =
                "[{\"id\": \"1\", \"nom\": \"Module1\"}, {\"id\": \"2\", \"nom\": \"Module2\"}]";

        // ✅ Crée un vrai JsonNode avec l'ObjectMapper réel
        JsonNode jsonNode = new ObjectMapper().readTree(jsonResponse);

        // ✅ Mock du comportement normal
        when(restTemplate.exchange(
                        eq("http://fake-oscar-service/modules"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(String.class)))
                .thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

        when(objectMapper.readTree(jsonResponse)).thenReturn(jsonNode); // ✅ Correction ici

        // Mock OscarBuilder
        Module module1 = new Module();
        Module module2 = new Module();
        when(oscarBuilder.buildModule(any(JsonNode.class))).thenReturn(module1, module2);

        // Exécuter la méthode
        List<Module> modules = oscarService.getModules();

        // Vérifications
        assertNotNull(modules);
        assertEquals(2, modules.size());

        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(String.class));
        verify(oscarBuilder, times(2)).buildModule(any(JsonNode.class));
    }

    @Test
    void testGetApplications() throws Exception {
        // Simule une réponse JSON valide
        String jsonResponse =
                "[{\"id\": \"1\", \"nom\": \"App1\"}, {\"id\": \"2\", \"nom\": \"App2\"}]";

        // ✅ Crée un vrai JsonNode avec l'ObjectMapper réel
        JsonNode jsonNode = new ObjectMapper().readTree(jsonResponse);

        // ✅ Mock du comportement normal
        when(restTemplate.exchange(
                        eq("http://fake-oscar-service/applications"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(String.class)))
                .thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

        when(objectMapper.readTree(jsonResponse)).thenReturn(jsonNode); // ✅ Correction ici

        // Mock OscarBuilder
        Application app1 = new Application();
        Application app2 = new Application();
        when(oscarBuilder.buildApplication(any(JsonNode.class))).thenReturn(app1, app2);

        // Exécuter la méthode
        List<Application> applications = oscarService.getApplications();

        // Vérifications
        assertNotNull(applications);
        assertEquals(2, applications.size());

        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(String.class));
        verify(oscarBuilder, times(2)).buildApplication(any(JsonNode.class));
    }

    @Test
    void testGetApplications_JsonProcessingException() throws Exception {
        // Simule une réponse JSON invalide
        String invalidJson = "INVALID_JSON";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidJson, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://fake-oscar-service/applications"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(String.class)))
                .thenReturn(responseEntity);

        // ✅ Mock de ObjectMapper utilisé par OscarService
        when(objectMapper.readTree(invalidJson)).thenThrow(new JacksonException("Invalid JSON") {});

        // ✅ Vérifier que RuntimeException est bien levée
        assertThrows(RuntimeException.class, () -> oscarService.getApplications());
    }

    @Test
    void testGetModules_JsonProcessingException() throws Exception {
        // Simule une réponse JSON invalide
        String invalidJson = "INVALID_JSON";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidJson, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://fake-oscar-service/modules"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(String.class)))
                .thenReturn(responseEntity);

        // ✅ Mock de ObjectMapper utilisé par OscarService
        when(objectMapper.readTree(invalidJson)).thenThrow(new JacksonException("Invalid JSON") {});

        // ✅ Vérifier que la méthode capture l'erreur sans lever d'exception
        oscarService.getModules();

        // ✅ Vérifier que `buildModule()` n'est jamais appelé
        verify(oscarBuilder, never()).buildModule(any(JsonNode.class));
    }

    @Test
    void testGetModuleHistorique_JsonProcessingException() throws Exception {
        // Données invalides simulées
        String invalidJson = "INVALID_JSON";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidJson, HttpStatus.OK);

        // Simuler l'API qui retourne un JSON invalide
        when(oscarClient.getModuleHistoriqueOscar()).thenReturn(responseEntity);

        // Simuler une exception lors du parsing
        when(objectMapper.readTree(invalidJson)).thenThrow(new JacksonException("Invalid JSON") {});

        // Appel à la méthode qui doit gérer l'exception
        Map<String, List<ModuleHistorique>> moduleHistoriquesMap =
                oscarService.getModulesHistorique();

        // Assertions
        assertNotNull(moduleHistoriquesMap);
        assertTrue(
                moduleHistoriquesMap.isEmpty()); // En cas de JSON invalide, la map doit être vide
        verify(oscarClient, times(1))
                .getModuleHistoriqueOscar(); // Vérifiez qu'il y a un appel à l'API
        verify(oscarBuilder, never())
                .buildModuleHistorique(any(JsonNode.class)); // Aucun objet ne doit être créé
    }

    @Test
    void testGetApplicationsTechniques() throws Exception {
        // Arrange
        String jsonResponse =
                """
                [
                    {"id": 1, "nom": "App1"},
                    {"id": 2, "nom": "App2"}
                ]
                """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        when(oscarClient.getApplicationsTechniques()).thenReturn(responseEntity);

        // Mock les JsonNode
        JsonNode mockNode1 = mock(JsonNode.class);
        JsonNode mockNode2 = mock(JsonNode.class);
        JsonNode mockRoot = mock(JsonNode.class);

        when(objectMapper.readTree(jsonResponse)).thenReturn(mockRoot);
        when(mockRoot.iterator()).thenReturn(java.util.List.of(mockNode1, mockNode2).iterator());

        ApplicationTechnique app1 = new ApplicationTechnique();
        app1.setId(1);
        app1.setNom("App1");

        ApplicationTechnique app2 = new ApplicationTechnique();
        app2.setId(2);
        app2.setNom("App2");

        when(oscarBuilder.buildApplicationTechnique(mockNode1)).thenReturn(app1);
        when(oscarBuilder.buildApplicationTechnique(mockNode2)).thenReturn(app2);

        // Act
        List<ApplicationTechnique> result = oscarService.getApplicationsTechniques();

        // Assert
        assertThat(result).hasSize(2).containsExactly(app1, app2);
        verify(oscarClient).getApplicationsTechniques();
        verify(oscarBuilder, times(2)).buildApplicationTechnique(any());
    }

    @Test
    void getApplicationsTechniques_retourneListeVide() throws Exception {

        String invalidJson = "invalid json";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidJson, HttpStatus.OK);

        when(oscarClient.getApplicationsTechniques()).thenReturn(responseEntity);
        when(objectMapper.readTree(invalidJson))
                .thenThrow(new JacksonException("JSON invalide") {});

        List<ApplicationTechnique> result = oscarService.getApplicationsTechniques();

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindApplicationByName() throws Exception {

        String jsonResponse = "[{\"id\":4,\"nom\":\"test\"},{\"id\":234,\"nom\":\"test2\"}]";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        ObjectMapper realMapper = new ObjectMapper();
        when(objectMapper.readTree(jsonResponse)).thenReturn(realMapper.readTree(jsonResponse));

        ApplicationTechnique appArc = new ApplicationTechnique();
        appArc.setId(4);
        appArc.setNom("test");

        ApplicationTechnique appParcours = new ApplicationTechnique();
        appParcours.setId(234);
        appParcours.setNom("test2");

        when(oscarBuilder.buildApplicationTechnique(any()))
                .thenReturn(appArc)
                .thenReturn(appParcours);

        ApplicationTechnique result = oscarService.findApplicationByName("test");

        assertNotNull(result);
        assertEquals(4, result.getId());
        assertEquals("test", result.getNom());
    }

    @Test
    void findApplicationByName_retourneNull() throws Exception {

        String invalidJson = "invalid json";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidJson, HttpStatus.OK);
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        when(objectMapper.readTree(invalidJson)).thenThrow(new RuntimeException("JSON invalide"));

        ApplicationTechnique result = oscarService.findApplicationByName("test");

        assertNull(result);
    }

    @Test
    void findApplicationByName_aucunMatch() throws Exception {

        String jsonResponse = "[{\"id\":234,\"nom\":\"parcours\"},{\"id\":8,\"nom\":\"archimed\"}]";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        ObjectMapper realMapper = new ObjectMapper();
        when(objectMapper.readTree(jsonResponse)).thenReturn(realMapper.readTree(jsonResponse));

        ApplicationTechnique appParcours = new ApplicationTechnique();
        appParcours.setId(234);
        appParcours.setNom("parcours");

        when(oscarBuilder.buildApplicationTechnique(any())).thenReturn(appParcours);

        ApplicationTechnique result = oscarService.findApplicationByName("arc");

        assertNotNull(result);
        assertEquals(234, result.getId());
        assertEquals("parcours", result.getNom());
    }
}
