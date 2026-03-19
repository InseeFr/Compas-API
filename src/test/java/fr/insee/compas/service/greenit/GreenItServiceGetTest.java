package fr.insee.compas.service.greenit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.ApplicationOscarView;
import fr.insee.compas.client.view.ModuleOscarView;
import fr.insee.compas.client.view.VmOscarView;
import fr.insee.compas.logic.update.greenit.kube.KubeMetricsCsvUpdater;
import fr.insee.compas.logic.update.greenit.vm.VmMetricsCsvUpdater;
import fr.insee.compas.mapper.MetriqueVmMapper;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.compas.dto.MetriqueApplicationDTO;
import fr.insee.compas.model.compas.dto.MetriqueModuleDTO;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.repository.projection.MetriqueApplicationProjection;
import fr.insee.compas.repository.projection.MetriqueModuleProjection;
import fr.insee.compas.repository.projection.MetriqueSumIndicateurProjection;

@ExtendWith(MockitoExtension.class)
class GreenItServiceGetTest {

    @InjectMocks @Spy private GreenItService greenItService;

    @Mock private OscarClient oscarClient;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @Mock private MetriqueVmMapper metriqueVmMapper;

    @Mock private KubeMetricsCsvUpdater kubeMetricsCsvUpdater;

    @Mock private VmMetricsCsvUpdater vmMetricsCsvUpdater;

    @Test
    void testGetIndicateursApplicationGreenIT() {
        when(oscarClient.getApplicationOscar(123)).thenReturn(mockAppliSirene4());
        final TableFaits tableFaits = new TableFaits();
        tableFaits.setIdApplication(123);
        tableFaits.setIdIndicateur(IndicateurType.RAM_ALLOUEE.getValue());
        tableFaits.setValeur(new BigDecimal(8));
        final List<TableFaits> list = new ArrayList<>();
        list.add(tableFaits);
        final List<VmOscarView> listVms = new ArrayList<>();
        listVms.add(
                VmOscarView.builder()
                        .idApplication(123)
                        .idModule(null)
                        .nom("pdsir4replm001")
                        .build());
        MetriqueSumIndicateurProjection p1 = mock(MetriqueSumIndicateurProjection.class);
        when(p1.getIdIndicateur()).thenReturn(201);
        when(p1.getTotalValeur()).thenReturn(BigDecimal.valueOf(12));

        MetriqueSumIndicateurProjection p2 = mock(MetriqueSumIndicateurProjection.class);
        when(p2.getIdIndicateur()).thenReturn(203);
        when(p2.getTotalValeur()).thenReturn(BigDecimal.valueOf(500.0));

        MetriqueSumIndicateurProjection p3 = mock(MetriqueSumIndicateurProjection.class);
        when(p3.getIdIndicateur()).thenReturn(205);
        when(p3.getTotalValeur()).thenReturn(BigDecimal.valueOf(20.0));

        MetriqueSumIndicateurProjection p4 = mock(MetriqueSumIndicateurProjection.class);
        when(p4.getIdIndicateur()).thenReturn(207);
        when(p4.getTotalValeur()).thenReturn(BigDecimal.valueOf(200.0));

        when(tableFaitsRepository.findSumByDateAndListIndicateurIdsAndIdApplication(
                        any(), anyList(), eq(123)))
                .thenReturn(List.of(p1, p2, p3, p4));
        final IndicateurApplicationGreenIT applicationGreenIT =
                greenItService.getIndicateursApplicationGreenIT(123);
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(applicationGreenIT).isNotNull();
        softAssertions.assertThat(applicationGreenIT.getApplicationId()).isEqualTo(123);
        softAssertions.assertThat(applicationGreenIT.getApplicationName()).isEqualTo("sirene4");
        softAssertions.assertThat(applicationGreenIT.getRamAllocated()).isGreaterThan(1);
        softAssertions.assertThat(applicationGreenIT.getDiskAllocated()).isGreaterThan(1);
        softAssertions.assertThat(applicationGreenIT.getCpuAllocated()).isGreaterThan(1);
        softAssertions.assertThat(applicationGreenIT.getConso()).isGreaterThan(1);
        softAssertions.assertAll();
    }

    @Test
    void testGetIndicateursModuleGreenIT() {
        when(oscarClient.getModuleOscar(238)).thenReturn(mockModulesSirene4());
        final TableFaits tableFaits = new TableFaits();
        tableFaits.setIdModule(238);
        tableFaits.setIdIndicateur(IndicateurType.RAM_ALLOUEE.getValue());
        tableFaits.setValeur(new BigDecimal(8));
        final List<TableFaits> list = new ArrayList<>();
        list.add(tableFaits);
        when(tableFaitsRepository.findByDateAndIdIndicateurAndIdModule(
                        any(), eq(IndicateurType.RAM_ALLOUEE.getValue()), eq(238)))
                .thenReturn(list);
        when(tableFaitsRepository.findByDateAndIdIndicateurAndIdModule(
                        any(), eq(IndicateurType.RAM_MAXI.getValue()), eq(238)))
                .thenReturn(list);
        when(tableFaitsRepository.findByDateAndIdIndicateurAndIdModule(
                        any(), eq(IndicateurType.DISQUE_ALLOUE.getValue()), eq(238)))
                .thenReturn(list);
        when(tableFaitsRepository.findByDateAndIdIndicateurAndIdModule(
                        any(), eq(IndicateurType.DISQUE_CONSOMME.getValue()), eq(238)))
                .thenReturn(list);
        when(tableFaitsRepository.findByDateAndIdIndicateurAndIdModule(
                        any(), eq(IndicateurType.CPU_ALLOUEE.getValue()), eq(238)))
                .thenReturn(list);
        when(tableFaitsRepository.findByDateAndIdIndicateurAndIdModule(
                        any(), eq(IndicateurType.CPU_MAXI.getValue()), eq(238)))
                .thenReturn(list);
        when(tableFaitsRepository.findByDateAndIdIndicateurAndIdModule(
                        any(), eq(IndicateurType.CONSO_ELEC.getValue()), eq(238)))
                .thenReturn(list);
        final IndicateurModuleGreenIT moduleGreenIT =
                greenItService.getIndicateursModuleGreenIT(238);
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(moduleGreenIT).isNotNull();
        softAssertions.assertThat(moduleGreenIT.getModuleId()).isEqualTo(238);
        softAssertions.assertThat(moduleGreenIT.getModuleName()).isEqualTo("sirene4");
        softAssertions.assertThat(moduleGreenIT.getRamAllocated()).isGreaterThan(1);
        softAssertions.assertThat(moduleGreenIT.getDiskAllocated()).isGreaterThan(1);
        softAssertions.assertThat(moduleGreenIT.getCpuAllocated()).isGreaterThan(1);
        softAssertions.assertThat(moduleGreenIT.getConso()).isGreaterThan(1);
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
    @DisplayName("getApplicationMetriques retourne du vide")
    void testGetApplicationMetriques_Vide() {
        when(tableFaitsRepository.findLatestSummedValuesByIndicateurForAllApplications(
                        IndicateurType.CONSO_ELEC.getValue()))
                .thenReturn(List.of());
        List<MetriqueApplicationDTO> dtos = greenItService.getApplicationMetriques();
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(dtos).isNotNull();
        softAssertions.assertThat(dtos).isEmpty();
        softAssertions.assertAll();
    }

    @Test
    @DisplayName("getApplicationMetriques retourne 3 lignes")
    void testGetApplicationMetriques_TroisLignes() {
        List<MetriqueApplicationProjection> rows =
                List.of(
                        new MetriqueApplicationProjectionStub(
                                3, LocalDate.of(2025, 2, 1), BigDecimal.ONE),
                        new MetriqueApplicationProjectionStub(
                                2, LocalDate.of(2025, 2, 2), BigDecimal.TEN),
                        new MetriqueApplicationProjectionStub(
                                1, LocalDate.of(2025, 2, 3), BigDecimal.ZERO));
        when(tableFaitsRepository.findLatestSummedValuesByIndicateurForAllApplications(
                        IndicateurType.CONSO_ELEC.getValue()))
                .thenReturn(rows);
        List<MetriqueApplicationDTO> dtos = greenItService.getApplicationMetriques();
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(dtos).isNotNull();
        softAssertions.assertThat(dtos.stream().count()).isEqualTo(3);
        softAssertions.assertAll();
    }

    @Test
    @DisplayName("getModuleMetriques retourne du vide")
    void testGetModuleMetriques_Vide() {
        when(tableFaitsRepository.findLatestSummedValuesByIndicateurForAllModules(
                        IndicateurType.CONSO_ELEC.getValue()))
                .thenReturn(List.of());
        List<MetriqueModuleDTO> dtos = greenItService.getModuleMetriques();
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(dtos).isNotNull();
        softAssertions.assertThat(dtos).isEmpty();
        softAssertions.assertAll();
    }

    @Test
    @DisplayName("getModuleMetriques retourne 3 lignes")
    void testGetModuleMetriques_TroisLignes() {
        List<MetriqueModuleProjection> rows =
                List.of(
                        new MetriqueModuleProjectionStub(
                                3, LocalDate.of(2025, 2, 1), BigDecimal.ONE),
                        new MetriqueModuleProjectionStub(
                                2, LocalDate.of(2025, 2, 2), BigDecimal.TEN),
                        new MetriqueModuleProjectionStub(
                                1, LocalDate.of(2025, 2, 3), BigDecimal.ZERO));
        when(tableFaitsRepository.findLatestSummedValuesByIndicateurForAllModules(
                        IndicateurType.CONSO_ELEC.getValue()))
                .thenReturn(rows);
        List<MetriqueModuleDTO> dtos = greenItService.getModuleMetriques();
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(dtos).isNotNull();
        softAssertions.assertThat((long) dtos.size()).isEqualTo(3);
        softAssertions.assertAll();
    }

    class MetriqueApplicationProjectionStub implements MetriqueApplicationProjection {

        private final Integer idApplication;
        private final LocalDate date;
        private final BigDecimal totalValeur;

        MetriqueApplicationProjectionStub(
                Integer idApplication, LocalDate date, BigDecimal totalValeur) {
            this.idApplication = idApplication;
            this.date = date;
            this.totalValeur = totalValeur;
        }

        @Override
        public Integer getIdApplication() {
            return idApplication;
        }

        @Override
        public LocalDate getDate() {
            return date;
        }

        @Override
        public BigDecimal getTotalValeur() {
            return totalValeur;
        }
    }

    class MetriqueModuleProjectionStub implements MetriqueModuleProjection {

        private final Integer idModule;
        private final LocalDate date;
        private final BigDecimal totalValeur;

        MetriqueModuleProjectionStub(Integer idModule, LocalDate date, BigDecimal totalValeur) {
            this.idModule = idModule;
            this.date = date;
            this.totalValeur = totalValeur;
        }

        @Override
        public Integer getIdModule() {
            return idModule;
        }

        @Override
        public LocalDate getDate() {
            return date;
        }

        @Override
        public BigDecimal getTotalValeur() {
            return totalValeur;
        }
    }
}
