package fr.insee.compas.service;

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
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.compas.builder.OscarBuilder;
import fr.insee.compas.client.OscarClient;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;

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
        when(objectMapper.readTree(invalidJson))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

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
        when(objectMapper.readTree(invalidJson))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

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
        when(objectMapper.readTree(invalidJson))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

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
}
