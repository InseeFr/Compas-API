package fr.insee.compas.service.maturitecloud.indicateur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.dto.MaturiteIndicateurDto;
import fr.insee.compas.exception.MaturiteIndicateurException;
import fr.insee.compas.model.maturite.MaturiteIndicateurTableProjection;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.util.MaturiteConstantes;
import fr.insee.compas.view.IndicateurMaturiteView;

@ExtendWith(MockitoExtension.class)
class MaturiteIndicateurServiceTest {

    @InjectMocks private MaturiteIndicateurService maturiteIndicateurService;

    @Mock private OscarService oscarService;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @Mock private MaturiteCalculatorService maturiteCalculatorService;

    @Mock private MaturiteMapperIndicateur maturiteMapperIndicateur;

    @Test
    void getIndicateurMaturite_shouldReturnViews_whenDataFound() {
        List<MaturiteIndicateurTableProjection> mockResult =
                List.of(mock(MaturiteIndicateurTableProjection.class));
        List<fr.insee.compas.model.oscar.Module> mockModules = List.of(mock(Module.class));
        Map<Integer, MaturiteConstantes.ModuleInfo> mockModuleMapped = Map.of();
        Map<Integer, List<MaturiteIndicateurDto>> mockByApp = Map.of();
        List<IndicateurMaturiteView> mockViews =
                List.of(IndicateurMaturiteView.builder().idApp(1).appName("App Test").build());

        when(tableFaitsRepository.getValuesByMaturiteIndicateur(any())).thenReturn(mockResult);
        when(oscarService.getModules()).thenReturn(mockModules);
        when(tableFaitsRepository.getMaturitesByIdIndicateur(any())).thenReturn(List.of());
        when(maturiteMapperIndicateur.getModulesMapped(mockModules)).thenReturn(mockModuleMapped);
        when(maturiteMapperIndicateur.resultatMaturiteIndicateurToMapByApp(
                        mockResult, mockModuleMapped))
                .thenReturn(mockByApp);
        when(maturiteMapperIndicateur.maturiteMapToListIndicateurMaturiteView(any(), any()))
                .thenReturn(mockViews);

        List<IndicateurMaturiteView> result = maturiteIndicateurService.getIndicateurMaturite();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getAppName()).isEqualTo("App Test");
        verify(maturiteMapperIndicateur).getModulesMapped(mockModules);
        verify(maturiteMapperIndicateur)
                .resultatMaturiteIndicateurToMapByApp(mockResult, mockModuleMapped);
        verify(maturiteMapperIndicateur).maturiteMapToListIndicateurMaturiteView(any(), any());
    }

    @Test
    void getIndicateurMaturite_shouldReturnEmptyList_whenNoResultFound() {
        when(tableFaitsRepository.getValuesByMaturiteIndicateur(any()))
                .thenReturn(Collections.emptyList());
        when(oscarService.getModules()).thenReturn(List.of());
        when(tableFaitsRepository.getMaturitesByIdIndicateur(any())).thenReturn(List.of());

        List<IndicateurMaturiteView> result = maturiteIndicateurService.getIndicateurMaturite();

        assertThat(result).isEmpty();
        verify(maturiteMapperIndicateur, never()).getModulesMapped(any());
    }

    @Test
    void getIndicateurMaturite_shouldThrowMaturiteIndicateurException_whenRepositoryFails() {
        when(tableFaitsRepository.getValuesByMaturiteIndicateur(any()))
                .thenThrow(new RuntimeException("Erreur BDD"));
        when(oscarService.getModules()).thenReturn(List.of());
        when(tableFaitsRepository.getMaturitesByIdIndicateur(any())).thenReturn(List.of());

        assertThatThrownBy(() -> maturiteIndicateurService.getIndicateurMaturite())
                .isInstanceOf(MaturiteIndicateurException.class)
                .hasMessageContaining("Erreur completion des futures");
    }

    @Test
    void getIndicateurMaturite_shouldThrowMaturiteIndicateurException_whenOscarFails() {
        when(tableFaitsRepository.getValuesByMaturiteIndicateur(any())).thenReturn(List.of());
        when(oscarService.getModules()).thenThrow(new RuntimeException("Erreur Oscar"));
        when(tableFaitsRepository.getMaturitesByIdIndicateur(any())).thenReturn(List.of());

        assertThatThrownBy(() -> maturiteIndicateurService.getIndicateurMaturite())
                .isInstanceOf(MaturiteIndicateurException.class)
                .hasMessageContaining("Erreur completion des futures");
    }
}
