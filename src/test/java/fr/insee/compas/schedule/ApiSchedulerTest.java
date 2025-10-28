package fr.insee.compas.schedule;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.insee.compas.model.sonar.RecuperationMeasures;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.a11y.A11yMajService;
import fr.insee.compas.service.devops.UpdateIndicatorDevopsService;
import fr.insee.compas.service.qualite.RecuperationIndicateurSonarService;
import fr.insee.compas.service.securite.RecupCveSecuriteService;

class ApiSchedulerTest {

    @Mock private OscarService oscarService;

    @Mock private RecuperationIndicateurSonarService indicateurSonar;

    @Mock private RecupCveSecuriteService cveService;

    @Mock private UpdateIndicatorDevopsService updateIndicatorDevopsService;

    @Mock private A11yMajService a11yMajService;

    @InjectMocks private ApiScheduler apiScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void callApi_shouldUpdateAllIndicators() throws IOException {
        // Given
        Map<String, RecuperationMeasures> dummyMap = new HashMap<>();
        when(indicateurSonar.putIndicateursSonarModule()).thenReturn(dummyMap);

        // When
        apiScheduler.callApi();

        // Then
        verify(oscarService, times(1)).miseAjourModuleOscarEnBaseDeDonnees();
        verify(indicateurSonar, times(1)).putIndicateursSonarModule();
        verify(indicateurSonar, times(1)).putIndicateursSonarApplication(dummyMap);
        verify(cveService, times(1)).recupereCve();
        verify(updateIndicatorDevopsService, times(1))
                .miseAJourIndicateursDevopsEnBaseDeDonnes(null, null);
    }

    @Test
    void callApi_shouldHandleIOExceptionGracefully() throws IOException {
        // Given
        when(indicateurSonar.putIndicateursSonarModule()).thenThrow(new IOException("Erreur test"));

        // When
        apiScheduler.callApi();

        // Then
        verify(oscarService, times(1)).miseAjourModuleOscarEnBaseDeDonnees();
        verify(indicateurSonar, times(1)).putIndicateursSonarModule();
        verify(indicateurSonar, times(0)).putIndicateursSonarApplication(any());
        verify(cveService, times(1)).recupereCve();
        verify(updateIndicatorDevopsService, times(1))
                .miseAJourIndicateursDevopsEnBaseDeDonnes(null, null);
    }
}
