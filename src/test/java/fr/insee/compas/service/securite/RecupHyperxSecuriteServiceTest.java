package fr.insee.compas.service.securite;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.hyperx.IndicateurRecuperationSecuriteVM;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.HyperxService;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.util.observer.EventTypeObserver;
import fr.insee.compas.util.observer.IEventManager;

@ExtendWith(MockitoExtension.class)
class RecupHyperxSecuriteServiceTest {

    @Mock private TableFaitsRepository tableFaitsRepository;

    @Mock private HyperxService hyperxService;

    @Mock private OscarService oscarService;

    @Mock private IEventManager eventObserverManager;

    @InjectMocks private RecupHyperxSecuriteService recupHyperxSecuriteService;

    private List<Application> applications;
    private IndicateurRecuperationSecuriteVM indicateurVM;

    @BeforeEach
    void setUp() {
        Application application1 = new Application();
        application1.setIdApplication(1);
        application1.setAppName("App1");

        Application application2 = new Application();
        application2.setIdApplication(2);
        application2.setAppName("App2");

        applications = Arrays.asList(application1, application2);

        indicateurVM = new IndicateurRecuperationSecuriteVM();
        indicateurVM.setMax(10);
        indicateurVM.setNb(5);
    }

    @Test
    void testUpdateDonneesVmNonMiseAjourDansDelaiParHyperX_Success() {
        when(oscarService.getApplications()).thenReturn(applications);
        when(hyperxService.maxMajVm("App1")).thenReturn(indicateurVM);
        when(hyperxService.maxMajVm("App2")).thenReturn(indicateurVM);

        recupHyperxSecuriteService.updateDonneesVmNonMiseAjourDansDelaiParHyperX();

        verify(tableFaitsRepository, times(4)).save(any(TableFaits.class));
        verify(eventObserverManager, never()).notifyObservers(any(), anyString());
    }

    @Test
    void testUpdateDonneesVmNonMiseAjourDansDelaiParHyperX_Exception() {
        when(oscarService.getApplications()).thenReturn(applications);
        when(hyperxService.maxMajVm("App1")).thenThrow(new RuntimeException("Erreur HyperX"));
        when(hyperxService.maxMajVm("App2")).thenReturn(indicateurVM);

        recupHyperxSecuriteService.updateDonneesVmNonMiseAjourDansDelaiParHyperX();

        verify(tableFaitsRepository, times(2)).save(any(TableFaits.class));
        verify(eventObserverManager, times(1))
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
    }

    @Test
    void testUpdateDonneesVmNonMiseAjourDansDelaiParHyperX_TableFaitsContent() {
        when(oscarService.getApplications()).thenReturn(applications);
        when(hyperxService.maxMajVm("App1")).thenReturn(indicateurVM);

        recupHyperxSecuriteService.updateDonneesVmNonMiseAjourDansDelaiParHyperX();

        verify(tableFaitsRepository, times(2)).save(any(TableFaits.class));
    }
}
