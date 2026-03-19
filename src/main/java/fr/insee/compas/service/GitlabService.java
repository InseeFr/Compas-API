package fr.insee.compas.service;

import static fr.insee.compas.util.GitLabMarkdownConstantes.INDICATEURS_MD;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.compas.dto.MarkdownResultGitlabDto;
import fr.insee.compas.exception.GitLabException;
import fr.insee.compas.util.DevopsConstantes;

import lombok.extern.slf4j.Slf4j;

/**
 * Service pour interagir avec l'API GitLab. Permet de récupérer les commits d'un projet et
 * d'extraire les auteurs uniques.
 */
@Service
@Slf4j
public class GitlabService {

    @Value("${fr.insee.compas.gitlab.token:}")
    private String gitlabToken;

    private HttpHeaders headers;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GitlabService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** Initialise les headers HTTP avec le token GitLab au démarrage du service. */
    @PostConstruct
    private void initHeaders() {
        headers = new HttpHeaders();
        headers.set("Private-Token", gitlabToken);
    }

    /**
     * Récupère tous les markdowns des indicateurs
     *
     * @return un ensemble une map reliant les indicateurs avec le markdown respectifs
     * @throws RestClientException si une erreur HTTP survient
     */
    public Map<String, String> getMarkdownIndicators() {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return INDICATEURS_MD.stream()
                .map(
                        indicateur -> {
                            log.debug("Récupération du markdown {}", indicateur);
                            URI url =
                                    URI.create(
                                            String.format(
                                                    "%s/projects/dsi%%2Fcompas%%2Fdocumentation%%2Fcompas-wiki/wikis/doc%%2F%s",
                                                    DevopsConstantes.GITLAB_API_URL, indicateur));
                            try {
                                ResponseEntity<MarkdownResultGitlabDto> response =
                                        restTemplate.exchange(
                                                url,
                                                HttpMethod.GET,
                                                entity,
                                                MarkdownResultGitlabDto.class);

                                if (response.getStatusCode() == HttpStatus.NOT_FOUND
                                        || response.getBody() == null) {
                                    log.warn("Aucune doc trouvée pour {}", indicateur);
                                    return null;
                                }

                                return Map.entry(indicateur, response.getBody().getContent());

                            } catch (HttpClientErrorException e) {
                                throw new GitLabException(
                                        "Erreur lors de la récupération du markdown " + indicateur,
                                        e);
                            }
                        })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Récupère tous les auteurs uniques d'un projet GitLab entre deux dates.
     *
     * <p>Les auteurs sont filtrés pour exclure les bots, les emails noreply et autres non valides
     * définis dans {@link DevopsConstantes}.
     *
     * @param pathWithNamespace chemin complet du projet GitLab (ex : "namespace/projet")
     * @param startDate date de début pour le filtrage des commits
     * @param endDate date de fin pour le filtrage des commits
     * @return un ensemble d'adresses email des auteurs valides
     * @throws IOException si une erreur HTTP ou JSON survient
     */
    public Set<String> getGitlabAuthorsForProject(
            String pathWithNamespace, LocalDateTime startDate, LocalDateTime endDate)
            throws IOException {

        Set<String> uniqueAuthors = new HashSet<>();
        String since = encodeDate(startDate);
        String until = encodeDate(endDate);

        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            ResponseEntity<String> response =
                    fetchCommitsPage(pathWithNamespace, since, until, page);

            JsonNode commitsPage = objectMapper.readTree(response.getBody());

            if (commitsPage == null || !commitsPage.isArray() || commitsPage.isEmpty()) {
                break;
            }

            uniqueAuthors.addAll(extractAuthorsFromCommits(commitsPage));

            String nextPage = response.getHeaders().getFirst(DevopsConstantes.FIELD_X_NEXT_PAGE);
            if (nextPage == null || nextPage.isBlank()) {
                hasMore = false;
            } else {
                page = Integer.parseInt(nextPage);
            }
        }

        return uniqueAuthors;
    }

    /**
     * Récupère une page de commits pour un projet GitLab.
     *
     * <p>La méthode effectue la requête HTTP avec le token privé et retourne la réponse brute pour
     * permettre l'accès aux headers de pagination.
     *
     * @param pathWithNamespace chemin complet du projet GitLab
     * @param since date de début encodée en ISO-8601
     * @param until date de fin encodée en ISO-8601
     * @param page numéro de la page à récupérer
     * @return ResponseEntity contenant le corps JSON et les headers HTTP
     * @throws IOException si une erreur HTTP survient
     */
    private ResponseEntity<String> fetchCommitsPage(
            String pathWithNamespace, String since, String until, int page) throws IOException {

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        URI url =
                URI.create(
                        String.format(
                                "%s/projects/%s/repository/commits?since=%s&until=%s&all=true&per_page=%d&page=%d",
                                DevopsConstantes.GITLAB_API_URL,
                                pathWithNamespace,
                                since,
                                until,
                                DevopsConstantes.MAX_COMMITS_PER_PAGE,
                                page));

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Aucun commit trouvé pour {}", pathWithNamespace);
            } else if (response.getStatusCode() != HttpStatus.OK) {
                throw new IOException(
                        "Erreur récupération commits GitLab : " + response.getStatusCode());
            }

            return response;

        } catch (HttpClientErrorException e) {
            throw new IOException(
                    "Erreur HTTP lors de la récupération des commits GitLab : " + e.getStatusCode(),
                    e);
        }
    }

    /**
     * Extrait les adresses email des auteurs valides depuis une page de commits JSON.
     *
     * @param commits JsonNode contenant les commits d'une page
     * @return ensemble d'adresses email filtrées
     */
    private Set<String> extractAuthorsFromCommits(JsonNode commits) {
        return StreamSupport.stream(commits.spliterator(), false)
                .map(
                        commit ->
                                Map.entry(
                                        commit.path(DevopsConstantes.FIELD_AUTHOR_EMAIL)
                                                .asText("")
                                                .toLowerCase(),
                                        commit.path(DevopsConstantes.FIELD_AUTHOR_NAME)
                                                .asText("")
                                                .toLowerCase()))
                .filter(entry -> isValidAuthor(entry.getKey(), entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Encode une date au format ISO pour l'API GitLab.
     *
     * @param dateTime date à encoder
     * @return chaîne de caractères encodée en UTF-8
     */
    private String encodeDate(LocalDateTime dateTime) {
        return URLEncoder.encode(
                dateTime.atZone(ZoneId.systemDefault()).toInstant().toString(),
                StandardCharsets.UTF_8);
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
