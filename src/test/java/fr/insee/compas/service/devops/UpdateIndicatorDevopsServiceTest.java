package fr.insee.compas.service.devops;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.UtilsService;
import fr.insee.compas.service.gitservice.GithubService;
import fr.insee.compas.service.gitservice.GitlabService;
import fr.insee.compas.util.DevopsConstantes;
import fr.insee.compas.util.IndicatorSpecialValue;

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

    @Test
    void testUpdateNbrJourMep_branches() {
        // GIVEN
        Module moduleManuel = new Module();
        moduleManuel.setId(1);
        moduleManuel.setIdApplication(10);
        moduleManuel.setSourceCreation(DevopsConstantes.SAISIE_MANUELLE);

        Module moduleDev = new Module();
        moduleDev.setId(2);
        moduleDev.setIdApplication(10);
        moduleDev.setStatut(DevopsConstantes.EN_DEVELOPPEMENT);

        Module moduleSansDate = new Module();
        moduleSansDate.setId(3);
        moduleSansDate.setIdApplication(10);
        moduleSansDate.setStatut("EN_PRODUCTION");

        Module moduleAvecDate = new Module();
        moduleAvecDate.setId(4);
        moduleAvecDate.setIdApplication(10);
        moduleAvecDate.setStatut("EN_PRODUCTION");
        moduleAvecDate.setDateDerniereLivraisonEnProduction(LocalDate.now().minusDays(7));

        // WHEN
        when(oscarService.getModules())
                .thenReturn(List.of(moduleManuel, moduleDev, moduleSansDate, moduleAvecDate));
        when(oscarService.getApplications()).thenReturn(List.of(new Application(10, "AppTest")));

        service.miseAJourIndicateursDevopsEnBaseDeDonnes(
                LocalDateTime.now().minusDays(10), LocalDateTime.now());

        // THEN
        ArgumentCaptor<TableFaits> captor = ArgumentCaptor.forClass(TableFaits.class);
        verify(tableFaitsRepository, atLeastOnce()).save(captor.capture());

        List<TableFaits> faits = captor.getAllValues();
        assertTrue(
                faits.stream()
                        .anyMatch(
                                f ->
                                        f.getValeur().intValue()
                                                == IndicatorSpecialValue.NR.getCode()));
        assertTrue(
                faits.stream()
                        .anyMatch(
                                f ->
                                        f.getValeur().intValue()
                                                == IndicatorSpecialValue.SO.getCode()));
        assertTrue(faits.stream().anyMatch(f -> f.getValeur().intValue() > 0)); // cas avec date
    }

    @Test
    void testUpdateDeploymentCount_branches() {
        // GIVEN
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();

        // --- Cas 1 : saisie manuelle
        Module moduleManuel = new Module();
        moduleManuel.setId(1);
        moduleManuel.setIdApplication(10);
        moduleManuel.setSourceCreation(DevopsConstantes.SAISIE_MANUELLE);

        // --- Cas 2 : en développement
        Module moduleDev = new Module();
        moduleDev.setId(2);
        moduleDev.setIdApplication(10);
        moduleDev.setStatut(DevopsConstantes.EN_DEVELOPPEMENT);

        // --- Cas 3 : sans historique
        Module moduleSansHist = new Module();
        moduleSansHist.setId(3);
        moduleSansHist.setIdApplication(10);
        moduleSansHist.setStatut("EN_PRODUCTION");

        // --- Cas 4 : historique présent avec un déploiement valide et un invalide
        Module moduleAvecHist = new Module();
        moduleAvecHist.setId(4);
        moduleAvecHist.setIdApplication(10);
        moduleAvecHist.setStatut("EN_PRODUCTION");

        ModuleHistorique histValide = new ModuleHistorique();
        histValide.setAuteurOperation(DevopsConstantes.SERVICE);
        histValide.setDateOperation(LocalDateTime.now().minusDays(5));
        histValide.setOperation(DevopsConstantes.MODIFICATION);

        ModuleHistorique histInvalide = new ModuleHistorique();
        histInvalide.setAuteurOperation("autre_utilisateur");
        histInvalide.setDateOperation(LocalDateTime.now().minusDays(10));
        histInvalide.setOperation("CREATION");

        Map<String, List<ModuleHistorique>> historiqueMap = new HashMap<>();
        historiqueMap.put("4", List.of(histValide, histInvalide));

        when(oscarService.getModules())
                .thenReturn(List.of(moduleManuel, moduleDev, moduleSansHist, moduleAvecHist));
        when(oscarService.getApplications()).thenReturn(List.of(new Application(10, "AppTest")));
        when(oscarService.getModulesHistorique()).thenReturn(historiqueMap);

        // WHEN
        service.miseAJourIndicateursDevopsEnBaseDeDonnes(start, end);

        // THEN
        ArgumentCaptor<TableFaits> captor = ArgumentCaptor.forClass(TableFaits.class);
        verify(tableFaitsRepository, atLeastOnce()).save(captor.capture());
        List<TableFaits> faits = captor.getAllValues();

        // Vérifie qu’on a bien des valeurs NR, SO et numériques
        boolean hasNR =
                faits.stream()
                        .anyMatch(
                                f ->
                                        f.getValeur().intValue()
                                                == IndicatorSpecialValue.NR.getCode());
        boolean hasSO =
                faits.stream()
                        .anyMatch(
                                f ->
                                        f.getValeur().intValue()
                                                == IndicatorSpecialValue.SO.getCode());
        boolean hasPositive = faits.stream().anyMatch(f -> f.getValeur().intValue() > 0);

        assertTrue(hasNR, "Devrait avoir un indicateur NR");
        assertTrue(hasSO, "Devrait avoir un indicateur SO");
        assertTrue(hasPositive, "Devrait avoir au moins un déploiement valide compté");
    }

    @Test
    void testNormalizeDates_givenVariousInputs_whenInvoked_thenBehavesAsExpected()
            throws Exception {
        // --- Given
        var method =
                UpdateIndicatorDevopsService.class.getDeclaredMethod(
                        "normalizeDates", LocalDateTime.class, LocalDateTime.class);
        method.setAccessible(true);

        // === Cas 1 : start ou end null ===
        // Given
        LocalDateTime startNull = null;
        LocalDateTime endNull = null;

        // When
        LocalDateTime[] resultDefault =
                (LocalDateTime[]) method.invoke(service, startNull, endNull);

        // Then
        assertTrue(
                resultDefault[0].isBefore(resultDefault[1]),
                "La date de début par défaut doit être avant la date de fin");
        assertTrue(
                resultDefault[0].isAfter(LocalDateTime.now().minusMonths(2)),
                "La date de début par défaut doit être environ à un mois dans le passé");

        // === Cas 2 : start > end ===
        // Given
        LocalDateTime startAfter = LocalDateTime.now();
        LocalDateTime endBefore = LocalDateTime.now().minusDays(1);

        // When / Then
        Exception thrown =
                assertThrows(
                        Exception.class,
                        () -> method.invoke(service, startAfter, endBefore),
                        "Une exception devait être levée quand start > end");

        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        assertTrue(thrown.getCause().getMessage().contains("startDate > endDate"));

        // === Cas 3 : start < end ===
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now();

        // When
        LocalDateTime[] resultNormal = (LocalDateTime[]) method.invoke(service, start, end);

        // Then
        assertEquals(start, resultNormal[0], "La date de début doit être inchangée");
        assertEquals(end, resultNormal[1], "La date de fin doit être inchangée");
    }

    @Test
    void testGetUniqueAuthorCount_givenVariousUrls_whenInvoked_thenReturnExpectedValues()
            throws Exception {
        // --- Given
        var method =
                UpdateIndicatorDevopsService.class.getDeclaredMethod(
                        "getUniqueAuthorCount",
                        String.class,
                        LocalDateTime.class,
                        LocalDateTime.class);
        method.setAccessible(true);

        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now();

        // --- Cas 1 : GitLab valide
        String gitlabUrl = "https://gitlab.insee.fr/project/repo";
        when(utilsService.extractRepoPath(gitlabUrl)).thenReturn("project/repo");
        when(gitlabService.getGitlabAuthorsForProject(anyString(), eq(start), eq(end)))
                .thenReturn(Set.of("author1", "author2"));

        // When
        int resultGitLab = (int) method.invoke(service, gitlabUrl, start, end);

        // Then
        assertEquals(2, resultGitLab, "GitLab: doit retourner le nombre d’auteurs");

        // --- Cas 2 : GitHub valide
        String githubUrl = "https://github.com/org/repo";
        when(githubService.getGithubAuthorsForRepo("org", "repo", start, end))
                .thenReturn(Set.of("authorA", "authorB", "authorC"));

        int resultGitHub = (int) method.invoke(service, githubUrl, start, end);

        assertEquals(3, resultGitHub, "GitHub: doit retourner le nombre d’auteurs");

        // --- Cas 3 : GitHub invalide (parts < 2)
        String githubBadUrl = "https://github.com/orgOnly";
        int resultGitHubBad = (int) method.invoke(service, githubBadUrl, start, end);

        assertEquals(
                IndicatorSpecialValue.NR.getCode(), resultGitHubBad, "GitHub: URL invalide -> NR");

        // --- Cas 4 : URL inconnue
        String unknownUrl = "https://bitbucket.org/project/repo";
        int resultUnknown = (int) method.invoke(service, unknownUrl, start, end);

        assertEquals(IndicatorSpecialValue.SO.getCode(), resultUnknown, "URL inconnue -> SO");

        // --- Cas 5 : Exception IO
        String gitlabUrlException = "https://gitlab.insee.fr/project/fail";
        when(utilsService.extractRepoPath(gitlabUrlException)).thenReturn("project/fail");
        when(gitlabService.getGitlabAuthorsForProject(anyString(), eq(start), eq(end)))
                .thenThrow(new IOException("Network error"));

        int resultException = (int) method.invoke(service, gitlabUrlException, start, end);

        assertEquals(IndicatorSpecialValue.NR.getCode(), resultException, "Exception IO -> NR");
    }
}
