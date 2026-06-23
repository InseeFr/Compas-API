package fr.insee.compas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.insee.compas.dto.AggregatedResultDto;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.repository.projection.DevopsProjection;
import fr.insee.compas.view.IndicateurDevopsView;

class TableFaitsServiceTest {

    private TableFaitsService tableFaitsService;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tableFaitsService = new TableFaitsService(tableFaitsRepository);
    }

    @Test
    void givenModuleMetrics_whenGetMapMetricByModule_thenReturnMap() {
        // given
        int indicateur = 1;
        TableFaits tableFait1 = new TableFaits();
        tableFait1.setIdModule(101);
        TableFaits tableFait2 = new TableFaits();
        tableFait2.setIdModule(102);
        List<TableFaits> mockMetrics = Arrays.asList(tableFait1, tableFait2);
        when(tableFaitsRepository.findLatestValueByIndicateurByModule(indicateur))
                .thenReturn(mockMetrics);

        // when
        Map<Integer, TableFaits> result = tableFaitsService.getMapMetricByModule(indicateur);

        // then
        assertEquals(2, result.size());
        assertEquals(tableFait1, result.get(101));
        assertEquals(tableFait2, result.get(102));
        verify(tableFaitsRepository, times(1)).findLatestValueByIndicateurByModule(indicateur);
    }

    @Test
    void givenApplicationMetrics_whenGetMapMetricByApplication_thenReturnMap() {
        // given
        int indicateur = 2;
        TableFaits app1 = new TableFaits();
        app1.setIdApplication(201);
        TableFaits app2 = new TableFaits();
        app2.setIdApplication(202);
        when(tableFaitsRepository.findLatestValueByIndicateurByApplication(indicateur))
                .thenReturn(Arrays.asList(app1, app2));

        // when
        Map<Integer, TableFaits> result = tableFaitsService.getMapMetricByApplication(indicateur);

        // then
        assertEquals(2, result.size());
        assertEquals(app1, result.get(201));
        assertEquals(app2, result.get(202));
    }

    @Test
    void givenAggregatedResults_whenFindAgregationSum_thenReturnDtoMap() {
        // given
        int indicateur = 3;
        List<Object[]> results = Arrays.asList(new Object[] {1, 100.5}, new Object[] {2, 200});
        when(tableFaitsRepository.findAggregatedSumResults(indicateur)).thenReturn(results);

        // when
        Map<Integer, AggregatedResultDto> map =
                tableFaitsService.findAgregationSumByIndicateurAndApplication(indicateur);

        // then
        assertEquals(new BigDecimal("100.5"), map.get(1).getSumValeur());
        assertEquals(new BigDecimal("200"), map.get(2).getSumValeur());
    }

    @Test
    void givenAggregatedResults_whenFindAgregationMax_thenReturnDtoMap() {
        // given
        int indicateur = 4;
        List<Object[]> results = Arrays.asList(new Object[] {10, 50}, new Object[] {20, 75});
        when(tableFaitsRepository.findAggregatedMaxResults(indicateur)).thenReturn(results);

        // when
        Map<Integer, AggregatedResultDto> map =
                tableFaitsService.findAgregationMaxByIndicateurAndApplication(indicateur);

        // then
        assertEquals(new BigDecimal("50"), map.get(10).getSumValeur());
        assertEquals(new BigDecimal("75"), map.get(20).getSumValeur());
    }

    @Test
    void givenAggregatedResults_whenFindAgregationAvg_thenReturnDtoMap() {
        // given
        int indicateur = 5;
        List<Object[]> results = Arrays.asList(new Object[] {1, 10.75}, new Object[] {2, 20});
        when(tableFaitsRepository.findAggregatedAvgResults(indicateur)).thenReturn(results);

        // when
        Map<Integer, AggregatedResultDto> map =
                tableFaitsService.findAgregationAvgByIndicateurAndApplication(indicateur);

        // then
        assertEquals(new BigDecimal("10.75"), map.get(1).getSumValeur());
        assertEquals(new BigDecimal("20"), map.get(2).getSumValeur());
    }

    @Test
    @DisplayName("Doit construire la map des indicateurs DevOps par module")
    void doitConstruireLaMapDesIndicateursDevopsParModule() {

        Date date = new Date();

        DevopsProjection projection = mock(DevopsProjection.class);

        when(projection.getIdModule()).thenReturn(10);
        when(projection.getDistanceCount()).thenReturn((int) 120d);
        when(projection.getNbDeploymentCount()).thenReturn((int) 45d);
        when(projection.getNbContributorCount()).thenReturn((int) 8d);

        when(tableFaitsRepository.findValueIndicateurModuleDevopsBrute(date))
                .thenReturn(List.of(projection));

        Map<Integer, IndicateurDevopsView> result =
                tableFaitsService.getIndicateurModuleDevops(date);

        assertThat(result).hasSize(1).containsKey(10);

        IndicateurDevopsView view = result.get(10);

        assertThat(view.getModuleId()).isEqualTo(10);
        assertThat(view.getDistanceCount()).isEqualTo("120");
        assertThat(view.getNbDeploymentCount()).isEqualTo("45");
        assertThat(view.getNbContributorCount()).isEqualTo("8");
    }

    @Test
    @DisplayName("Doit retourner une map vide lorsqu'aucun indicateur module n'existe")
    void doitRetournerUneMapVidePourLesModules() {

        Date date = new Date();

        when(tableFaitsRepository.findValueIndicateurModuleDevopsBrute(date)).thenReturn(List.of());

        Map<Integer, IndicateurDevopsView> result =
                tableFaitsService.getIndicateurModuleDevops(date);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Doit construire la map des indicateurs DevOps par application")
    void doitConstruireLaMapDesIndicateursDevopsParApplication() {

        Date date = new Date();

        DevopsProjection projection = mock(DevopsProjection.class);

        when(projection.getIdApplication()).thenReturn(20);
        when(projection.getDistanceCount()).thenReturn((int) 300d);
        when(projection.getNbDeploymentCount()).thenReturn((int) 90d);
        when(projection.getNbContributorCount()).thenReturn((int) 15d);

        when(tableFaitsRepository.findValueIndicateurApplicationDevopsBrute(date))
                .thenReturn(List.of(projection));

        Map<Integer, IndicateurDevopsView> result =
                tableFaitsService.getIndicateurApplicationDevops(date);

        assertThat(result).hasSize(1).containsKey(20);

        IndicateurDevopsView view = result.get(20);

        assertThat(view.getApplicationId()).isEqualTo(20);
        assertThat(view.getDistanceCount()).isEqualTo("300");
        assertThat(view.getNbDeploymentCount()).isEqualTo("90");
        assertThat(view.getNbContributorCount()).isEqualTo("15");
    }

    @Test
    @DisplayName("Doit retourner une map vide lorsqu'aucun indicateur application n'existe")
    void doitRetournerUneMapVidePourLesApplications() {

        Date date = new Date();

        when(tableFaitsRepository.findValueIndicateurApplicationDevopsBrute(date))
                .thenReturn(List.of());

        Map<Integer, IndicateurDevopsView> result =
                tableFaitsService.getIndicateurApplicationDevops(date);

        assertThat(result).isEmpty();
    }
}
