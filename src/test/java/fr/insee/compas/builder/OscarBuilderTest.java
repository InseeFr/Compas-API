package fr.insee.compas.builder;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

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
                    "nomTechnique": "TechModule",
                    "sourceCreation": "TestSource",
                    "urlCodeSource": "http://example.com/repo",
                    "statut": "Actif",
                    "typeLivrable": "Librairie",
                    "dateDerniereLivraisonEnProduction": 1672531200000,
                    "projectKeySonar": "key-sonar",
                    "applicationTechnique": {
                        "nom": "AppTechniqueNom",
                        "application": {
                            "id": 2,
                            "nom" : "AppTest",
                            "sndi": {"nom": "TestSndi"},
                            "domaineSndi": {"nom": "TestDomaineSndi"},
                            "domaineFonctionnel": {"nom": "TestDomaineFonctionnel"}
                        }
                    }
                }
                """;
        JsonNode moduleNode = objectMapper.readTree(json);

        Module module = oscarBuilder.buildModule(moduleNode);

        assertNotNull(module);
        assertEquals(1, module.getId());
        assertEquals("TestModule", module.getModName());
        assertEquals("TechModule", module.getNomTechnique());
        assertEquals("TestSource", module.getSourceCreation());
        assertEquals("http://example.com/repo", module.getUrlCodeSource());
        assertEquals("Actif", module.getStatut());
        assertEquals("Librairie", module.getTypeLivrable());
        assertEquals("AppTest", module.getAppName());
        assertEquals(LocalDate.of(2023, 1, 1), module.getDateDerniereLivraisonEnProduction());
        assertEquals("key-sonar", module.getKeySonar());
        assertEquals(2, module.getIdApplication());
        assertEquals("TestSndi", module.getSndi());
        assertEquals("TestDomaineSndi", module.getDomaineSndi());
        assertEquals("TestDomaineFonctionnel", module.getDomaineFonctionnel());
    }

    @Test
    void testBuildModule_missingDateDerniereLivraisonEnProduction() throws Exception {
        // Arrange
        String json =
                """
                    {
                                    "id": 1,
                                    "nom": "TestModule",
                                    "nomTechnique": "TechModule",
                                    "sourceCreation": "TestSource",
                                    "urlCodeSource": "http://example.com/repo",
                                    "statut": "Actif",
                                    "typeLivrable": "Librairie",
                                    "projectKeySonar": "key-sonar",
                                    "applicationTechnique": {
                                        "nom": "AppTechniqueNom",
                                        "application": {
                                            "id": 2,
                                            "nom": "TestApplication",
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
        assertNull(module.getDateDerniereLivraisonEnProduction());
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
