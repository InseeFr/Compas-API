package fr.insee.compas.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.oscar.Module;

@Service
public class GitlabService {

    @Value("${fr.insee.compas.gitlab.token:}")
    private String token;

    private HttpClient httpClient = HttpClient.newHttpClient();

    private static final String BASE_URL = "https://gitlab.insee.fr/api/v4";
    private static final String PROJECT_ID = "13644";

    public GitlabService() {
        // Constructeur vide intentionnellement pour des raisons de compatibilité
    }

    public String getJson(Module module) throws IOException, InterruptedException {
        String encodedFilePath =
                URLEncoder.encode("rapports/" + module.getId() + ".json", StandardCharsets.UTF_8);
        String fetchFileUrl =
                String.format(
                        "%s/projects/%s/repository/files/%s/raw",
                        BASE_URL, PROJECT_ID, encodedFilePath);

        HttpRequest fetchFileRequest =
                HttpRequest.newBuilder()
                        .uri(URI.create(fetchFileUrl))
                        .header("Private-Token", token)
                        .GET()
                        .build();

        HttpResponse<String> fetchFileResponse =
                httpClient.send(fetchFileRequest, HttpResponse.BodyHandlers.ofString());

        if (fetchFileResponse.statusCode() == 200) {
            return fetchFileResponse.body();
        }
        return "";
    }
}
