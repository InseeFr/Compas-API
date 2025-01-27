package fr.insee.compas.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
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
import fr.insee.compas.mapper.MetriqueVmMapper;
import fr.insee.compas.model.Indicateur;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.model.greenit.MetriqueVm;
import fr.insee.compas.model.greenit.util.LectureCsvUtil;
import fr.insee.compas.repository.TableFaitsRepository;

@ExtendWith(MockitoExtension.class)
class GreenItServiceTest {

    @InjectMocks private GreenItService greenItService;

    @Mock private OscarClient oscarClient;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @Mock private MetriqueVmMapper metriqueVmMapper;

    private List<MetriqueVm> metrics;

    @BeforeEach
    public void init() {
        metrics = new ArrayList<>();
        final MetriqueVm metric1 =
                MetriqueVm.builder()
                        .vm("pdsir4esli001")
                        .diskAllocated(LectureCsvUtil.process("154,83"))
                        .diskUsed(LectureCsvUtil.process("49,46"))
                        .ramAllocated(LectureCsvUtil.process("4 788,75"))
                        .ramMaxi(LectureCsvUtil.process("6 390,57"))
                        .cpuAllocated(LectureCsvUtil.process("4"))
                        .cpuMaxi(LectureCsvUtil.process("3,9"))
                        .conso(LectureCsvUtil.process("1,13"))
                        .build();

        final MetriqueVm metric2 =
                MetriqueVm.builder()
                        .vm("pdsir4replm001")
                        .diskAllocated(LectureCsvUtil.process("12,84"))
                        .diskUsed(LectureCsvUtil.process("4,74"))
                        .ramAllocated(LectureCsvUtil.process("2 394,38"))
                        .ramMaxi(LectureCsvUtil.process("498,73"))
                        .cpuAllocated(LectureCsvUtil.process("1"))
                        .cpuMaxi(LectureCsvUtil.process("1"))
                        .conso(LectureCsvUtil.process("0,07"))
                        .build();

        final MetriqueVm metric3 =
                MetriqueVm.builder()
                        .vm("pdsir4replm001")
                        .diskAllocated(LectureCsvUtil.process("12,84"))
                        .diskUsed(LectureCsvUtil.process("4,74"))
                        .ramAllocated(LectureCsvUtil.process("2 394,38"))
                        .ramMaxi(LectureCsvUtil.process("498,73"))
                        .cpuAllocated(LectureCsvUtil.process("1"))
                        .cpuMaxi(LectureCsvUtil.process("1"))
                        .conso(LectureCsvUtil.process("0,07"))
                        .build();
        metrics = Arrays.asList(metric1, metric2, metric3);
        greenItService.setMetrics(metrics);
    }

    @Test
    void testGetIndicateursApplicationGreenIT() {
        Mockito.when(oscarClient.getApplicationOscar(123)).thenReturn(mockAppliSirene4());
        final List<BigDecimal> list = new ArrayList<>();
        list.add(new BigDecimal(8));
        Mockito.when(
                        tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                                Indicateur.RAM_ALLOUEE.getValue(), 123))
                .thenReturn(list);
        final IndicateurApplicationGreenIT applicationGreenIT =
                greenItService.getIndicateursApplicationGreenIT(123);
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(applicationGreenIT).isNotNull();
        softAssertions.assertThat(applicationGreenIT.getApplicationId()).isEqualTo(123);
        softAssertions.assertThat(applicationGreenIT.getApplicationName()).isEqualTo("sirene4");
        softAssertions.assertThat(applicationGreenIT.getRamAllocated()).isGreaterThan(1);
        softAssertions.assertAll();
    }

    @Test
    void testGetIndicateursModuleGreenIT() {
        Mockito.when(oscarClient.getModuleOscar(238)).thenReturn(mockModulesSirene4());
        final TableFaits tableFaits = new TableFaits();
        tableFaits.setIdModule(238);
        tableFaits.setIdIndicateur(Indicateur.RAM_ALLOUEE.getValue());
        tableFaits.setValeur(new BigDecimal(8));
        final List<TableFaits> list = new ArrayList<>();
        list.add(tableFaits);
        Mockito.when(
                        tableFaitsRepository.findLatestValueByIndicateurAndModule(
                                Indicateur.RAM_ALLOUEE.getValue(), 238))
                .thenReturn(list);
        final IndicateurModuleGreenIT moduleGreenIT =
                greenItService.getIndicateursModuleGreenIT(238);
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(moduleGreenIT).isNotNull();
        softAssertions.assertThat(moduleGreenIT.getModuleId()).isEqualTo(238);
        softAssertions.assertThat(moduleGreenIT.getModuleName()).isEqualTo("sirene4");
        softAssertions.assertThat(moduleGreenIT.getRamAllocated()).isGreaterThan(1);
        softAssertions.assertAll();
    }

    private ResponseEntity<ApplicationOscarView> mockAppliSirene4() {
        final ApplicationOscarView applicationOscarView = new ApplicationOscarView();
        applicationOscarView.setId(123);
        applicationOscarView.setNom("sirene4");
        applicationOscarView.setNomTechnique("Sirene 4");
        applicationOscarView.setDescription("le répertoire des entreprises et des établissements");
        return new ResponseEntity<ApplicationOscarView>(applicationOscarView, HttpStatus.ACCEPTED);
    }

    private ResponseEntity<ModuleOscarView> mockModulesSirene4() {
        final ModuleOscarView moduleOscarView = new ModuleOscarView();
        moduleOscarView.setId(238);
        moduleOscarView.setNom("sirene4");
        moduleOscarView.setNomTechnique("Sirene 4");
        return new ResponseEntity<ModuleOscarView>(moduleOscarView, HttpStatus.ACCEPTED);
    }

    @Test
    void testMiseAJourIndicateursModuleGreenIT() {
        greenItService.miseAJourIndicateursModuleGreenIT();
        verify(tableFaitsRepository, times(8)).save(Mockito.any(TableFaits.class));
    }

    @Test
    void testmiseAJourIndicateursApplicationGreenIT_Valide() {
        greenItService.miseAJourIndicateursApplicationGreenIT();
        verify(tableFaitsRepository, times(8)).save(Mockito.any(TableFaits.class));
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
