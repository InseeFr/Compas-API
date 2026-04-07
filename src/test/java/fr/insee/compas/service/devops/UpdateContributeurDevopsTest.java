package fr.insee.compas.service.devops;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.dto.devops.AuthorsDto;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.UtilsService;
import fr.insee.compas.service.devops.update.strat.UpdateContributeurDevops;
import fr.insee.compas.service.gitservice.GithubService;
import fr.insee.compas.service.gitservice.IGitlabService;
import fr.insee.compas.util.DevopsConstantes;
import fr.insee.compas.util.IndicatorSpecialValue;
import fr.insee.compas.util.SaveTFByIndicator;

@ExtendWith(MockitoExtension.class)
class UpdateContributeurDevopsTest {

    @Mock private IGitlabService gitlabService;

    @Mock private GithubService githubService;

    @Mock private UtilsService utilsService;

    @Mock private SaveTFByIndicator saveTFByIndicator;

    @InjectMocks private UpdateContributeurDevops updateContributeurDevops;

    private fr.insee.compas.model.oscar.Module module;

    @BeforeEach
    void setup() {
        module = fr.insee.compas.model.oscar.Module.builder().id(1).idApplication(100).build();
    }

    @Test
    void shouldDoNothingWhenModulesEmpty() {
        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList(), Map.of());

        verifyNoInteractions(saveTFByIndicator);
    }

    @Test
    void shouldSaveNRWhenUrlIsNull() {
        module.setUrlCodeSource(null);

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module), Map.of());

        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(1),
                        eq(100),
                        eq(IndicateurType.NBR_CONTRIBUTIONS_PROJET),
                        eq(BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode())),
                        eq(SourceType.OSCAR));
    }

    @Test
    void shouldSaveSOWhenUrlIsSansObjet() {
        module.setUrlCodeSource(DevopsConstantes.SANS_OBJET);

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module), Map.of());

        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(1),
                        eq(100),
                        eq(IndicateurType.NBR_CONTRIBUTIONS_PROJET),
                        eq(BigDecimal.valueOf(IndicatorSpecialValue.SO.getCode())),
                        eq(SourceType.OSCAR));
    }

    @Test
    void shouldCallGitlabService() throws IOException {
        module.setUrlCodeSource("https://gitlab.insee.fr/group/project");

        when(utilsService.extractRepoPath(anyString())).thenReturn("group/project");
        when(gitlabService.getGitlabAuthorsForProject(anyString(), any(), any()))
                .thenReturn(Set.of(new AuthorsDto("test@mail.com", "s")));

        updateContributeurDevops.updateDevops(
                LocalDateTime.now().minusDays(1), LocalDateTime.now(), List.of(module), Map.of());

        verify(gitlabService).getGitlabAuthorsForProject(anyString(), any(), any());
    }

    @Test
    void shouldCallGithubService() throws IOException {
        module.setUrlCodeSource("https://github.com/org/repo");

        when(githubService.getGithubAuthorsForRepo(anyString(), anyString(), any(), any()))
                .thenReturn(Set.of(new AuthorsDto("test@mail.com", "s")));

        updateContributeurDevops.updateDevops(
                LocalDateTime.now().minusDays(1), LocalDateTime.now(), List.of(module), Map.of());

        verify(githubService).getGithubAuthorsForRepo(eq("org"), eq("repo"), any(), any());
    }

    @Test
    void shouldHandleIOExceptionAndReturnNR() throws IOException {
        module.setUrlCodeSource("https://github.com/org/repo");

        when(githubService.getGithubAuthorsForRepo(anyString(), anyString(), any(), any()))
                .thenThrow(new IOException());

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module), Map.of());

        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(1),
                        eq(100),
                        eq(IndicateurType.NBR_CONTRIBUTIONS_PROJET),
                        eq(BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode())),
                        eq(SourceType.OSCAR));
    }

    @Test
    void shouldAggregateDistinctAuthorsByApplication() throws IOException {
        Module module2 =
                fr.insee.compas.model.oscar.Module.builder()
                        .id(2)
                        .idApplication(100)
                        .urlCodeSource("https://github.com/org/repo2")
                        .build();
        module2.setUrlCodeSource("https://github.com/org/repo2");

        module.setUrlCodeSource("https://github.com/org/repo1");

        when(githubService.getGithubAuthorsForRepo(anyString(), anyString(), any(), any()))
                .thenReturn(
                        Set.of(
                                new AuthorsDto("a@mail.com", "s"),
                                new AuthorsDto("b@mail.com", "q")));

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module, module2), Map.of());

        verify(saveTFByIndicator)
                .saveByIndicator(
                        isNull(),
                        eq(100),
                        eq(IndicateurType.NBR_CONTRIBUTIONS_PROJET),
                        eq(BigDecimal.valueOf(2)), // distinct authors
                        eq(SourceType.OSCAR));
    }
}
