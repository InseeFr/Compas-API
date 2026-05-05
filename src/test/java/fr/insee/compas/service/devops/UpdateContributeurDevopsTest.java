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
import fr.insee.compas.util.observer.EventTypeObserver;
import fr.insee.compas.util.observer.IEventManager;

@ExtendWith(MockitoExtension.class)
class UpdateContributeurDevopsTest {

    @Mock private IGitlabService gitlabService;
    @Mock private GithubService githubService;
    @Mock private UtilsService utilsService;
    @Mock private SaveTFByIndicator saveTFByIndicator;
    @Mock private IEventManager manager;

    @InjectMocks private UpdateContributeurDevops updateContributeurDevops;

    private Module module;

    private Module buildModule(int id, int idApp, String url) {
        Module m = Module.builder().id(id).idApplication(idApp).build();
        m.setUrlCodeSource(url);
        return m;
    }

    private void verifySavedModule(int moduleId, BigDecimal value) {
        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(moduleId),
                        eq(100),
                        eq(IndicateurType.NBR_CONTRIBUTIONS_PROJET),
                        eq(value),
                        eq(SourceType.OSCAR));
    }

    private void verifySavedApplication(int appId, BigDecimal value) {
        verify(saveTFByIndicator)
                .saveByIndicator(
                        isNull(),
                        eq(appId),
                        eq(IndicateurType.NBR_CONTRIBUTIONS_PROJET),
                        eq(value),
                        eq(SourceType.OSCAR));
    }

    @BeforeEach
    void setup() {
        module = Module.builder().id(1).idApplication(100).build();
    }

    @Test
    void shouldDoNothingWhenModulesEmpty() {
        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList(), Map.of());

        verifyNoInteractions(saveTFByIndicator);
    }

    @Test
    void shouldDoNothingWhenModulesNull() {
        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), null, Map.of());

        verifyNoInteractions(saveTFByIndicator);
    }

    @Test
    void shouldSaveNRWhenUrlIsNull() {
        module.setUrlCodeSource(null);

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module), Map.of());

        verifySavedModule(1, BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode()));
    }

    @Test
    void shouldSaveNRWhenUrlIsEmpty() {
        module.setUrlCodeSource(DevopsConstantes.EMPTY);

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module), Map.of());

        verifySavedModule(1, BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode()));
    }

    @Test
    void shouldSaveSOWhenUrlIsSansObjet() {
        module.setUrlCodeSource(DevopsConstantes.SANS_OBJET);

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module), Map.of());

        verifySavedModule(1, BigDecimal.valueOf(IndicatorSpecialValue.SO.getCode()));
    }

    /** URL ni gitlab ni github ni SANS_OBJET → hors périmètre → SO. */
    @Test
    void shouldSaveSOWhenUrlIsOutOfScope() {
        module.setUrlCodeSource("https://bitbucket.org/org/repo");

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module), Map.of());

        verifySavedModule(1, BigDecimal.valueOf(IndicatorSpecialValue.SO.getCode()));
        verify(manager, atLeastOnce())
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
    }

    /** URL github malformée (moins de 2 segments après github.com/) → NR. */
    @Test
    void shouldSaveNRWhenGithubUrlMalformed() {
        module.setUrlCodeSource("https://github.com/org-only");

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module), Map.of());

        verifySavedModule(1, BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode()));
    }

    @Test
    void shouldCallGitlabService() {
        module.setUrlCodeSource("https://gitlab.insee.fr/group/project");

        when(utilsService.extractRepoPath(anyString())).thenReturn("group/project");
        when(gitlabService.getGitlabAuthorsForProject(anyString(), any(), any()))
                .thenReturn(Set.of(new AuthorsDto("test@mail.com", "s")));

        updateContributeurDevops.updateDevops(
                LocalDateTime.now().minusDays(1), LocalDateTime.now(), List.of(module), Map.of());

        verify(gitlabService).getGitlabAuthorsForProject(anyString(), any(), any());
    }

    @Test
    void shouldSaveAuthorCountWhenGitlabReturnsAuthors() {
        module.setUrlCodeSource("https://gitlab.insee.fr/group/project");

        when(utilsService.extractRepoPath(anyString())).thenReturn("group/project");
        when(gitlabService.getGitlabAuthorsForProject(anyString(), any(), any()))
                .thenReturn(
                        Set.of(
                                new AuthorsDto("a@insee.fr", "Alice"),
                                new AuthorsDto("b@insee.fr", "Bob")));

        updateContributeurDevops.updateDevops(
                LocalDateTime.now().minusDays(1), LocalDateTime.now(), List.of(module), Map.of());

        verifySavedModule(1, BigDecimal.valueOf(2));
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
    void shouldHandleIOExceptionFromGithubAndReturnNR() throws IOException {
        module.setUrlCodeSource("https://github.com/org/repo");

        when(githubService.getGithubAuthorsForRepo(anyString(), anyString(), any(), any()))
                .thenThrow(new IOException());

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module), Map.of());

        verifySavedModule(1, BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode()));
    }

    @Test
    void shouldAggregateDistinctAuthorsByApplication() throws IOException {
        Module module2 = buildModule(2, 100, "https://github.com/org/repo2");
        module.setUrlCodeSource("https://github.com/org/repo1");

        when(githubService.getGithubAuthorsForRepo(anyString(), anyString(), any(), any()))
                .thenReturn(
                        Set.of(
                                new AuthorsDto("a@mail.com", "s"),
                                new AuthorsDto("b@mail.com", "q")));

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module, module2), Map.of());

        verifySavedApplication(100, BigDecimal.valueOf(2));
    }

    /**
     * Deux modules de la même application, auteurs se chevauchant → seuls les emails distincts sont
     * comptés.
     */
    @Test
    void shouldDeduplicateAuthorsAcrossModulesForSameApplication() throws IOException {
        Module module2 = buildModule(2, 100, "https://github.com/org/repo2");
        module.setUrlCodeSource("https://github.com/org/repo1");

        // repo1 → a + b, repo2 → b + c : 3 distincts attendus
        when(githubService.getGithubAuthorsForRepo(eq("org"), eq("repo1"), any(), any()))
                .thenReturn(
                        Set.of(
                                new AuthorsDto("a@mail.com", "A"),
                                new AuthorsDto("b@mail.com", "B")));
        when(githubService.getGithubAuthorsForRepo(eq("org"), eq("repo2"), any(), any()))
                .thenReturn(
                        Set.of(
                                new AuthorsDto("b@mail.com", "B"),
                                new AuthorsDto("c@mail.com", "C")));

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module, module2), Map.of());

        verifySavedApplication(100, BigDecimal.valueOf(3));
    }

    /** Tous les modules d'une application sont SO → l'application doit recevoir SO. */
    @Test
    void shouldSaveSOForApplicationWhenAllModulesAreSO() {
        Module mod1 = buildModule(1, 200, DevopsConstantes.SANS_OBJET);
        Module mod2 = buildModule(2, 200, DevopsConstantes.SANS_OBJET);

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(mod1, mod2), Map.of());

        verifySavedApplication(200, BigDecimal.valueOf(IndicatorSpecialValue.SO.getCode()));
    }

    /** Modules d'une application avec NR (pas SO) et aucun auteur → l'application reçoit NR. */
    @Test
    void shouldSaveNRForApplicationWhenAllModulesAreNRWithoutAuthors() {
        Module mod1 = buildModule(1, 300, null); // NR
        Module mod2 = buildModule(2, 300, DevopsConstantes.EMPTY); // NR

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(mod1, mod2), Map.of());

        verifySavedApplication(300, BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode()));
    }

    /**
     * Mélange : un module SO et un module avec auteurs réels → l'application ne doit compter que
     * les auteurs réels (le SO est ignoré dans le décompte).
     */
    @Test
    void shouldIgnoreSOModulesWhenCountingAuthorsForApplication() throws IOException {
        Module modSO = buildModule(1, 400, DevopsConstantes.SANS_OBJET);
        Module modGithub = buildModule(2, 400, "https://github.com/org/repo");

        when(githubService.getGithubAuthorsForRepo(eq("org"), eq("repo"), any(), any()))
                .thenReturn(Set.of(new AuthorsDto("dev@mail.com", "Dev")));

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(modSO, modGithub), Map.of());

        verifySavedApplication(400, BigDecimal.valueOf(1));
    }

    /** Une exception dans saveModule ne doit pas stopper les autres sauvegardes. */
    @Test
    void shouldNotifyObserverWhenSaveModuleThrows() throws IOException {
        Module modOk = buildModule(2, 100, "https://github.com/org/repo-ok");
        module.setUrlCodeSource("https://github.com/org/repo-fail");

        when(githubService.getGithubAuthorsForRepo(eq("org"), eq("repo-fail"), any(), any()))
                .thenReturn(Set.of(new AuthorsDto("x@mail.com", "X")));
        when(githubService.getGithubAuthorsForRepo(eq("org"), eq("repo-ok"), any(), any()))
                .thenReturn(Set.of(new AuthorsDto("y@mail.com", "Y")));

        doThrow(new RuntimeException("DB indisponible"))
                .when(saveTFByIndicator)
                .saveByIndicator(eq(1), eq(100), any(), any(), any());

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module, modOk), Map.of());

        verify(manager, atLeastOnce())
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
        // Le module 2 doit quand même être sauvegardé
        verifySavedModule(2, BigDecimal.valueOf(1));
    }

    /** Une exception dans saveApplication déclenche le notifyObservers. */
    @Test
    void shouldNotifyObserverWhenSaveApplicationThrows() throws IOException {
        module.setUrlCodeSource("https://github.com/org/repo");

        when(githubService.getGithubAuthorsForRepo(anyString(), anyString(), any(), any()))
                .thenReturn(Set.of(new AuthorsDto("a@mail.com", "A")));

        doThrow(new RuntimeException("Contrainte DB"))
                .when(saveTFByIndicator)
                .saveByIndicator(isNull(), eq(100), any(), any(), any());

        updateContributeurDevops.updateDevops(
                LocalDateTime.now(), LocalDateTime.now(), List.of(module), Map.of());

        verify(manager, atLeastOnce())
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
    }
}
