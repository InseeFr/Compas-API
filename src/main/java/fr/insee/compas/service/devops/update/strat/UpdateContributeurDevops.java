package fr.insee.compas.service.devops.update.strat;

import static fr.insee.compas.util.DevopsConstantes.normalizeDates;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import fr.insee.compas.dto.devops.AuthorResultDto;
import fr.insee.compas.dto.devops.AuthorsDto;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;
import fr.insee.compas.service.UtilsService;
import fr.insee.compas.service.gitservice.GithubService;
import fr.insee.compas.service.gitservice.IGitlabService;
import fr.insee.compas.util.DevopsConstantes;
import fr.insee.compas.util.IndicatorSpecialValue;
import fr.insee.compas.util.SaveTFByIndicator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("Contributeur")
@RequiredArgsConstructor
@Slf4j
public class UpdateContributeurDevops implements IUpdateDevopsStrategy {

    private final IGitlabService gitlabService;
    private final GithubService githubService;
    private final UtilsService utilsService;
    private final SaveTFByIndicator saveTFByIndicator;

    @Override
    public void updateDevops(
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<Module> modules,
            Map<String, List<ModuleHistorique>> moduleHistoriques) {
        LocalDateTime[] dates = normalizeDates(startDate, endDate);
        if (modules == null || modules.isEmpty()) return;
        Map<Module, AuthorResultDto> moduleAuthorResultDtoMap =
                resultModuleIdAuthors(modules, dates);

        Map<Integer, Integer> authorsByAppId = resultAuthorsByAppId(moduleAuthorResultDtoMap);
        for (Map.Entry<Module, AuthorResultDto> entry : moduleAuthorResultDtoMap.entrySet()) {
            int value =
                    entry.getValue().isSpecial()
                            ? entry.getValue().specialValue().getCode()
                            : entry.getValue().authors().size();
            saveModule(entry.getKey(), BigDecimal.valueOf(value));
        }

        for (Map.Entry<Integer, Integer> entry : authorsByAppId.entrySet()) {
            this.saveApplication(entry.getKey(), BigDecimal.valueOf(entry.getValue()));
        }
    }

    private void saveModule(Module module, BigDecimal value) {
        saveTFByIndicator.saveByIndicator(
                module.getId(),
                module.getIdApplication(),
                IndicateurType.NBR_CONTRIBUTIONS_PROJET,
                value,
                SourceType.OSCAR);
    }

    private void saveApplication(Integer id, BigDecimal value) {
        saveTFByIndicator.saveByIndicator(
                null, id, IndicateurType.NBR_CONTRIBUTIONS_PROJET, value, SourceType.OSCAR);
    }

    private Map<Integer, Integer> resultAuthorsByAppId(
            Map<Module, AuthorResultDto> moduleAuthorResultDtoMap) {
        return moduleAuthorResultDtoMap.entrySet().stream()
                .collect(
                        Collectors.groupingBy(
                                entry -> entry.getKey().getIdApplication(),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        entries -> {
                                            List<AuthorsDto> authors =
                                                    entries.stream()
                                                            .filter(e -> !e.getValue().isSpecial())
                                                            .flatMap(
                                                                    e ->
                                                                            e
                                                                                    .getValue()
                                                                                    .authors()
                                                                                    .stream())
                                                            .toList();

                                            if (authors.isEmpty()) {
                                                boolean allSO =
                                                        entries.stream()
                                                                .allMatch(
                                                                        e ->
                                                                                e.getValue()
                                                                                                .specialValue()
                                                                                        == IndicatorSpecialValue
                                                                                                .SO);

                                                return allSO
                                                        ? IndicatorSpecialValue.SO.getCode()
                                                        : IndicatorSpecialValue.NR.getCode();
                                            }

                                            return (int)
                                                    authors.stream()
                                                            .map(AuthorsDto::email)
                                                            .distinct()
                                                            .count();
                                        })));
    }

    private Map<Module, AuthorResultDto> resultModuleIdAuthors(
            List<Module> modules, LocalDateTime[] dates) {
        return modules.parallelStream()
                .collect(
                        Collectors.toConcurrentMap(
                                module -> module,
                                module -> {
                                    String url = module.getUrlCodeSource();
                                    if (url == null || DevopsConstantes.EMPTY.equals(url)) {
                                        return AuthorResultDto.special(IndicatorSpecialValue.NR);
                                    }
                                    if (DevopsConstantes.SANS_OBJET.equals(url)) {
                                        return AuthorResultDto.special(IndicatorSpecialValue.SO);
                                    }
                                    return resolveAuthors(url, dates[0], dates[1]);
                                }));
    }

    private AuthorResultDto resolveAuthors(
            String sourceUrl, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return searchFromUrlSource(sourceUrl, startDate, endDate);
        } catch (IllegalArgumentException e) {
            log.warn("Scan ignoré (hors périmètre) pour l'URL {} : {}", sourceUrl, e.getMessage());
            return AuthorResultDto.special(IndicatorSpecialValue.SO);

        } catch (IOException e) {
            log.error("Erreur réseau lors récupération auteurs: {}", sourceUrl, e);
            return AuthorResultDto.special(IndicatorSpecialValue.NR);
        }
    }

    private AuthorResultDto searchFromUrlSource(
            String sourceUrl, LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        switch (sourceUrl) {
            case String url when url.contains("gitlab.insee.fr") -> {
                String repoPath = utilsService.extractRepoPath(url);
                String encodedPath = UriUtils.encodePathSegment(repoPath, StandardCharsets.UTF_8);
                return AuthorResultDto.of(
                        gitlabService.getGitlabAuthorsForProject(encodedPath, startDate, endDate));
            }
            case String url when url.contains("github.com") -> {
                String[] parts = url.split("github.com/")[1].split("/");
                if (parts.length < 2) {
                    return AuthorResultDto.special(IndicatorSpecialValue.NR);
                }
                return AuthorResultDto.of(
                        githubService.getGithubAuthorsForRepo(
                                parts[0], parts[1], startDate, endDate));
            }
            default -> {
                return AuthorResultDto.special(IndicatorSpecialValue.SO);
            }
        }
    }
}
