package fr.insee.compas.service.gitservice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import fr.insee.compas.dto.devops.AuthorsDto;
import fr.insee.compas.dto.gitlab.TagsGitLabDto;

public interface IGitlabService {
    TagsGitLabDto getLatestTags();

    Map<String, String> getMarkdownIndicators();

    Set<AuthorsDto> getGitlabAuthorsForProject(
            String pathWithNamespace, LocalDateTime startDate, LocalDateTime endDate);
}
