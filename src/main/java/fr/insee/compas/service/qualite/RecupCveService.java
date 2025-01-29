package fr.insee.compas.service.qualite;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.compas.model.Indicateur;
import fr.insee.compas.model.Source;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RecupCveService {

    public RecupCveService(TableFaitsRepository tableFaitsRepository, OscarService oscarService) {
        this.tableFaitsRepository = tableFaitsRepository;
        this.oscarService = oscarService;
    }

    private final TableFaitsRepository tableFaitsRepository;
    private final OscarService oscarService;

    @Value("${fr.insee.compas.gitlab.token:}")
    private String ACCESS_TOKEN;

    private static final String BASE_URL = "https://gitlab.insee.fr/api/v4";
    private static final String PROJECT_ID = "13644";
    private static final String DIRECTORY_PATH = "rapports";

    public void getCveInBdd() {
        HttpClient client = HttpClient.newHttpClient();
        List<Module> modules = oscarService.getModules();
        try {
            for (Module module : modules) {
                String encodedFilePath =
                        URLEncoder.encode(
                                "rapports/" + module.getId() + ".json", StandardCharsets.UTF_8);
                String fetchFileUrl =
                        String.format(
                                "%s/projects/%s/repository/files/%s/raw",
                                BASE_URL, PROJECT_ID, encodedFilePath);
                HttpRequest fetchFileRequest =
                        HttpRequest.newBuilder()
                                .uri(URI.create(fetchFileUrl))
                                .header("Private-Token", ACCESS_TOKEN)
                                .GET()
                                .build();
                HttpResponse<String> fetchFileResponse =
                        client.send(fetchFileRequest, HttpResponse.BodyHandlers.ofString());
                if (fetchFileResponse.statusCode() == 200) {
                    // Extraction des Cve du fichier json
                    Map<String, Integer> cveData = getCveFromJson(fetchFileResponse.body());
                    putCveInBdd(module, cveData);
                    log.debug("Processed {}: {}", module.getId(), cveData);
                } else {
                    log.error(
                            "Failed to fetch {}: {}",
                            module.getId(),
                            fetchFileResponse.statusCode());
                }
            }
        } catch (IOException | InterruptedException e) {
            log.error("An error occurred: {}", e.getMessage());
        }
    }

    private void putCveInBdd(Module module, Map<String, Integer> cveData) {
        for (Map.Entry<String, Integer> entry : cveData.entrySet()) {
            TableFaits fait = new TableFaits();
            fait.setIdModule(module.getId());
            fait.setIdApplication(module.getIdApplication());
            fait.setIdIndicateur(
                    switch (entry.getKey()) {
                        case "CRITICAL" -> Indicateur.CVE_CRITICAL.getValue();
                        case "HIGH" -> Indicateur.CVE_HIGH.getValue();
                        case "MEDIUM" -> Indicateur.CVE_MEDIUM.getValue();
                        case "LOW" -> Indicateur.CVE_LOW.getValue();
                        default ->
                                throw new IllegalStateException(
                                        "Unexpected value: " + entry.getKey());
                    });
            fait.setValeur(BigDecimal.valueOf(entry.getValue()));
            fait.setIdSource(Source.GITLAB.getValue());
            fait.setCommentaire("");
            fait.setDate(LocalDate.now());
            tableFaitsRepository.save(fait);
        }
    }

    /*
       Récupération des CVE à partir du json
    */
    public Map<String, Integer> getCveFromJson(String jsonFile) {

        Map<String, Integer> severityCounts = new HashMap<>();
        try {
            // Charger le fichier JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonFile);

            // Parcourir les résultats
            JsonNode resultsNode = rootNode.get("Results");
            if (resultsNode != null && resultsNode.isArray()) {
                Set<String> highs = new HashSet<>();
                Set<String> criticals = new HashSet<>();
                Set<String> mediums = new HashSet<>();
                Set<String> lows = new HashSet<>();
                for (JsonNode result : resultsNode) {
                    JsonNode vulnerabilitiesNode = result.get("Vulnerabilities");
                    if (vulnerabilitiesNode != null && vulnerabilitiesNode.isArray()) {
                        for (JsonNode vulnerability : vulnerabilitiesNode) {
                            // Extraire la criticité
                            String severity = vulnerability.get("Severity").asText();
                            // Incrémenter la bonne liste suivant la severite compteur pour cette
                            // criticité
                            switch (severity) {
                                case "HIGH":
                                    highs.add(vulnerability.get("VulnerabilityID").asText());
                                    break;
                                case "CRITICAL":
                                    criticals.add(vulnerability.get("VulnerabilityID").asText());
                                    break;
                                case "MEDIUM":
                                    mediums.add(vulnerability.get("VulnerabilityID").asText());
                                    break;
                                case "LOW":
                                    lows.add(vulnerability.get("VulnerabilityID").asText());
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
                severityCounts.put("CRITICAL", criticals.size());
                severityCounts.put("HIGH", highs.size());
                severityCounts.put("MEDIUM", mediums.size());
                severityCounts.put("LOW", lows.size());
            }

        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier JSON : {}", e.getMessage());
        }
        return severityCounts;
    }

    /*
       Récupération des noms des fichiers json contenus dans le répertoire.
       On retire les fichier html qui sont dans le même répertoire.
    */
    private List<String> fetchAllJsonFiles(HttpClient client, ObjectMapper objectMapper)
            throws IOException, InterruptedException {
        List<String> jsonFiles = new ArrayList<>();
        int page = 1;
        boolean hasMoreFiles = true;

        while (hasMoreFiles) {
            String encodedPath = URLEncoder.encode(DIRECTORY_PATH, StandardCharsets.UTF_8);
            String listFilesUrl =
                    String.format(
                            "%s/projects/%s/repository/tree?path=%s&recursive=true&per_page=100&page=%d",
                            BASE_URL, PROJECT_ID, encodedPath, page);

            HttpRequest listFilesRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(listFilesUrl))
                            .header("Private-Token", ACCESS_TOKEN)
                            .GET()
                            .build();

            HttpResponse<String> listFilesResponse =
                    client.send(listFilesRequest, HttpResponse.BodyHandlers.ofString());

            if (listFilesResponse.statusCode() == 200) {
                List<JsonNode> files =
                        objectMapper.readValue(
                                listFilesResponse.body(), new TypeReference<List<JsonNode>>() {});

                // Filtre les JSONS
                List<String> jsonFilesOnPage =
                        files.stream()
                                .filter(
                                        file ->
                                                file.get("type")
                                                        .asText()
                                                        .equals("blob")) // Only files (not
                                // directories)
                                .filter(
                                        file ->
                                                file.get("path")
                                                        .asText()
                                                        .endsWith(".json")) // Only JSON files
                                .map(file -> file.get("path").asText())
                                .toList();

                jsonFiles.addAll(jsonFilesOnPage);

                // Stop si il n'y a plus de fichier
                if (jsonFilesOnPage.isEmpty()) {
                    hasMoreFiles = false;
                } else {
                    page++;
                }
            } else {
                System.out.println(
                        "Failed to list files on page "
                                + page
                                + ": "
                                + listFilesResponse.statusCode());
                hasMoreFiles = false;
            }
        }

        return jsonFiles;
    }
}
