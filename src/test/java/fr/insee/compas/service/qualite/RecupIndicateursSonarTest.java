package fr.insee.compas.service.qualite;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.sonar.Component;
import fr.insee.compas.model.sonar.Measure;
import fr.insee.compas.model.sonar.RecuperationMeasures;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.SonarService;
import fr.insee.compas.util.observer.EventTypeObserver;
import fr.insee.compas.util.observer.IEventManager;

@ExtendWith(MockitoExtension.class)
class RecupIndicateursSonarTest {

    @Mock private OscarService oscarService;
    @Mock private SonarService sonarService;
    @Mock private TableFaitsRepository tableFaitsRepository;
    @Mock private IEventManager eventManager;

    @InjectMocks private RecuperationIndicateurSonarService recupIndicateursSonarService;

    private Module module;
    private LocalDate date;

    private RecuperationMeasures buildMeasures(String metric, String value) {
        RecuperationMeasures rm = new RecuperationMeasures();
        Component c =
                Component.builder()
                        .measures(new ArrayList<>(List.of(new Measure(metric, value))))
                        .build();
        rm.setComponent(c);
        return rm;
    }

    private Module buildModule(int id, int idApp, String name, String keySonar) {
        return Module.builder()
                .id(id)
                .idApplication(idApp)
                .modName(name)
                .keySonar(keySonar)
                .build();
    }

    private Application buildApplication(int id, String name) {
        Application app = new Application();
        app.setIdApplication(id);
        app.setAppName(name);
        return app;
    }

    @BeforeEach
    void setUp() {
        module = new Module();
        module.setId(1);
        module.setIdApplication(100);
        module.setKeySonar("sonar-key");
        date = LocalDate.now();
    }

    @Test
    void testPutIndicateurSonarInBdd_Success() {
        RecuperationMeasures rm = buildMeasures("lines_to_cover", "10");

        boolean result =
                recupIndicateursSonarService.putIndicateurSonarInBdd(module, null, rm, date);

        assertTrue(result);
        verify(tableFaitsRepository, times(1)).save(any(TableFaits.class));
    }

    @Test
    void testPutIndicateurSonarInBdd_Fail_NoMeasure() {
        RecuperationMeasures rm = buildMeasures("metrique_inconnue", "10");

        boolean result =
                recupIndicateursSonarService.putIndicateurSonarInBdd(module, null, rm, date);

        assertFalse(result);
        verify(tableFaitsRepository, never()).save(any(TableFaits.class));
    }

    /** Branche module == null : sauvegarde rattachée à l'application uniquement. */
    @Test
    void testPutIndicateurSonarInBdd_ApplicationBranch() {
        Application application = buildApplication(42, "mon-appli");
        RecuperationMeasures rm = buildMeasures("lines_to_cover", "500");

        boolean result =
                recupIndicateursSonarService.putIndicateurSonarInBdd(null, application, rm, date);

        assertTrue(result);
        verify(tableFaitsRepository, times(1))
                .save(argThat(tf -> tf.getIdModule() == null && tf.getIdApplication() == 42));
    }

    @Test
    void testPutIndicateursSonarModule_NominalAvecGitlab() {
        Module mod = buildModule(488, 10, "module", "keySonarValide");
        when(oscarService.getModules()).thenReturn(List.of(mod));

        RecuperationMeasures mesures = buildMeasures("lines_to_cover", "1000");
        when(sonarService.getDataFromSonarAPIMeasures("keySonarValide", "gitlab", "module"))
                .thenReturn(mesures);

        Map<String, RecuperationMeasures> result =
                recupIndicateursSonarService.putIndicateursSonarModule();

        assertTrue(result.containsKey("keySonarValide"));
        verify(tableFaitsRepository, times(1)).save(any(TableFaits.class));
        // github ne doit pas être appelé puisque gitlab a réussi
        verify(sonarService, never())
                .getDataFromSonarAPIMeasures("keySonarValide", "github", "module");
    }

    /** Gitlab échoue (null), github prend le relais. */
    @Test
    void testPutIndicateursSonarModule_FallbackGitlabVersGithub() {
        Module mod = buildModule(1, 10, "module-gh", "keySonarGithub");
        when(oscarService.getModules()).thenReturn(List.of(mod));

        when(sonarService.getDataFromSonarAPIMeasures("keySonarGithub", "gitlab", "module-gh"))
                .thenReturn(null);

        RecuperationMeasures mesuresGithub = buildMeasures("lines_to_cover", "800");
        when(sonarService.getDataFromSonarAPIMeasures("keySonarGithub", "github", "module-gh"))
                .thenReturn(mesuresGithub);

        Map<String, RecuperationMeasures> result =
                recupIndicateursSonarService.putIndicateursSonarModule();

        assertTrue(result.containsKey("keySonarGithub"));
        verify(tableFaitsRepository, times(1)).save(any(TableFaits.class));
    }

    /** Gitlab retourne des mesures vides, github prend le relais. */
    @Test
    void testPutIndicateursSonarModule_FallbackGitlabMesuresVides() {
        Module mod = buildModule(2, 10, "module-vide", "keySonarVide");
        when(oscarService.getModules()).thenReturn(List.of(mod));

        RecuperationMeasures mesuresVides = new RecuperationMeasures();
        mesuresVides.setComponent(Component.builder().measures(Collections.emptyList()).build());
        when(sonarService.getDataFromSonarAPIMeasures("keySonarVide", "gitlab", "module-vide"))
                .thenReturn(mesuresVides);

        RecuperationMeasures mesuresGithub = buildMeasures("lines_to_cover", "300");
        when(sonarService.getDataFromSonarAPIMeasures("keySonarVide", "github", "module-vide"))
                .thenReturn(mesuresGithub);

        Map<String, RecuperationMeasures> result =
                recupIndicateursSonarService.putIndicateursSonarModule();

        assertTrue(result.containsKey("keySonarVide"));
        verify(tableFaitsRepository, times(1)).save(any(TableFaits.class));
    }

    @Test
    void testPutIndicateursSonarModule_ModuleSansObjet() {
        Module moduleSO = buildModule(5, 10, "module-so", "Sans objet");
        when(oscarService.getModules()).thenReturn(List.of(moduleSO));

        recupIndicateursSonarService.putIndicateursSonarModule();

        // Doit sauvegarder -1 pour NBR_LIGNE et ne pas appeler sonar
        verify(tableFaitsRepository, times(1)).save(argThat(tf -> tf.getValeur().intValue() == -1));
        verify(sonarService, never()).getDataFromSonarAPIMeasures(any(), any(), any());
    }

    /** Modules dont la keySonar est null, vide ou "null" : ignorés silencieusement. */
    @Test
    void testPutIndicateursSonarModule_KeySonarNullOuVide() {
        Module moduleKeyNull = buildModule(1, 10, "mod-null", null);
        Module moduleKeyVide = buildModule(2, 10, "mod-vide", "");
        Module moduleKeyString = buildModule(3, 10, "mod-string", "null");

        when(oscarService.getModules())
                .thenReturn(List.of(moduleKeyNull, moduleKeyVide, moduleKeyString));

        Map<String, RecuperationMeasures> result =
                recupIndicateursSonarService.putIndicateursSonarModule();

        assertTrue(result.isEmpty());
        verify(sonarService, never()).getDataFromSonarAPIMeasures(any(), any(), any());
        verify(tableFaitsRepository, never()).save(any(TableFaits.class));
    }

    /** Une exception dans le traitement d'un module ne doit pas bloquer les autres. */
    @Test
    void testPutIndicateursSonarModule_ExceptionSurUnModule() {
        Module moduleBug = buildModule(1, 10, "module-bug", "keySonarBug");
        Module moduleOk = buildModule(2, 20, "module-ok", "keySonarOk");
        when(oscarService.getModules()).thenReturn(List.of(moduleBug, moduleOk));

        when(sonarService.getDataFromSonarAPIMeasures("keySonarBug", "gitlab", "module-bug"))
                .thenThrow(new RuntimeException("Erreur réseau simulée"));

        RecuperationMeasures mesuresOk = buildMeasures("lines_to_cover", "200");
        when(sonarService.getDataFromSonarAPIMeasures("keySonarOk", "gitlab", "module-ok"))
                .thenReturn(mesuresOk);

        Map<String, RecuperationMeasures> result =
                recupIndicateursSonarService.putIndicateursSonarModule();

        // Le module OK est bien traité malgré l'exception du premier
        assertTrue(result.containsKey("keySonarOk"));
        assertFalse(result.containsKey("keySonarBug"));
        verify(eventManager, atLeastOnce())
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
    }

    @Test
    void testPutIndicateursSonarModule_SansAnalyse_NotifieObservateur() {
        Module mod = buildModule(1, 10, "mod-sans-analyse", "keySansAnalyse");
        when(oscarService.getModules()).thenReturn(List.of(mod));

        when(sonarService.getDataFromSonarAPIMeasures(
                        "keySansAnalyse", "gitlab", "mod-sans-analyse"))
                .thenReturn(null);
        when(sonarService.getDataFromSonarAPIMeasures(
                        "keySansAnalyse", "github", "mod-sans-analyse"))
                .thenReturn(null);

        recupIndicateursSonarService.putIndicateursSonarModule();

        verify(eventManager, times(1))
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
        verify(tableFaitsRepository, never()).save(any(TableFaits.class));
    }

    @Test
    void testPutIndicateursSonarApplication_Nominal() {
        Application app = buildApplication(1, "app-test");
        Set<String> keys = Set.of("keySonarA");

        when(oscarService.mapApplicationsToKeySonars()).thenReturn(Map.of(app, keys));

        RecuperationMeasures mesures = buildMeasures("lines_to_cover", "400");
        Map<String, RecuperationMeasures> analyseModule = Map.of("keySonarA", mesures);

        recupIndicateursSonarService.putIndicateursSonarApplication(analyseModule);

        verify(tableFaitsRepository, times(1))
                .save(argThat(tf -> tf.getIdModule() == null && tf.getIdApplication() == 1));
    }

    /** Toutes les keys de l'application sont "Sans objet" : on sauvegarde -1. */
    @Test
    void testPutIndicateursSonarApplication_ToutesKeysSansObjet() {
        Application app = buildApplication(2, "app-so");
        Set<String> keysSO = Set.of("Sans objet");

        when(oscarService.mapApplicationsToKeySonars()).thenReturn(Map.of(app, keysSO));

        recupIndicateursSonarService.putIndicateursSonarApplication(Map.of());

        verify(tableFaitsRepository, times(1))
                .save(argThat(tf -> tf.getValeur().intValue() == -1 && tf.getIdApplication() == 2));
    }

    /** Aucune mesure disponible dans analyseModule pour la clé : rien n'est sauvegardé. */
    @Test
    void testPutIndicateursSonarApplication_AucuneMesureDansAnalyse() {
        Application app = buildApplication(3, "app-vide");
        Set<String> keys = Set.of("keySonarManquante");

        when(oscarService.mapApplicationsToKeySonars()).thenReturn(Map.of(app, keys));

        recupIndicateursSonarService.putIndicateursSonarApplication(Map.of());

        verify(tableFaitsRepository, never()).save(any(TableFaits.class));
    }

    /** Une exception sur une application ne doit pas bloquer les autres. */
    @Test
    void testPutIndicateursSonarApplication_ExceptionSurUneApplication() {
        Application appBug = buildApplication(10, "app-bug");
        Application appOk = buildApplication(11, "app-ok");

        when(oscarService.mapApplicationsToKeySonars())
                .thenReturn(Map.of(appBug, Set.of("keyBug"), appOk, Set.of("keyOk")));

        RecuperationMeasures mesuresOk = buildMeasures("lines_to_cover", "100");
        Map<String, RecuperationMeasures> analyseModule = Map.of("keyOk", mesuresOk);

        recupIndicateursSonarService.putIndicateursSonarApplication(analyseModule);

        verify(tableFaitsRepository, atLeastOnce())
                .save(argThat(tf -> tf.getIdApplication() == 11));
    }

    @Test
    void testPutIndicateursSonar() {
        Module moduleok =
                Module.builder()
                        .id(488)
                        .modName("module")
                        .appName("application")
                        .sndi("SNDI")
                        .keySonar("keySonarValide")
                        .build();

        Module moduleSansObjet =
                Module.builder()
                        .id(489)
                        .modName("module-so")
                        .appName("application")
                        .sndi("SNDI")
                        .keySonar("Sans objet")
                        .build();

        when(oscarService.getModules()).thenReturn(List.of(moduleok, moduleSansObjet));

        RecuperationMeasures mesuresValides = buildMeasures("lines_to_cover", "1000");
        when(sonarService.getDataFromSonarAPIMeasures("keySonarValide", "gitlab", "module"))
                .thenReturn(mesuresValides);

        recupIndicateursSonarService.putIndicateursSonarModule();

        verify(tableFaitsRepository, times(2)).save(any(TableFaits.class));
    }

    @Test
    void testPutIndicateursSonar_ModuleAvecKeyMaisSansAnalyse() {
        Module moduleSansAnalyse =
                Module.builder().id(1).idApplication(10).keySonar("keySansAnalyse").build();

        when(oscarService.getModules()).thenReturn(List.of(moduleSansAnalyse));
        when(sonarService.getDataFromSonarAPIMeasures("keySansAnalyse", "gitlab", null))
                .thenReturn(null);
        when(sonarService.getDataFromSonarAPIMeasures("keySansAnalyse", "github", null))
                .thenReturn(null);

        Map<String, RecuperationMeasures> result =
                recupIndicateursSonarService.putIndicateursSonarModule();

        assertTrue(result.isEmpty());
        verify(tableFaitsRepository, never()).save(any(TableFaits.class));
        verify(sonarService, times(1))
                .getDataFromSonarAPIMeasures("keySansAnalyse", "gitlab", null);
        verify(sonarService, times(1))
                .getDataFromSonarAPIMeasures("keySansAnalyse", "github", null);
    }
}
