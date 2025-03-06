package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.dto.AggregatedSumResultDto;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.view.IndicateurApplicationDeveloppementLogicielView;
import fr.insee.compas.view.IndicateurModuleDeveloppementLogicielView;

@ExtendWith(MockitoExtension.class)
class IndicateurOscarServiceTest {

    @Mock private TableFaitsService tableFaitsService;

    @Mock private OscarService oscarService;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @InjectMocks private IndicateurOscarService indicateurOscarService;

    @Test
    void testCalculateDistanceGrades_WithValues() {
        TableFaits faits1 =
                TableFaits.builder()
                        .idModule(1)
                        .idIndicateur(3)
                        .date(LocalDate.now())
                        .valeur(BigDecimal.valueOf(20))
                        .idSource(0)
                        .build();
        TableFaits faits2 =
                TableFaits.builder()
                        .idModule(2)
                        .idIndicateur(3)
                        .date(LocalDate.now())
                        .valeur(BigDecimal.valueOf(50))
                        .idSource(0)
                        .build();
        List<TableFaits> latestValues = List.of(faits1, faits2);
        Module module1 =
                Module.builder()
                        .id(1)
                        .modName("name1")
                        .sndi("sndi1")
                        .domaineSndi("domaine1")
                        .keySonar("keySonar1")
                        .build();
        Module module2 =
                Module.builder()
                        .id(2)
                        .modName("name2")
                        .sndi("sndi2")
                        .domaineSndi("domaine2")
                        .keySonar("keySonar2")
                        .build();
        List<Module> modules = List.of(module1, module2);

        when(oscarService.getModules()).thenReturn(modules);
        when(tableFaitsRepository.findLatestValueByIndicateurByModule(
                        IndicateurType.NBR_JOUR_MEP.getValue()))
                .thenReturn(latestValues);

        List<IndicateurModuleDeveloppementLogicielView> result =
                indicateurOscarService.calculateDistanceGradesModule();

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getNoteDistance());
        assertEquals("B", result.get(1).getNoteDistance());
    }

    @Test
    void testMiseAJourLinesTableFaitsEnBaseDeDonnees_WithDate() {
        LocalDate now = LocalDate.now(); // Date actuelle pour le test
        Module module1 =
                Module.builder()
                        .id(1)
                        .modName("name1")
                        .domaineSndi("sndi1")
                        .keySonar("keySonar1")
                        .dateDerniereLivraisonEnProduction(LocalDate.now().minusDays(2))
                        .build();
        Module module2 =
                Module.builder()
                        .id(2)
                        .modName("name2")
                        .domaineSndi("sndi2")
                        .keySonar("keySonar2")
                        .build();

        List<Module> modules = List.of(module1, module2);

        when(oscarService.getModules()).thenReturn(modules);
        indicateurOscarService.miseAJourLinesTableFaitsEnBaseDeDonnees();

        verify(tableFaitsRepository)
                .save(
                        Mockito.argThat(
                                tableFaits ->
                                        tableFaits.getIdModule().equals(1)
                                                && tableFaits.getIdIndicateur().equals(301)
                                                && tableFaits
                                                        .getValeur()
                                                        .equals(BigDecimal.valueOf(2))
                                                && tableFaits.getDate().equals(now)));

        verify(tableFaitsRepository)
                .save(
                        Mockito.argThat(
                                tableFaits ->
                                        tableFaits.getIdModule().equals(2)
                                                && tableFaits.getIdIndicateur().equals(301)
                                                && tableFaits
                                                        .getValeur()
                                                        .equals(BigDecimal.valueOf(-1))
                                                && tableFaits.getDate().equals(now)));
    }

    @Test
    void testCalculateDistanceGradesApplication_WithValues() {
        // Mock des valeurs agrégées
        Map<Integer, AggregatedSumResultDto> latestValues = new HashMap<>();
        latestValues.put(
                1,
                new AggregatedSumResultDto(BigDecimal.valueOf(10.0), 1)); // Simulation de données

        // Mock des applications
        List<Application> applications =
                List.of(Application.builder().idApplication(1).appName("App1").build());

        when(tableFaitsService.findAgregationAvgByIndicateurAndApplication(anyInt()))
                .thenReturn(latestValues);
        when(oscarService.getApplications()).thenReturn(applications);

        // Appel de la méthode testée
        List<IndicateurApplicationDeveloppementLogicielView> result =
                indicateurOscarService.calculateDistanceGradesApplication();

        // Vérifications
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getApplicationId());
        assertEquals(10, result.getFirst().getValueDistance());
    }

    @Test
    void testCalculateDistanceGradesApplication_NoValues() {
        // Aucun latestValues trouvé
        when(tableFaitsService.findAgregationAvgByIndicateurAndApplication(anyInt()))
                .thenReturn(new HashMap<>());

        // Mock des applications
        List<Application> applications =
                List.of(Application.builder().idApplication(1).appName("App1").build());

        when(oscarService.getApplications()).thenReturn(applications);

        // Appel de la méthode testée
        List<IndicateurApplicationDeveloppementLogicielView> result =
                indicateurOscarService.calculateDistanceGradesApplication();

        // Vérifications
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getApplicationId());
        assertEquals(Notation.NR.getGrade(), result.getFirst().getNoteDistance());
    }
}
