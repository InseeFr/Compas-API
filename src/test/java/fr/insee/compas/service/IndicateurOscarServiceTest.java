package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.Indicateur;
import fr.insee.compas.model.compas.ModuleGradeDistance;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.ModuleOscarRepository;
import fr.insee.compas.repository.TableFaitsRepository;

@ExtendWith(MockitoExtension.class)
class IndicateurOscarServiceTest {

    @Mock private OscarService oscarService;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @Mock private ModuleOscarRepository moduleOscarRepo;

    @InjectMocks private IndicateurOscarService indicateurOscarService;

    @Test
    void testCalculateDistanceGrades_WithValues() throws IOException {
        List<TableFaits> latestValues =
                List.of(
                        new TableFaits(1, 3, LocalDate.now(), BigDecimal.valueOf(20), 0),
                        new TableFaits(2, 3, LocalDate.now(), BigDecimal.valueOf(50), 0));
        List<Module> modules =
                List.of(
                        new Module(1, "name1", "domaine1", "keySonar1", "sndi1"),
                        new Module(2, "name2", "domaine2", "keySonar2", "sndi2"));

        Mockito.when(oscarService.getModules()).thenReturn(modules);
        Mockito.when(
                        tableFaitsRepository.findLatestValueByIndicateur(
                                Indicateur.NBR_JOUR_MEP.getValue()))
                .thenReturn(latestValues);

        Map<Integer, ModuleGradeDistance> result = indicateurOscarService.calculateDistanceGrades();

        assertEquals(2, result.size());
        assertEquals("A", result.get(1).getGrade());
        assertEquals("B", result.get(2).getGrade());
        assertEquals("name1", result.get(1).getName());
        assertEquals("sndi1", result.get(1).getSndi());
        assertEquals("domaine1", result.get(1).getDomaine());
        assertEquals("name2", result.get(2).getName());
        assertEquals("sndi2", result.get(2).getSndi());
        assertEquals("domaine2", result.get(2).getDomaine());
    }

    @Test
    void testMiseAJourLinesTableFaitsEnBaseDeDonnees_WithDate() throws IOException {
        LocalDate now = LocalDate.now(); // Date actuelle pour le test
        List<Module> modules =
                List.of(
                        new Module(1, "name1", null, null, null, LocalDate.now().minusDays(2)),
                        new Module(2, "name2", null, null, null, null));

        Mockito.when(oscarService.getModules()).thenReturn(modules);
        indicateurOscarService.miseAJourLinesTableFaitsEnBaseDeDonnees();

        verify(tableFaitsRepository)
                .save(
                        Mockito.argThat(
                                tableFaits -> {
                                    return tableFaits.getIdModule().equals(1)
                                            && tableFaits.getIdIndicateur().equals(301)
                                            && tableFaits.getValeur().equals(BigDecimal.valueOf(2))
                                            && tableFaits.getDate().equals(now);
                                }));

        verify(tableFaitsRepository)
                .save(
                        Mockito.argThat(
                                tableFaits -> {
                                    return tableFaits.getIdModule().equals(2)
                                            && tableFaits.getIdIndicateur().equals(301)
                                            && tableFaits.getValeur().equals(BigDecimal.valueOf(-1))
                                            && tableFaits.getDate().equals(now);
                                }));
    }
}
