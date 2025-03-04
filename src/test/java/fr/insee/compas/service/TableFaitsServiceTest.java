package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.TableFaitsRepository;

class TableFaitsServiceTest {

    private TableFaitsService tableFaitsService;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tableFaitsService = new TableFaitsService(tableFaitsRepository);
    }

    @Test
    void testGetMapMetricByModule() {
        // Arrange
        int indicateur = 1;
        TableFaits tableFait1 = new TableFaits();
        tableFait1.setIdModule(101);
        TableFaits tableFait2 = new TableFaits();
        tableFait2.setIdModule(102);

        List<TableFaits> mockMetrics = Arrays.asList(tableFait1, tableFait2);

        when(tableFaitsRepository.findLatestValueByIndicateurByModule(indicateur))
                .thenReturn(mockMetrics);

        // Act
        Map<Integer, TableFaits> result = tableFaitsService.getMapMetricByModule(indicateur);

        // Assert
        assertEquals(2, result.size());
        assertEquals(tableFait1, result.get(101));
        assertEquals(tableFait2, result.get(102));
        verify(tableFaitsRepository, times(1)).findLatestValueByIndicateurByModule(indicateur);
    }
}
