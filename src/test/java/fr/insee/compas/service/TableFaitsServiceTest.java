package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.insee.compas.dto.AggregatedResultDto;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.TableFaitsRepository;
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
    void givenRawData_whenGetIndicateurModuleDevops_thenReturnView() {
        // given
        Object[][] raw = {{1, 7, 15, 4}};
        List<Object[]> faits = Arrays.asList(raw);
        when(tableFaitsRepository.findValueIndicateurModuleDevopsBrute()).thenReturn(faits);

        // when
        Map<Integer, IndicateurDevopsView> map = tableFaitsService.getIndicateurModuleDevops();

        // then
        IndicateurDevopsView view = map.get(1);
        assertEquals("7", view.getDistanceCount());
        assertEquals("15", view.getNbDeploymentCount());
        assertEquals("4", view.getNbContributorCount());
    }
}
