package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import fr.insee.compas.dto.AggregatedResultDto;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.view.IndicateurApplicationDeveloppementLogicielView;
import fr.insee.compas.view.IndicateurModuleDeveloppementLogicielView;

@ExtendWith(MockitoExtension.class)
class DeveloppementLogicielServiceTest {

    @Mock private TableFaitsService tableFaitsService;

    @Mock private OscarService oscarService;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @InjectMocks private DeveloppementLogicielService developpementLogicielService;

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
                developpementLogicielService.calculateGradesModule(IndicateurType.NBR_JOUR_MEP);

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getNote());
        assertEquals("B", result.get(1).getNote());
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
                        .statut("en production")
                        .dateDerniereLivraisonEnProduction(LocalDate.now().minusDays(2))
                        .build();
        Module module2 =
                Module.builder()
                        .id(2)
                        .modName("name2")
                        .domaineSndi("sndi2")
                        .statut("en production")
                        .keySonar("keySonar2")
                        .build();

        List<Module> modules = List.of(module1, module2);

        when(oscarService.getModules()).thenReturn(modules);
        developpementLogicielService.miseAJourIndicateurDistanceEnBaseDeDonnees();

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
                                                        .equals(BigDecimal.valueOf(-2))
                                                && tableFaits.getDate().equals(now)));
    }

    @Test
    void testCalculateDistanceGradesApplication_WithValues() {
        // Mock des valeurs agrégées
        Map<Integer, AggregatedResultDto> latestValues = new HashMap<>();
        latestValues.put(
                1, new AggregatedResultDto(BigDecimal.valueOf(10.0), 1)); // Simulation de données

        // Mock des applications
        List<Application> applications =
                List.of(Application.builder().idApplication(1).appName("App1").build());

        when(tableFaitsService.findAgregationAvgByIndicateurAndApplication(anyInt()))
                .thenReturn(latestValues);
        when(oscarService.getApplications()).thenReturn(applications);

        // Appel de la méthode testée
        List<IndicateurApplicationDeveloppementLogicielView> result =
                developpementLogicielService.calculateGradesApplication(
                        IndicateurType.NBR_JOUR_MEP);

        // Vérifications
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getApplicationId());
        assertEquals(10, result.getFirst().getValue());
    }

    @Test
    void testCalculateDistanceGradesApplication_NoValues() {
        Map<Integer, AggregatedResultDto> latestValues = new HashMap<>();
        latestValues.put(
                1, new AggregatedResultDto(new BigDecimal(-2), 1)); // La valeur est 0 pour la clé 1

        when(tableFaitsService.findAgregationAvgByIndicateurAndApplication(anyInt()))
                .thenReturn(latestValues);
        List<Application> applications =
                List.of(Application.builder().idApplication(1).appName("App1").build());
        when(oscarService.getApplications()).thenReturn(applications);

        List<IndicateurApplicationDeveloppementLogicielView> result =
                developpementLogicielService.calculateGradesApplication(
                        IndicateurType.NBR_JOUR_MEP);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getApplicationId());
        assertEquals(Notation.NR.getGrade(), result.getFirst().getNote());
    }

    @Test
    void testMiseAJourIndicateurDeploymentCountEnBaseDeDonnees() {
        // Given : List de modules simulée et un historique de modules simulé
        LocalDate now = LocalDate.now();
        LocalDateTime startDate = now.minusMonths(1).atStartOfDay(); // Date de début il y a un mois
        LocalDateTime endDate = now.atTime(23, 59, 59); // Date de fin aujourd'hui à 23:59:59

        Module module1 =
                Module.builder()
                        .id(1)
                        .modName("Module1")
                        .sourceCreation("Puppet6")
                        .statut("en production")
                        .build();
        Module module2 =
                Module.builder()
                        .id(2)
                        .modName("Module2")
                        .sourceCreation("Puppet6")
                        .statut("en production")
                        .build();

        List<Module> modules = List.of(module1, module2);

        List<ModuleHistorique> moduleHistorique1 =
                List.of(
                        ModuleHistorique.builder()
                                .auteurOperation("service-account-oscar4-service")
                                .dateOperation(LocalDateTime.now().minusDays(5))
                                .operation("MODIFICATION")
                                .statut("en production")
                                .build());
        List<ModuleHistorique> moduleHistorique2 =
                List.of(
                        ModuleHistorique.builder()
                                .auteurOperation("service-account-oscar4-service")
                                .dateOperation(LocalDateTime.now().minusDays(2))
                                .operation("MODIFICATION")
                                .statut("en production")
                                .build());

        // Créer un Map avec les historiques des modules
        Map<String, List<ModuleHistorique>> moduleHistoriqueMap = new HashMap<>();
        moduleHistoriqueMap.put("1", moduleHistorique1);
        moduleHistoriqueMap.put("2", moduleHistorique2);

        // Simuler les appels du service
        when(oscarService.getModules()).thenReturn(modules);
        when(oscarService.getModulesHistorique()).thenReturn(moduleHistoriqueMap);

        // When : On appelle la méthode miseAJourIndicateurDeploymentCountEnBaseDeDonnees avec les
        // dates
        developpementLogicielService.miseAJourIndicateurDeploymentCountEnBaseDeDonnees(
                startDate, endDate);

        // Then : Vérifier que save a été appelé avec les bonnes valeurs
        verify(tableFaitsRepository)
                .save(
                        Mockito.argThat(
                                tableFaits ->
                                        tableFaits.getIdModule().equals(1)
                                                && tableFaits
                                                        .getIdIndicateur()
                                                        .equals(
                                                                IndicateurType.DEPLOYMENT_COUNT
                                                                        .getValue())
                                                && tableFaits
                                                        .getValeur()
                                                        .equals(BigDecimal.valueOf(1))
                                                && tableFaits.getDate().equals(now)));

        verify(tableFaitsRepository)
                .save(
                        Mockito.argThat(
                                tableFaits ->
                                        tableFaits.getIdModule().equals(2)
                                                && tableFaits
                                                        .getIdIndicateur()
                                                        .equals(
                                                                IndicateurType.DEPLOYMENT_COUNT
                                                                        .getValue())
                                                && tableFaits
                                                        .getValeur()
                                                        .equals(BigDecimal.valueOf(1))
                                                && tableFaits.getDate().equals(now)));
    }

    @Test
    void testMiseAJourIndicateurDeploymentCountEnBaseDeDonnees_NoHistory() {
        // Given : List de modules simulée et pas d'historique pour ces modules
        LocalDate now = LocalDate.now();
        LocalDateTime startDate = now.minusMonths(1).atStartOfDay(); // Date de début il y a un mois
        LocalDateTime endDate = now.atTime(23, 59, 59); // Date de fin aujourd'hui à 23:59:59

        Module module1 = Module.builder().id(1).modName("Module1").build();
        Module module2 = Module.builder().id(2).modName("Module2").build();

        List<Module> modules = List.of(module1, module2);

        // Aucun historique pour les modules
        List<ModuleHistorique> moduleHistorique1 = List.of();
        List<ModuleHistorique> moduleHistorique2 = List.of();

        // Créer un Map avec les historiques des modules (qui sont vides ici)
        Map<String, List<ModuleHistorique>> moduleHistoriqueMap = new HashMap<>();
        moduleHistoriqueMap.put("1", moduleHistorique1);
        moduleHistoriqueMap.put("2", moduleHistorique2);

        // Simuler les appels du service
        when(oscarService.getModules()).thenReturn(modules);
        when(oscarService.getModulesHistorique()).thenReturn(moduleHistoriqueMap);

        // When : On appelle la méthode miseAJourIndicateurDeploymentCountEnBaseDeDonnees avec les
        // dates
        developpementLogicielService.miseAJourIndicateurDeploymentCountEnBaseDeDonnees(
                startDate, endDate);

        // Then : Vérifier que save a été appelé avec la valeur -1 pour les modules sans historique
        verify(tableFaitsRepository)
                .save(
                        Mockito.argThat(
                                tableFaits ->
                                        tableFaits.getIdModule().equals(1)
                                                && tableFaits
                                                        .getIdIndicateur()
                                                        .equals(
                                                                IndicateurType.DEPLOYMENT_COUNT
                                                                        .getValue())
                                                && tableFaits
                                                        .getValeur()
                                                        .equals(BigDecimal.valueOf(-1))
                                                && tableFaits.getDate().equals(now)));

        verify(tableFaitsRepository)
                .save(
                        Mockito.argThat(
                                tableFaits ->
                                        tableFaits.getIdModule().equals(2)
                                                && tableFaits
                                                        .getIdIndicateur()
                                                        .equals(
                                                                IndicateurType.DEPLOYMENT_COUNT
                                                                        .getValue())
                                                && tableFaits
                                                        .getValeur()
                                                        .equals(BigDecimal.valueOf(-1))
                                                && tableFaits.getDate().equals(now)));
    }

    @ParameterizedTest
    @CsvSource({"25, A", "16, B", "11, C", "6, D", "0, E"})
    void testGetGradeFromDeploymentCount(Integer count, String expectedGrade) {
        // Given: A DeveloppementLogicielService and a count value
        DeveloppementLogicielService service =
                new DeveloppementLogicielService(
                        tableFaitsRepository, oscarService, tableFaitsService);

        // When: We invoke the private method getGradeFromDeploymentCount
        String result =
                ReflectionTestUtils.invokeMethod(service, "getGradeFromDeploymentCount", count);

        // Then: The returned grade should match the expected grade
        assertEquals(expectedGrade, result);
    }
}
