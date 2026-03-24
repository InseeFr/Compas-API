package fr.insee.compas.service.gitservice;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import fr.insee.compas.dto.gitlab.TagsGitLabDto;

public interface IGitlabService {
    TagsGitLabDto getLatestTags();

    Map<String, String> getMarkdownIndicators();

    Set<String> getGitlabAuthorsForProject(
            String pathWithNamespace, LocalDateTime startDate, LocalDateTime endDate)
            throws IOException;
}
