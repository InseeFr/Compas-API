package fr.insee.compas.service.qualite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class CveServiceTest {

    private final RecupCveService cveService = new RecupCveService(null, null);

    @Test
    public void recupererCveFromJson() {
        // Chemin vers le fichier JSON dans les ressources
        String json10 = null;
        try {
            json10 = new String(Files.readAllBytes(Paths.get("src/test/resources/cve/10.json")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Appeler la méthode et récupérer les résultats
        Map<String, Integer> severityCounts10 = cveService.getCveFromJson(json10);
        // Vérifier les résultats
        assertEquals(
                11,
                severityCounts10.getOrDefault("CRITICAL", 0),
                "Le nombre de CVE CRITICAL est incorrect.");
        assertEquals(
                27,
                severityCounts10.getOrDefault("HIGH", 0),
                "Le nombre de CVE HIGH est incorrect.");
        assertEquals(
                26,
                severityCounts10.getOrDefault("MEDIUM", 0),
                "Le nombre de CVE MEDIUM est incorrect.");
        assertEquals(
                1, severityCounts10.getOrDefault("LOW", 0), "Le nombre de CVE LOW est incorrect.");

        // Chemin vers le fichier JSON dans les ressources
        String json220 = null;
        try {
            json220 = new String(Files.readAllBytes(Paths.get("src/test/resources/cve/220.json")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Appeler la méthode et récupérer les résultats
        Map<String, Integer> severityCounts = cveService.getCveFromJson(json220);
        // Vérifier les résultats
        assertEquals(
                0,
                severityCounts.getOrDefault("CRITICAL", 0),
                "Le nombre de CVE CRITICAL est incorrect.");
        assertEquals(
                3, severityCounts.getOrDefault("HIGH", 0), "Le nombre de CVE HIGH est incorrect.");
        assertEquals(
                5,
                severityCounts.getOrDefault("MEDIUM", 0),
                "Le nombre de CVE MEDIUM est incorrect.");
        assertEquals(
                1, severityCounts.getOrDefault("LOW", 0), "Le nombre de CVE LOW est incorrect.");
    }
}
