package fr.insee.compas.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.ApplicationOscarView;
import fr.insee.compas.client.view.ModuleOscarView;
import fr.insee.compas.client.view.VmOscarView;
import fr.insee.compas.mapper.MetriqueVmMapper;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.compas.dto.MetriqueApplicationDTO;
import fr.insee.compas.model.compas.dto.MetriqueModuleDTO;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.model.greenit.MetriqueVm;
import fr.insee.compas.model.greenit.MetriqueVmCsvRead;
import fr.insee.compas.model.greenit.util.LectureCsvUtil;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.greenit.GreenItService;

@ExtendWith(MockitoExtension.class)
class GreenItServiceGetTest {

    @InjectMocks @Spy private GreenItService greenItService;

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
        when(tableFaitsRepository.findSumByDateAndIdIndicateurAndIdApplication(
                        any(), eq(IndicateurType.RAM_ALLOUEE.getValue()), eq(123)))
                .thenReturn(BigDecimal.valueOf(12.0));
        when(tableFaitsRepository.findSumByDateAndIdIndicateurAndIdApplication(
                        any(), eq(IndicateurType.RAM_MAXI.getValue()), eq(123)))
                .thenReturn(BigDecimal.valueOf(12.0));
        when(tableFaitsRepository.findSumByDateAndIdIndicateurAndIdApplication(
                        any(), eq(IndicateurType.DISQUE_ALLOUE.getValue()), eq(123)))
                .thenReturn(BigDecimal.valueOf(500.0));
        when(tableFaitsRepository.findSumByDateAndIdIndicateurAndIdApplication(
                        any(), eq(IndicateurType.DISQUE_CONSOMME.getValue()), eq(123)))
                .thenReturn(BigDecimal.valueOf(50.0));
        when(tableFaitsRepository.findSumByDateAndIdIndicateurAndIdApplication(
                        any(), eq(IndicateurType.CPU_ALLOUEE.getValue()), eq(123)))
                .thenReturn(BigDecimal.valueOf(20.0));
        when(tableFaitsRepository.findSumByDateAndIdIndicateurAndIdApplication(
                        any(), eq(IndicateurType.CPU_MAXI.getValue()), eq(123)))
                .thenReturn(BigDecimal.valueOf(20.0));
        when(tableFaitsRepository.findSumByDateAndIdIndicateurAndIdApplication(
                        any(), eq(IndicateurType.CONSO_ELEC.getValue()), eq(123)))
                .thenReturn(BigDecimal.valueOf(200.0));

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
        List<Object[]> rows =
                List.of(
                        new Object[] {3, LocalDate.of(2025, 2, 1), BigDecimal.ONE},
                        new Object[] {2, LocalDate.of(2025, 2, 2), BigDecimal.TEN},
                        new Object[] {1, LocalDate.of(2025, 2, 3), BigDecimal.ZERO});
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
        List<Object[]> rows =
                List.of(
                        new Object[] {3, LocalDate.of(2025, 2, 1), BigDecimal.ONE},
                        new Object[] {2, LocalDate.of(2025, 2, 2), BigDecimal.TEN},
                        new Object[] {1, LocalDate.of(2025, 2, 3), BigDecimal.ZERO});
        when(tableFaitsRepository.findLatestSummedValuesByIndicateurForAllModules(
                        IndicateurType.CONSO_ELEC.getValue()))
                .thenReturn(rows);
        List<MetriqueModuleDTO> dtos = greenItService.getModuleMetriques();
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(dtos).isNotNull();
        softAssertions.assertThat((long) dtos.size()).isEqualTo(3);
        softAssertions.assertAll();
    }

    @Test
    @DisplayName(
            "Mappe les Optionals (present/empty), met à jour metrics et appelle"
                    + " miseAJourIndicateursGreenIT")
    void testMajIndicateursGreenItfromfile() throws Exception {
        // given
        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "metrics.csv",
                        "text/csv",
                        "dummy".getBytes(StandardCharsets.UTF_8));

        // 3 DTO "csv"
        MetriqueVmCsvRead dto1 = mock(MetriqueVmCsvRead.class);
        MetriqueVmCsvRead dto2 = mock(MetriqueVmCsvRead.class);
        MetriqueVmCsvRead dto3 = mock(MetriqueVmCsvRead.class);

        // 2 métriques mappées (dto2 -> Optional.empty pour tester flatMap Optional::stream)
        MetriqueVm m1 = mock(MetriqueVm.class);
        MetriqueVm m3 = mock(MetriqueVm.class);

        // Spy: on remplace loadCSVData(file)
        doReturn(List.of(dto1, dto2, dto3)).when(greenItService).loadCSVData(file);

        // Mapper: present / empty / present
        when(metriqueVmMapper.toMetriqueVm(dto1)).thenReturn(Optional.of(m1));
        when(metriqueVmMapper.toMetriqueVm(dto2)).thenReturn(Optional.empty());
        when(metriqueVmMapper.toMetriqueVm(dto3)).thenReturn(Optional.of(m3));

        LocalDate date = LocalDate.of(2025, 9, 1);

        // On ne veut pas exécuter la vraie logique interne ici : on vérifie juste l'appel
        doNothing().when(greenItService).miseAJourIndicateursGreenIT(date);

        // when
        greenItService.miseAJourIndicateursGreenItFromFile(file, date);

        // then — interactions
        verify(greenItService).loadCSVData(file);
        verify(metriqueVmMapper).toMetriqueVm(dto1);
        verify(metriqueVmMapper).toMetriqueVm(dto2);
        verify(metriqueVmMapper).toMetriqueVm(dto3);
        verify(greenItService).miseAJourIndicateursGreenIT(date);
    }
}
