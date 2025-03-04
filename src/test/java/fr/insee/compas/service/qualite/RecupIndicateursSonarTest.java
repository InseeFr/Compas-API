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
import fr.insee.compas.service.SonarService;

@ExtendWith(MockitoExtension.class)
class RecupIndicateursSonarTest {

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
    void testPutIndicateurSonarInBdd_Success() throws IOException {
        RecuperationMeasures recuperationMeasures = new RecuperationMeasures();
        Component component = new Component();
        Measure measure = new Measure();

        measure.setValue("10");
        measure.setMetric("lines_to_cover");
        List<Measure> measures = List.of(measure);

        component.setMeasures(measures);
        recuperationMeasures.setComponent(component);

        // Act
        boolean result =
                recupIndicateursSonarService.putIndicateurSonarInBdd(
                        module, recuperationMeasures, date);

        // Assert
        assertTrue(result);
        verify(tableFaitsRepository, times(1)).save(any(TableFaits.class));
    }

    @Test
    void testPutIndicateurSonarInBdd_Fail_NoMeasure() throws IOException {
        RecuperationMeasures recuperationMeasures = new RecuperationMeasures();
        Component component = new Component();
        Measure measure = new Measure();

        measure.setValue("10");
        measure.setMetric("Aucune mesure disponible");
        List<Measure> measures = List.of(measure);

        component.setMeasures(measures);
        recuperationMeasures.setComponent(component);

        // Act
        boolean result =
                recupIndicateursSonarService.putIndicateurSonarInBdd(
                        module, recuperationMeasures, date);

        // Assert
        assertFalse(result);
        verify(tableFaitsRepository, never()).save(any(TableFaits.class));
    }
}
