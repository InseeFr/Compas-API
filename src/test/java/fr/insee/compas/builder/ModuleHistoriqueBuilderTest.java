package fr.insee.compas.builder;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insee.compas.model.oscar.ModuleHistorique;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class ModuleHistoriqueBuilderTest {

    private OscarBuilder oscarBuilder;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        oscarBuilder = new OscarBuilder();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testBuildModuleHistorique_validJsonNode() throws Exception {
        // Given
        String json =
                """
                   {
                             "idModuleHistorique": 1,
                             "auteurOperation": "TestAuthor",
                             "statut": "TestStatus",
                             "idModule": 123,
                             "dateOperation": [2025, 3, 26, 14, 30, 45, 500],
                             "operation": "TestOperation"
                           }
                """;
        JsonNode moduleHistoriqueNode = objectMapper.readTree(json);

        // When
        ModuleHistorique moduleHistorique =
                oscarBuilder.buildModuleHistorique(moduleHistoriqueNode);

        // Then
        assertNotNull(moduleHistorique);
        assertEquals(1, moduleHistorique.getIdModuleHistorique());
        assertEquals("TestAuthor", moduleHistorique.getAuteurOperation());
        assertEquals("TestStatus", moduleHistorique.getStatut());
        assertEquals(
                LocalDateTime.of(2025, 3, 26, 14, 30, 45, 500),
                moduleHistorique.getDateOperation());
        assertEquals("TestOperation", moduleHistorique.getOperation());
    }

    @Test
    void testBuildModuleHistorique_missingDateOperation() throws Exception {
        // Given
        String json =
                """
                    {
                        "idModuleHistorique": 1,
                        "idModule":123,
                        "auteurOperation": "TestAuthor",
                        "statut": "TestStatus",
                        "operation": "TestOperation",
                        "dateOperation": []
                    }
                """;
        JsonNode moduleHistoriqueNode = objectMapper.readTree(json);

        // When
        ModuleHistorique moduleHistorique =
                oscarBuilder.buildModuleHistorique(moduleHistoriqueNode);

        // Then
        assertNotNull(moduleHistorique);
        assertNull(
                moduleHistorique.getDateOperation()); // No dateOperation provided, should be null
    }

    @Test
    void testBuildModuleHistorique_incorrectDateOperationFormat() throws Exception {
        // Given
        String json =
                """
                    {
                        "idModuleHistorique": 1,
                        "idModule":123,
                        "auteurOperation": "TestAuthor",
                        "statut": "TestStatus",
                        "dateOperation": [2025, 3, 26, 14, 30],
                        "operation": "TestOperation"
                    }
                """;
        JsonNode moduleHistoriqueNode = objectMapper.readTree(json);

        // When
        ModuleHistorique moduleHistorique =
                oscarBuilder.buildModuleHistorique(moduleHistoriqueNode);

        // Then
        assertNotNull(moduleHistorique);
        assertNull(moduleHistorique.getDateOperation()); // Invalid date array, should be null
    }

    @Test
    void testBuildModuleHistorique_missingStatut() throws Exception {
        // Given
        String json =
                """
                    {
                        "idModuleHistorique": 1,
                        "idModule":123,
                        "statut": "",
                        "auteurOperation": "TestAuthor",
                        "dateOperation": [2025, 3, 26, 14, 30, 45, 500],
                        "operation": "TestOperation"
                    }
                """;
        JsonNode moduleHistoriqueNode = objectMapper.readTree(json);

        // When
        ModuleHistorique moduleHistorique =
                oscarBuilder.buildModuleHistorique(moduleHistoriqueNode);

        // Then
        assertNotNull(moduleHistorique);
        assertEquals("", moduleHistorique.getStatut());
    }

    @Test
    void testBuildModuleHistorique_missingOperation() throws Exception {
        // Given
        String json =
                """
                    {
                        "idModuleHistorique": 1,
                        "idModule":123,
                        "auteurOperation": "TestAuthor",
                        "statut": "TestStatus",
                        "dateOperation": [2025, 3, 26, 14, 30, 45, 500],
                        "operation":""
                    }
                """;
        JsonNode moduleHistoriqueNode = objectMapper.readTree(json);

        // When
        ModuleHistorique moduleHistorique =
                oscarBuilder.buildModuleHistorique(moduleHistoriqueNode);

        // Then
        assertNotNull(moduleHistorique);
        assertEquals("", moduleHistorique.getOperation()); // Operation not provided, should be null
    }
}
