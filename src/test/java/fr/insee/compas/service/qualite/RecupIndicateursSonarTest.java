package fr.insee.compas.service.qualite;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.sonar.Component;
import fr.insee.compas.model.sonar.Measure;
import fr.insee.compas.model.sonar.RecuperationMeasures;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.SonarService;

@ExtendWith(MockitoExtension.class)
class RecupIndicateursSonarTest {

    @Mock private OscarService oscarService;

    @Mock private SonarService sonarService;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @InjectMocks private RecuperationIndicateurSonarService recupIndicateursSonarService;

    private Module module;

    private LocalDate date;

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
        RecuperationMeasures recuperationMeasures = new RecuperationMeasures();
        Measure measure = new Measure("lines_to_cover", "10");
        List<Measure> measures = List.of(measure);
        Component c = Component.builder().measures(measures).build();
        c.setMeasures(measures);
        recuperationMeasures.setComponent(c);

        // Act
        boolean result =
                recupIndicateursSonarService.putIndicateurSonarInBdd(
                        module, null, recuperationMeasures, date);

        // Assert
        assertTrue(result);
        verify(tableFaitsRepository, times(1)).save(any(TableFaits.class));
    }

    @Test
    void testPutIndicateurSonarInBdd_Fail_NoMeasure() {

        RecuperationMeasures recuperationMeasures = new RecuperationMeasures();
        Measure measure = new Measure("Aucune mesure disponible", "10");
        List<Measure> measures = List.of(measure);
        Component c = Component.builder().measures(measures).build();
        c.setMeasures(measures);
        recuperationMeasures.setComponent(c);

        // Act
        boolean result =
                recupIndicateursSonarService.putIndicateurSonarInBdd(
                        module, null, recuperationMeasures, date);

        // Assert
        assertFalse(result);
        verify(tableFaitsRepository, never()).save(any(TableFaits.class));
    }

    @Test
    void testPutIndicateursSonar() throws IOException {
        // 1. Préparer les données mockées
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
                        .id(488)
                        .modName("module")
                        .appName("application")
                        .sndi("SNDI")
                        .keySonar("Sans objet")
                        .build();

        List<Module> modules = List.of(moduleok, moduleSansObjet);
        when(oscarService.getModules()).thenReturn(modules);

        RecuperationMeasures mesuresValides = new RecuperationMeasures();
        Measure m = new Measure("lines_to_cover", "1000");
        List<Measure> mesures = List.of(m);
        Component c = Component.builder().measures(mesures).build();
        mesuresValides.setComponent(c);
        when(sonarService.getDataFromSonarAPIMeasures("keySonarValide", "gitlab"))
                .thenReturn(mesuresValides);

        // 2. Appeler la méthode à tester
        recupIndicateursSonarService.putIndicateursSonarModule();

        // 3. Vérifier les appels et les effets
        verify(tableFaitsRepository, times(2)).save(any(TableFaits.class));
    }
}
