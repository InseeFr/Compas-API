package fr.insee.compas.service.gitservice;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.compas.util.DevopsConstantes;

import lombok.extern.slf4j.Slf4j;

/**
 * Service pour interagir avec l'API GitHub GraphQL. Permet de récupérer les commits d'un projet et
 * d'extraire les auteurs uniques.
 */
@Service
@Slf4j
public class GithubService {

    /**
     * Token GitHub pour l'authentification aux requêtes GraphQL. Injecté depuis la configuration
     * Spring.
     */
    @Value("${fr.insee.compas.github.token:}")
    private String githubToken;

    private final RestTemplate restTemplate;
    private HttpHeaders headers;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final List<String> ALLOWED_ORGANIZATIONS = List.of("InseeFr", "InseeFrLab");

    /**
     * Constructeur du service GitHub.
     *
     * @param restTemplate instance de RestTemplate pour les requêtes HTTP
     */
    public GithubService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** Initialise les headers HTTP avec le token Github au démarrage du service. */
    @PostConstruct
    private void initHeaders() {
        headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * Récupère tous les auteurs uniques d'un dépôt GitHub entre deux dates.
     *
     * @param owner propriétaire du dépôt
     * @param repo nom du dépôt
     * @param start date de début pour le filtrage des commits
     * @param end date de fin pour le filtrage des commits
     * @return un ensemble d'adresses email d'auteurs valides
     * @throws IOException en cas d'erreur lors de la récupération ou du parsing des commits
     */
    public Set<String> getGithubAuthorsForRepo(
            String owner, String repo, LocalDateTime start, LocalDateTime end) throws IOException {

        boolean isAllowed =
                ALLOWED_ORGANIZATIONS.stream()
                        .anyMatch(allowedOrg -> allowedOrg.equalsIgnoreCase(owner));

        if (!isAllowed) {
            log.warn("Tentative de scan bloquée pour l'organisation externe : {}", owner);
            throw new IllegalArgumentException(
                    String.format(
                            "L'analyse GitHub est restreinte aux organisations %s. Organisation"
                                    + " demandée : '%s'",
                            ALLOWED_ORGANIZATIONS, owner));
        }

        String graphqlQuery = buildGraphqlQuery(owner, repo, start, end);
        String responseBody = executeGraphqlQuery(graphqlQuery);
        return parseAuthorsFromResponse(responseBody);
    }

    /**
     * Construit la requête GraphQL pour récupérer les commits d'un dépôt.
     *
     * @param owner propriétaire du dépôt
     * @param repo nom du dépôt
     * @param start date de début pour le filtrage des commits
     * @param end date de fin pour le filtrage des commits
     * @return chaîne JSON représentant la requête GraphQL
     */
    private String buildGraphqlQuery(
            String owner, String repo, LocalDateTime start, LocalDateTime end) {
        String since = start.atOffset(ZoneOffset.UTC).toString();
        String until = end.atOffset(ZoneOffset.UTC).toString();

        return "{ \"query\": \"query { repository(owner: \\\""
                + owner
                + "\\\", name: \\\""
                + repo
                + "\\\") { refs(refPrefix: \\\"refs/heads/\\\", first: "
                + DevopsConstantes.MAX_BRANCHES
                + ") { nodes { name target { ... on Commit { history(first: "
                + DevopsConstantes.MAX_COMMITS_PER_BRANCH
                + ", since: \\\""
                + since
                + "\\\", until: \\\""
                + until
                + "\\\") { nodes { author { email name } } } } } } } } }\" }";
    }

    /**
     * Exécute une requête GraphQL vers l'API GitHub.
     *
     * @param graphqlQuery chaîne JSON représentant la requête GraphQL
     * @return réponse brute du serveur GitHub
     * @throws IOException si le serveur renvoie une erreur HTTP
     */
    private String executeGraphqlQuery(String graphqlQuery) throws IOException {
        HttpEntity<String> entity = new HttpEntity<>(graphqlQuery, headers);
        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        DevopsConstantes.GITHUB_GRAPHQL_API_URL, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IOException("Erreur GitHub GraphQL : " + response.getStatusCode());
        }

        return response.getBody();
    }

    /**
     * Parse les auteurs uniques à partir de la réponse GraphQL.
     *
     * @param responseBody réponse JSON brute de l'API GitHub
     * @return ensemble d'emails d'auteurs valides
     * @throws IOException en cas d'erreur lors du parsing JSON
     */
    private Set<String> parseAuthorsFromResponse(String responseBody) throws IOException {
        JsonNode refs =
                objectMapper
                        .readTree(responseBody)
                        .path(DevopsConstantes.FIELD_DATA)
                        .path(DevopsConstantes.FIELD_REPOSITORY)
                        .path(DevopsConstantes.FIELD_REFS)
                        .path(DevopsConstantes.FIELD_NODES);

        return StreamSupport.stream(refs.spliterator(), false)
                .flatMap(
                        branchNode ->
                                StreamSupport.stream(
                                        branchNode
                                                .path(DevopsConstantes.FIELD_TARGET)
                                                .path(DevopsConstantes.FIELD_HISTORY)
                                                .path(DevopsConstantes.FIELD_NODES)
                                                .spliterator(),
                                        false))
                .map(commit -> commit.path(DevopsConstantes.FIELD_AUTHOR))
                .map(
                        authorNode ->
                                Map.entry(
                                        authorNode
                                                .path(DevopsConstantes.FIELD_EMAIL)
                                                .asText("")
                                                .toLowerCase(),
                                        authorNode
                                                .path(DevopsConstantes.FIELD_NAME)
                                                .asText("")
                                                .toLowerCase()))
                .filter(entry -> isValidAuthor(entry.getKey(), entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Vérifie si un auteur est valide (exclut les bots, noreply, maintenance, etc.).
     *
     * @param email email de l'auteur
     * @param name nom de l'auteur
     * @return true si l'auteur est valide, false sinon
     */
    private boolean isValidAuthor(String email, String name) {
        return DevopsConstantes.INVALID_EMAIL_KEYWORDS.stream().noneMatch(email::contains)
                && DevopsConstantes.INVALID_NAME_KEYWORDS.stream().noneMatch(name::contains);
    }
}
