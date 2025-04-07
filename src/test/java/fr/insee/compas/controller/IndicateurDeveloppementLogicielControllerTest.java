package fr.insee.compas.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.service.DeveloppementLogicielService;
import fr.insee.compas.view.IndicateurApplicationDeveloppementLogicielView;
import fr.insee.compas.view.IndicateurModuleDeveloppementLogicielView;

class IndicateurDeveloppementLogicielControllerTest {

    @Mock private DeveloppementLogicielService developpementLogicielService;

    @InjectMocks private IndicateurDeveloppementLogicielController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testMiseAjourValeursDistance() throws Exception {
        // Given
        doNothing().when(developpementLogicielService).miseAJourIndicateurDistanceEnBaseDeDonnees();

        // When
        mockMvc.perform(put("/developpement-logiciel/update-distance-indicator"))
                // Then
                .andExpect(status().isOk());

        verify(developpementLogicielService, times(1)).miseAJourIndicateurDistanceEnBaseDeDonnees();
    }

    @Test
    void testMiseAjourValeursDeploymentCount() throws Exception {
        // Given
        doNothing()
                .when(developpementLogicielService)
                .miseAJourIndicateurDeploymentCountEnBaseDeDonnees(null, null);

        // When
        mockMvc.perform(put("/developpement-logiciel/update-deployment-count-indicator"))
                // Then
                .andExpect(status().isOk());

        verify(developpementLogicielService, times(1))
                .miseAJourIndicateurDeploymentCountEnBaseDeDonnees(null, null);
    }

    @Test
    void testGetGradeDistanceMEPModule() throws Exception {
        // Given
        List<IndicateurModuleDeveloppementLogicielView> mockResult =
                Arrays.asList(
                        new IndicateurModuleDeveloppementLogicielView(1, "A", 5),
                        new IndicateurModuleDeveloppementLogicielView(2, "B", 10));
        when(developpementLogicielService.calculateGradesModule(IndicateurType.NBR_JOUR_MEP))
                .thenReturn(mockResult);

        // When
        mockMvc.perform(get("/developpement-logiciel/get-distance-grade-module"))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].moduleId").value(1))
                .andExpect(jsonPath("$[0].note").value("A"))
                .andExpect(jsonPath("$[0].value").value(5));

        verify(developpementLogicielService, times(1))
                .calculateGradesModule(IndicateurType.NBR_JOUR_MEP);
    }

    @Test
    void testGetGradeDistanceMEPApplication() throws Exception {
        // Given
        List<IndicateurApplicationDeveloppementLogicielView> mockResult =
                Arrays.asList(
                        new IndicateurApplicationDeveloppementLogicielView(1, "A", 5),
                        new IndicateurApplicationDeveloppementLogicielView(2, "B", 10));
        when(developpementLogicielService.calculateGradesApplication(IndicateurType.NBR_JOUR_MEP))
                .thenReturn(mockResult);

        // When
        mockMvc.perform(get("/developpement-logiciel/get-distance-grade-application"))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(1))
                .andExpect(jsonPath("$[0].note").value("A"))
                .andExpect(jsonPath("$[0].value").value(5));

        verify(developpementLogicielService, times(1))
                .calculateGradesApplication(IndicateurType.NBR_JOUR_MEP);
    }

    @Test
    void testGetMonthlyDeploymentsGradeModule() throws Exception {
        // Given
        List<IndicateurModuleDeveloppementLogicielView> mockResult =
                Arrays.asList(
                        new IndicateurModuleDeveloppementLogicielView(1, "A", 5),
                        new IndicateurModuleDeveloppementLogicielView(2, "B", 10));
        when(developpementLogicielService.calculateGradesModule(IndicateurType.DEPLOYMENT_COUNT))
                .thenReturn(mockResult);

        // When
        mockMvc.perform(get("/developpement-logiciel/get-monthly-deployments-grade-module"))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].moduleId").value(1))
                .andExpect(jsonPath("$[0].note").value("A"))
                .andExpect(jsonPath("$[0].value").value(5));

        verify(developpementLogicielService, times(1))
                .calculateGradesModule(IndicateurType.DEPLOYMENT_COUNT);
    }

    @Test
    void testGetMonthlyDeploymentsGradeApplication() throws Exception {
        // Given
        List<IndicateurApplicationDeveloppementLogicielView> mockResult =
                Arrays.asList(
                        new IndicateurApplicationDeveloppementLogicielView(1, "A", 5),
                        new IndicateurApplicationDeveloppementLogicielView(2, "B", 10));
        when(developpementLogicielService.calculateGradesApplication(
                        IndicateurType.DEPLOYMENT_COUNT))
                .thenReturn(mockResult);

        // When
        mockMvc.perform(get("/developpement-logiciel/get-monthly-deployments-grade-application"))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(1))
                .andExpect(jsonPath("$[0].note").value("A"))
                .andExpect(jsonPath("$[0].value").value(5));

        verify(developpementLogicielService, times(1))
                .calculateGradesApplication(IndicateurType.DEPLOYMENT_COUNT);
    }
}
