package fr.insee.compas.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;

class OscarBuilderTest {

    private OscarBuilder oscarBuilder;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        oscarBuilder = new OscarBuilder();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testBuildModule_validJsonNode() throws Exception {
        // Arrange
        String json =
                """
                    {
                        "id": 1,
                        "nom": "TestModule",
                        "dateDerniereLivraison": 1672531200000,
                        "projectKeySonar": "key-sonar",
                        "applicationTechnique": {
                            "application": {
                                "id": 2,
                                "sndi": {"nom": "TestSndi"},
                                "domaineSndi": {"nom": "TestDomaineSndi"},
                                "domaineFonctionnel": {"nom": "TestDomaineFonctionnel"}
                            }
                        }
                    }
                """;
        JsonNode moduleNode = objectMapper.readTree(json);

        // Act
        Module module = oscarBuilder.buildModule(moduleNode);

        // Assert
        assertNotNull(module);
        assertEquals(1, module.getId());
        assertEquals("TestModule", module.getModName());
        assertEquals("TestModule", module.getAppName());
        assertEquals(LocalDate.of(2023, 1, 1), module.getDateDerniereLivraison());
        assertEquals("key-sonar", module.getKeySonar());
        assertEquals(2, module.getIdApplication());
        assertEquals("TestSndi", module.getSndi());
        assertEquals("TestDomaineSndi", module.getDomaineSndi());
        assertEquals("TestDomaineFonctionnel", module.getDomaineFonctionnel());
    }

    @Test
    void testBuildModule_missingDateDerniereLivraison() throws Exception {
        // Arrange
        String json =
                """
                    {
                        "id": 1,
                        "nom": "TestModule",
                        "projectKeySonar": "key-sonar",
                        "applicationTechnique": {
                            "application": {
                                "id": 2,
                                "sndi": {"nom": "TestSndi"},
                                "domaineSndi": {"nom": "TestDomaineSndi"},
                                "domaineFonctionnel": {"nom": "TestDomaineFonctionnel"}
                            }
                        }
                    }
                """;
        JsonNode moduleNode = objectMapper.readTree(json);

        // Act
        Module module = oscarBuilder.buildModule(moduleNode);

        // Assert
        assertNotNull(module);
        assertNull(module.getDateDerniereLivraison());
    }

    @Test
    void testBuildApplication_validJsonNode() throws Exception {
        // Arrange
        String json =
                """
                    {
                        "id": 1,
                        "nom": "TestApplication",
                        "sndi": {"nom": "TestSndi"},
                        "domaineFonctionnel": {"nom": "TestDomaineFonctionnel"},
                        "domaineSndi": {"nom": "TestDomaineSndi"}
                    }
                """;
        JsonNode applicationNode = objectMapper.readTree(json);

        // Act
        Application application = oscarBuilder.buildApplication(applicationNode);

        // Assert
        assertNotNull(application);
        assertEquals(1, application.getIdApplication());
        assertEquals("TestApplication", application.getAppName());
        assertEquals("TestSndi", application.getSndi());
        assertEquals("TestDomaineFonctionnel", application.getDomaineFonctionnel());
        assertEquals("TestDomaineSndi", application.getDomaineSndi());
    }
}
