package fr.insee.compas.service.gitservice;

import static fr.insee.compas.util.GitLabConstantes.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import fr.insee.compas.dto.devops.AuthorsDto;
import fr.insee.compas.dto.gitlab.MarkdownResultGitlabDto;
import fr.insee.compas.dto.gitlab.TagsGitLabDto;
import fr.insee.compas.exception.GitLabException;
import fr.insee.compas.util.DevopsConstantes;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/**
 * Service pour interagir avec l'API GitLab. Permet de récupérer les commits d'un projet et
 * d'extraire les auteurs uniques.
 */
@Service
@Slf4j
public class GitlabService implements IGitlabService {

    @Value("${fr.insee.compas.gitlab.token:}")
    private String gitlabToken;

    private HttpHeaders headers;

    private final RestTemplate restTemplate;

    public GitlabService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** Initialise les headers HTTP avec le token GitLab au démarrage du service. */
    @PostConstruct
    private void initHeaders() {
        headers = new HttpHeaders();
        headers.set("Private-Token", gitlabToken);
    }

    public TagsGitLabDto getLatestTags() {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        log.info("Appels à l'API GitLab pour récupérer les tags");
        URI urlApiTag =
                URI.create(
                        String.format(
                                "%s/projects/%d/repository/tags?order_by=updated&sort=desc&per_page=1",
                                DevopsConstantes.GITLAB_API_URL, ID_PROJET_COMPAS_API));
        URI urlIhmTag =
                URI.create(
                        String.format(
                                "%s/projects/%d/repository/tags?order_by=updated&sort=desc&per_page=1",
                                DevopsConstantes.GITLAB_API_URL, ID_PROJET_COMPAS_UI));
        try {
            ResponseEntity<List<TagsGitLabDto.TagApi>> responseApiTag =
                    restTemplate.exchange(
                            urlApiTag,
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<>() {});

            ResponseEntity<List<TagsGitLabDto.TagIhm>> responseIhmTag =
                    restTemplate.exchange(
                            urlIhmTag,
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<>() {});
            if (responseApiTag.getStatusCode() == HttpStatus.NOT_FOUND
                    || responseApiTag.getBody() == null
                    || responseApiTag.getBody().isEmpty()
                    || responseIhmTag.getStatusCode() == HttpStatus.NOT_FOUND
                    || responseIhmTag.getBody() == null
                    || responseIhmTag.getBody().isEmpty()) {
                log.warn("Aucune tag trouvée pour API ou IHM");
                return null;
            }
            return new TagsGitLabDto(
                    responseApiTag.getBody().getFirst(), responseIhmTag.getBody().getFirst());
        } catch (HttpStatusCodeException e) {
            log.error("Erreur lors de la récupération des tags pour API ou IHM");
            throw new GitLabException("Erreur lors de la récupération des tags", e);
        }
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
     */
    public Set<AuthorsDto> getGitlabAuthorsForProject(
            String pathWithNamespace, LocalDateTime startDate, LocalDateTime endDate) {

        String since = encodeDate(startDate);
        String until = encodeDate(endDate);

        ResponseEntity<List<AuthorsDto>> firstResponse =
                fetchCommitsPage(pathWithNamespace, since, until, 1);

        if (firstResponse.getBody() == null || firstResponse.getBody().isEmpty()) {
            return Set.of();
        }

        String totalPagesHeader =
                firstResponse.getHeaders().getFirst(DevopsConstantes.FIELD_X_TOTAL_PAGE);
        int totalPages =
                (totalPagesHeader != null && !totalPagesHeader.isBlank())
                        ? Integer.parseInt(totalPagesHeader)
                        : 1;

        Stream<AuthorsDto> firstPage = firstResponse.getBody().stream();

        Stream<AuthorsDto> remainingPages =
                totalPages > 1
                        ? IntStream.rangeClosed(2, totalPages)
                                .parallel()
                                .mapToObj(
                                        page ->
                                                fetchCommitsPage(
                                                        pathWithNamespace, since, until, page))
                                .filter(response -> response.getBody() != null)
                                .flatMap(response -> response.getBody().stream())
                        : Stream.empty();

        return Stream.concat(firstPage, remainingPages)
                .collect(
                        Collectors.toMap(
                                AuthorsDto::email, Function.identity(), (existing, k) -> existing))
                .values()
                .stream()
                .collect(Collectors.toUnmodifiableSet());
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
     */
    private ResponseEntity<List<AuthorsDto>> fetchCommitsPage(
            String pathWithNamespace, String since, String until, int page) {

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
            ResponseEntity<List<JsonNode>> response =
                    restTemplate.exchange(
                            url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

            if (response.getBody() == null) {
                log.warn("Aucun commit trouvé pour {}", pathWithNamespace);
                return ResponseEntity.ok(List.of());
            }
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new GitLabException(
                        "Erreur récupération commits GitLab : " + response.getStatusCode());
            }

            List<AuthorsDto> filtered =
                    response.getBody().stream()
                            .map(
                                    node ->
                                            new AuthorsDto(
                                                    node.path("committer_email")
                                                            .asString("")
                                                            .toLowerCase(),
                                                    node.path("committer_name")
                                                            .asString("")
                                                            .toLowerCase()))
                            .filter(author -> isValidAuthor(author.email(), author.name()))
                            .toList();

            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(filtered);

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Projet GitLab introuvable (404) : {}", pathWithNamespace);
            return ResponseEntity.ok(List.of());

        } catch (HttpClientErrorException e) {
            throw new GitLabException(
                    "Erreur HTTP lors de la récupération des commits GitLab : " + e.getStatusCode(),
                    e);
        }
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
