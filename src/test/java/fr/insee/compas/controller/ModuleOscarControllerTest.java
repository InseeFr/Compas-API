package fr.insee.compas.controller;

import static org.mockito.Mockito.*;
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

import fr.insee.compas.model.compas.ModuleOscar;
import fr.insee.compas.repository.ModuleOscarRepository;
import fr.insee.compas.service.OscarService;

class ModuleOscarControllerTest {

    @Mock private ModuleOscarRepository moduleRepository;

    @Mock private OscarService oscarService;

    @InjectMocks private ModuleOscarController moduleOscarController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(moduleOscarController).build();
    }

    @Test
    void testGetAllModules() throws Exception {
        // Given
        List<ModuleOscar> mockModules =
                Arrays.asList(new ModuleOscar(1, true), new ModuleOscar(2, true));
        when(moduleRepository.findAll()).thenReturn(mockModules);

        // When
        mockMvc.perform(get("/module-oscar/find-all"))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idModule").value(1))
                .andExpect(jsonPath("$[0].actif").value(true))
                .andExpect(jsonPath("$[1].idModule").value(2))
                .andExpect(jsonPath("$[1].actif").value(true));

        verify(moduleRepository, times(1)).findAll();
    }

    @Test
    void testGetActiveModules() throws Exception {
        // Given
        List<ModuleOscar> mockActiveModules =
                Arrays.asList(new ModuleOscar(1, true), new ModuleOscar(2, true));
        when(moduleRepository.findByActif(true)).thenReturn(mockActiveModules);

        // When
        mockMvc.perform(get("/module-oscar/find-module-actif"))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idModule").value(1))
                .andExpect(jsonPath("$[0].actif").value(true))
                .andExpect(jsonPath("$[1].idModule").value(2))
                .andExpect(jsonPath("$[1].actif").value(true));

        verify(moduleRepository, times(1)).findByActif(true);
    }

    @Test
    void testUpdateModules() throws Exception {
        // Given
        doNothing().when(oscarService).miseAjourModuleOscarEnBaseDeDonnees();

        // When
        mockMvc.perform(put("/module-oscar/mise-a-jour"))
                // Then
                .andExpect(status().isOk());

        verify(oscarService, times(1)).miseAjourModuleOscarEnBaseDeDonnees();
    }
}
