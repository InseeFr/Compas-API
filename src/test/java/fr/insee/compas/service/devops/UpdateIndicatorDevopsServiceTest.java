package fr.insee.compas.service.devops;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.GithubService;
import fr.insee.compas.service.GitlabService;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.UtilsService;

class UpdateIndicatorDevopsServiceTest {

    private TableFaitsRepository tableFaitsRepository;
    private OscarService oscarService;
    private GitlabService gitlabService;
    private GithubService githubService;
    private UtilsService utilsService;

    private UpdateIndicatorDevopsService service;

    @BeforeEach
    void setUp() {
        tableFaitsRepository = mock(TableFaitsRepository.class);
        oscarService = mock(OscarService.class);
        gitlabService = mock(GitlabService.class);
        githubService = mock(GithubService.class);
        utilsService = mock(UtilsService.class);

        service =
                new UpdateIndicatorDevopsService(
                        tableFaitsRepository,
                        oscarService,
                        gitlabService,
                        githubService,
                        utilsService);
    }

    @Test
    void testMiseAJourIndicateursDevopsEnBaseDeDonnes() throws IOException {
        // GIVEN
        Module module = mock(Module.class);
        Application application = mock(Application.class);

        when(module.getId()).thenReturn(1);
        when(module.getIdApplication()).thenReturn(100);
        when(module.getStatut()).thenReturn("EN_PRODUCTION");
        when(module.getDateDerniereLivraisonEnProduction())
                .thenReturn(LocalDate.now().minusDays(5));
        when(module.getUrlCodeSource()).thenReturn("https://gitlab.insee.fr/project/repo");

        when(application.getIdApplication()).thenReturn(100);

        when(oscarService.getModules()).thenReturn(List.of(module));
        when(oscarService.getApplications()).thenReturn(List.of(application));
        when(oscarService.getModulesHistorique()).thenReturn(new HashMap<>());
        // Mock GitLab authors
        when(utilsService.extractRepoPath(anyString())).thenReturn("project/repo");
        when(gitlabService.getGitlabAuthorsForProject(anyString(), any(), any()))
                .thenReturn(Set.of("author1", "author2"));

        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now();

        // WHEN
        service.miseAJourIndicateursDevopsEnBaseDeDonnes(start, end);

        // THEN
        ArgumentCaptor<TableFaits> captor = ArgumentCaptor.forClass(TableFaits.class);
        verify(tableFaitsRepository, atLeastOnce()).save(captor.capture());

        List<TableFaits> saved = captor.getAllValues();

        // On vérifie qu’on a sauvegardé des indicateurs pour le module et pour l’application
        boolean hasNbrJourMep =
                saved.stream()
                        .anyMatch(
                                f -> f.getIdIndicateur() == IndicateurType.NBR_JOUR_MEP.getValue());
        boolean hasDeploymentCount =
                saved.stream()
                        .anyMatch(
                                f ->
                                        f.getIdIndicateur()
                                                == IndicateurType.DEPLOYMENT_COUNT.getValue());
        boolean hasContribCount =
                saved.stream()
                        .anyMatch(
                                f ->
                                        f.getIdIndicateur()
                                                == IndicateurType.NBR_CONTRIBUTIONS_PROJET
                                                        .getValue());

        assertTrue(hasNbrJourMep);
        assertTrue(hasDeploymentCount);
        assertTrue(hasContribCount);
    }
}
