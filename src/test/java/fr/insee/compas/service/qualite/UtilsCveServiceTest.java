package fr.insee.compas.service.qualite;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.compas.model.compas.IndicateurType;

class UtilsCveServiceTest {

    private UtilsCveService utilsService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        utilsService = new UtilsCveService();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetIndicateurApplication() {

        assertEquals(
                IndicateurType.CVE_CRITICAL_APPLI.getValue(),
                utilsService.getIndicateurApplication("CRITICAL"));
        assertEquals(
                IndicateurType.CVE_HIGH_APPLI.getValue(),
                utilsService.getIndicateurApplication("HIGH"));
        assertEquals(
                IndicateurType.CVE_MEDIUM_APPLI.getValue(),
                utilsService.getIndicateurApplication("MEDIUM"));
        assertEquals(
                IndicateurType.CVE_LOW_APPLI.getValue(),
                utilsService.getIndicateurApplication("LOW"));
    }

    @Test
    void testGetIndicateurModule() {

        assertEquals(
                IndicateurType.CVE_CRITICAL.getValue(),
                utilsService.getIndicateurModule("CRITICAL"));
        assertEquals(IndicateurType.CVE_HIGH.getValue(), utilsService.getIndicateurModule("HIGH"));
        assertEquals(
                IndicateurType.CVE_MEDIUM.getValue(), utilsService.getIndicateurModule("MEDIUM"));
        assertEquals(IndicateurType.CVE_LOW.getValue(), utilsService.getIndicateurModule("LOW"));
    }

    @Test
    void testGetIndicateurModuleWithInvalidSeverity() {

        String invalidSeverity = "UNKNOWN";

        Exception exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> {
                            utilsService.getIndicateurModule(invalidSeverity);
                        });

        assertEquals("Unexpected value: " + invalidSeverity, exception.getMessage());
    }

    @Test
    void testGetIndicateurApplicationWithInvalidSeverity() {

        String invalidSeverity = "UNKNOWN";

        Exception exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> {
                            utilsService.getIndicateurApplication(invalidSeverity);
                        });

        assertEquals("Unexpected value: " + invalidSeverity, exception.getMessage());
    }

    @Test
    void testClassifyVulnerabilities() throws Exception {
        // JSON simulant les vulnérabilités
        String json =
                """
                [
                    {"Severity": "High", "VulnerabilityID": "CVE-1234"},
                    {"Severity": "Medium", "VulnerabilityID": "CVE-5678"},
                    {"Severity": "High", "VulnerabilityID": "CVE-91011"}
                ]
                """;

        JsonNode vulnerabilitiesNode = objectMapper.readTree(json);
        Map<String, Set<String>> classifiedCves = new HashMap<>();
        classifiedCves.put("High", new HashSet<>());
        classifiedCves.put("Medium", new HashSet<>());

        utilsService.classifyVulnerabilities(vulnerabilitiesNode, classifiedCves);

        // Vérifications
        assertEquals(2, classifiedCves.get("High").size());
        assertTrue(classifiedCves.get("High").contains("CVE-1234"));
        assertTrue(classifiedCves.get("High").contains("CVE-91011"));

        assertEquals(1, classifiedCves.get("Medium").size());
        assertTrue(classifiedCves.get("Medium").contains("CVE-5678"));
    }

    @Test
    void testClassifyVulnerabilitiesWithEmptyInput() throws Exception {
        JsonNode vulnerabilitiesNode = objectMapper.readTree("[]");
        Map<String, Set<String>> classifiedCves = new HashMap<>();
        classifiedCves.put("High", new HashSet<>());
        classifiedCves.put("Medium", new HashSet<>());

        utilsService.classifyVulnerabilities(vulnerabilitiesNode, classifiedCves);

        // Vérifications
        assertTrue(classifiedCves.get("High").isEmpty());
        assertTrue(classifiedCves.get("Medium").isEmpty());
    }

    @Test
    void concatInventaireCve() {
        Map<String, Set<String>> inventaireApplication = new HashMap<>();
        inventaireApplication.put(
                "CRITICAL", new HashSet<>(Arrays.asList("CVE-2024-001", "CVE-2024-002")));
        inventaireApplication.put("HIGH", new HashSet<>(Arrays.asList("CVE-2024-003")));

        Map<String, Set<String>> inventaireModule = new HashMap<>();
        inventaireModule.put("CRITICAL", new HashSet<>(Arrays.asList("CVE-2024-004")));
        inventaireModule.put("HIGH", new HashSet<>(Arrays.asList("CVE-2024-003", "CVE-2024-006")));

        Map<String, Set<String>> resultat =
                utilsService.concatInventaireCve(inventaireApplication, inventaireModule);
        assertEquals(3, resultat.get("CRITICAL").size());
        assertEquals(2, resultat.get("HIGH").size());
    }

    @Test
    void testParseResults() throws Exception {
        // JSON simulant une liste de résultats avec différentes vulnérabilités
        String json =
                """
                    [
                        {
                            "Vulnerabilities": [
                                {"Severity": "CRITICAL", "VulnerabilityID": "CVE-1234"},
                                {"Severity": "HIGH", "VulnerabilityID": "CVE-5678"}
                            ]
                        },
                        {
                            "Vulnerabilities": [
                                {"Severity": "MEDIUM", "VulnerabilityID": "CVE-9012"},
                                {"Severity": "LOW", "VulnerabilityID": "CVE-3456"},
                                {"Severity": "HIGH", "VulnerabilityID": "CVE-7890"}
                            ]
                        }
                    ]
                """;

        // Conversion du JSON en JsonNode
        JsonNode resultsNode = objectMapper.readTree(json);

        // Appel de la méthode à tester
        Map<String, Set<String>> result = utilsService.parseResults(resultsNode);

        // Vérifications
        assertEquals(Set.of("CVE-1234"), result.get("CRITICAL"));
        assertEquals(Set.of("CVE-5678", "CVE-7890"), result.get("HIGH"));
        assertEquals(Set.of("CVE-9012"), result.get("MEDIUM"));
        assertEquals(Set.of("CVE-3456"), result.get("LOW"));
    }

    @Test
    void testParseResultsWithEmptyVulnerabilities() throws Exception {
        String json = "[{\"Vulnerabilities\": []}]";
        JsonNode resultsNode = objectMapper.readTree(json);

        Map<String, Set<String>> result = utilsService.parseResults(resultsNode);

        assertTrue(result.get("CRITICAL").isEmpty());
        assertTrue(result.get("HIGH").isEmpty());
        assertTrue(result.get("MEDIUM").isEmpty());
        assertTrue(result.get("LOW").isEmpty());
    }

    @Test
    void testParseResultsWithNullVulnerabilities() throws Exception {
        String json = "[{}]"; // Pas de clé "Vulnerabilities"
        JsonNode resultsNode = objectMapper.readTree(json);

        Map<String, Set<String>> result = utilsService.parseResults(resultsNode);

        assertTrue(result.get("CRITICAL").isEmpty());
        assertTrue(result.get("HIGH").isEmpty());
        assertTrue(result.get("MEDIUM").isEmpty());
        assertTrue(result.get("LOW").isEmpty());
    }
}
