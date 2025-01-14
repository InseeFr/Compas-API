package fr.insee.compas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.ApplicationOscarView;
import fr.insee.compas.client.view.ModuleOscarView;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.model.greenit.MetriqueVm;
import fr.insee.compas.repository.TableFaitsRepository;

@ExtendWith(MockitoExtension.class)
class GreenItServiceTest {

    @InjectMocks private GreenItService greenItService;

    @Mock private OscarClient oscarClient;

    @Mock private TableFaitsRepository tableFaitsRepository;

    private List<MetriqueVm> metrics;

    @BeforeEach
    public void init() {
        metrics = new ArrayList<>();
        final MetriqueVm metric1 = new MetriqueVm("pdsir4esli001", 2048, 80, 0, 2, 8);
        final MetriqueVm metric2 = new MetriqueVm("pdsir4esli002", 1024, 50, 0, 2, 8);
        final MetriqueVm metric3 = new MetriqueVm("pdsir4replm001", 1024, 50, 0, 2, 8);
        metrics = Arrays.asList(metric1, metric2, metric3);
        greenItService.setMetrics(metrics);
    }

    @Test
    void testGetIndicateursApplicationGreenIT() {
        Mockito.when(oscarClient.getApplicationOscar(123)).thenReturn(mockAppliSirene4());
        final IndicateurApplicationGreenIT applicationGreenIT =
                greenItService.getIndicateursApplicationGreenIT(123);
        assertThat(applicationGreenIT)
                .isNotNull()
                .satisfies(
                        c -> {
                            c.getApplicationId().equals(123);
                            c.getApplicationName().equals("sirene4");
                        });
        // TODO à ce stade, je vérifie le random
        assertThat(applicationGreenIT.getNbVm()).isLessThan(1000);
    }

    @Test
    void testGetIndicateursModuleGreenIT() {
        Mockito.when(oscarClient.getModuleOscar(244)).thenReturn(mockModulesSirene4());
        final TableFaits tableFaits = new TableFaits();
        tableFaits.setIdModule(244);
        tableFaits.setIdIndicateur(101);
        tableFaits.setValeur(new BigDecimal(8));
        final List<TableFaits> list = new ArrayList<>();
        list.add(tableFaits);
        Mockito.when(tableFaitsRepository.findLatestValueByIndicateurAndModule(101, 244))
                .thenReturn(list);
        final IndicateurModuleGreenIT moduleGreenIT =
                greenItService.getIndicateursModuleGreenIT(244);
        assertThat(moduleGreenIT)
                .isNotNull()
                .satisfies(
                        c -> {
                            c.getModuleId().equals(244);
                            c.getModuleName().equals("sirene4");
                        });
        assertThat(moduleGreenIT.getRamAllocated()).isLessThan(1000);
    }

    private ResponseEntity<ApplicationOscarView> mockAppliSirene4() {
        final ApplicationOscarView applicationOscarView = new ApplicationOscarView();
        applicationOscarView.setId(123);
        applicationOscarView.setNom("sirene3");
        applicationOscarView.setNomTechnique("Sirene 4");
        applicationOscarView.setDescription("le répertoire des entreprises et des établissements");
        final ResponseEntity<ApplicationOscarView> responseEntity =
                new ResponseEntity<ApplicationOscarView>(applicationOscarView, HttpStatus.ACCEPTED);
        return responseEntity;
    }

    private ResponseEntity<ModuleOscarView> mockModulesSirene4() {
        final ModuleOscarView moduleOscarView = new ModuleOscarView();
        moduleOscarView.setId(238);
        moduleOscarView.setNom("sirene4");
        moduleOscarView.setNomTechnique("Sirene 4");
        final ResponseEntity<ModuleOscarView> responseEntity =
                new ResponseEntity<ModuleOscarView>(moduleOscarView, HttpStatus.ACCEPTED);
        return responseEntity;
    }

    @Test
    void testMiseAJourIndicateursModuleGreenIT() {
        greenItService.miseAJourIndicateursModuleGreenIT();
        verify(tableFaitsRepository, times(5)).save(Mockito.any(TableFaits.class));
    }

    @Test
    void testmiseAJourIndicateursApplicationGreenIT_Valide() {
        greenItService.miseAJourIndicateursApplicationGreenIT();
        verify(tableFaitsRepository, times(5)).save(Mockito.any(TableFaits.class));
    }

    @Test
    void testmiseAJourIndicateursApplicationGreenIT_ErreurRepo() {
        greenItService.setMetrics(metrics);
        Mockito.doThrow(new RuntimeException("Database error"))
                .when(tableFaitsRepository)
                .save(Mockito.any(TableFaits.class));
        assertThatThrownBy(() -> greenItService.miseAJourIndicateursApplicationGreenIT())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
    }
}
