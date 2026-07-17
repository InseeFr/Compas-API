package fr.insee.compas.service.greenit;

import static fr.insee.compas.util.greenit.GreenITutils.ViewGreen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.compas.dto.green.GreenKubeDto;
import fr.insee.compas.dto.green.GreenVmDto;
import fr.insee.compas.logic.update.greenit.kube.KubeMetricsCsvUpdater;
import fr.insee.compas.logic.update.greenit.vm.ApplishareMetricsApiUpdater;
import fr.insee.compas.logic.update.greenit.vm.VmMetricsCsvUpdater;
import fr.insee.compas.mapper.green.GreenItMapper;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.repository.projection.green.GreenItAppKubeProjection;
import fr.insee.compas.repository.projection.green.GreenItAppVmProjection;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.view.green.IndicateurAppGreenBaseView;
import fr.insee.compas.view.green.IndicateurAppGreenKubeView;
import fr.insee.compas.view.green.IndicateurAppGreenVmView;

@ExtendWith(MockitoExtension.class)
class GreenItServiceTest {

    @Mock private OscarService oscarService;

    @Mock private GreenItMapper greenItMapper;
    @Mock private VmMetricsCsvUpdater vmMetricsCsvUpdater;
    @Mock private KubeMetricsCsvUpdater kubeMetricsCsvUpdater;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @InjectMocks private GreenItService greenItService;

    @Mock private ApplishareMetricsApiUpdater applishareMetricsApiUpdater;

    @Test
    void miseAJourVmMetricsGreenItFromFile_shouldDelegateToVmMetricsCsvUpdater() {
        MultipartFile file = mock(MultipartFile.class);
        LocalDate fileDate = LocalDate.of(2026, 6, 22);

        greenItService.miseAJourVmMetricsGreenItFromFile(file, fileDate);

        verify(vmMetricsCsvUpdater).miseAJourIndicateursGreenItFromFile(file, fileDate);
        verifyNoInteractions(kubeMetricsCsvUpdater, applishareMetricsApiUpdater);
        verifyNoMoreInteractions(vmMetricsCsvUpdater);
    }

    @Test
    void miseAJourKubeMetricsGreenItFromFile_shouldDelegateToKubeMetricsCsvUpdater() {
        MultipartFile file = mock(MultipartFile.class);
        LocalDate fileDate = LocalDate.of(2026, 6, 22);

        greenItService.miseAJourKubeMetricsGreenItFromFile(file, fileDate);

        verify(kubeMetricsCsvUpdater).miseAJourIndicateursGreenItFromFile(file, fileDate);
        verifyNoInteractions(vmMetricsCsvUpdater, applishareMetricsApiUpdater);
        verifyNoMoreInteractions(kubeMetricsCsvUpdater);
    }

    @Test
    void miseAJourApplishareMetricsGreenItFromApi_shouldDelegateToApplishareMetricsApiUpdater() {
        greenItService.miseAJourApplishareMetricsGreenItFromApi();

        verify(applishareMetricsApiUpdater).miseAJourIndicateursGreenItFromApi();
        verifyNoInteractions(vmMetricsCsvUpdater, kubeMetricsCsvUpdater);
        verifyNoMoreInteractions(applishareMetricsApiUpdater);
    }

    @Test
    void getIndicateursApplicationGreenIT_vm_success() {
        when(tableFaitsRepository.findLastDateIndicateur())
                .thenReturn(List.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)));

        Date origine =
                Date.from(
                        LocalDate.of(2024, 1, 2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date passee =
                Date.from(
                        LocalDate.of(2024, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Application app =
                Application.builder()
                        .idApplication(1)
                        .appName("app-test")
                        .domaineSndi("dev")
                        .domaineFonctionnel("fonc")
                        .sndi("service")
                        .build();

        GreenItAppVmProjection projection = mock(GreenItAppVmProjection.class);
        GreenItAppVmProjection projectionHist = mock(GreenItAppVmProjection.class);

        when(projection.getIdApplication()).thenReturn(1);
        when(projectionHist.getIdApplication()).thenReturn(1);

        when(tableFaitsRepository.getGreenItAppVm(origine)).thenReturn(List.of(projection));
        when(tableFaitsRepository.getGreenItAppVm(passee)).thenReturn(List.of(projectionHist));

        when(oscarService.getApplications()).thenReturn(List.of(app));

        GreenVmDto dto = GreenVmDto.builder().applicationId(1).build();
        when(greenItMapper.buildIndicateurDtoVm(
                        eq(1), eq(app), eq(projection), eq(projectionHist), any(LocalDate.class)))
                .thenReturn(dto);

        IndicateurAppGreenVmView view =
                IndicateurAppGreenVmView.builder()
                        .applicationId(1)
                        .applicationName("app-test")
                        .build();
        when(greenItMapper.mapToView(dto)).thenReturn(view);

        List<IndicateurAppGreenBaseView> result =
                greenItService.getIndicateursApplicationGreenIT(ViewGreen.VM, origine, passee);

        assertEquals(1, result.size());
        verify(oscarService).getApplications();
        verify(tableFaitsRepository).getGreenItAppVm(origine);
        verify(tableFaitsRepository).getGreenItAppVm(passee);
        verify(greenItMapper)
                .buildIndicateurDtoVm(
                        eq(1), eq(app), eq(projection), eq(projectionHist), any(LocalDate.class));
        verify(greenItMapper).mapToView(dto);
        verify(greenItMapper, never()).buildIndicateurKubeDto(any(), any(), any(), any(), any());
    }

    @Test
    void getIndicateursApplicationGreenIT_kube_success() {
        when(tableFaitsRepository.findLastDateIndicateur())
                .thenReturn(List.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)));

        Date origine =
                Date.from(
                        LocalDate.of(2024, 1, 2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date passee =
                Date.from(
                        LocalDate.of(2024, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Application app = Application.builder().idApplication(1).appName("app-test").build();

        GreenItAppKubeProjection projection = mock(GreenItAppKubeProjection.class);
        GreenItAppKubeProjection projectionHist = mock(GreenItAppKubeProjection.class);

        when(projection.getIdApplication()).thenReturn(1);
        when(projectionHist.getIdApplication()).thenReturn(1);

        when(tableFaitsRepository.getGreenItAppKube(origine)).thenReturn(List.of(projection));
        when(tableFaitsRepository.getGreenItAppKube(passee)).thenReturn(List.of(projectionHist));

        when(oscarService.getApplications()).thenReturn(List.of(app));

        GreenKubeDto dto = GreenKubeDto.builder().applicationId(1).build();
        when(greenItMapper.buildIndicateurKubeDto(
                        eq(1), eq(app), eq(projection), eq(projectionHist), any(LocalDate.class)))
                .thenReturn(dto);

        IndicateurAppGreenKubeView view =
                IndicateurAppGreenKubeView.builder()
                        .applicationId(1)
                        .applicationName("app-test")
                        .build();
        when(greenItMapper.mapToView(dto)).thenReturn(view);

        List<IndicateurAppGreenBaseView> result =
                greenItService.getIndicateursApplicationGreenIT(ViewGreen.KUBE, origine, passee);

        assertEquals(1, result.size());
        verify(greenItMapper)
                .buildIndicateurKubeDto(
                        eq(1), eq(app), eq(projection), eq(projectionHist), any(LocalDate.class));
        verify(greenItMapper, never()).buildIndicateurDtoVm(any(), any(), any(), any(), any());
    }

    @Test
    void getIndicateursApplicationGreenIT_invalidDates_shouldThrowException() {
        Date origine =
                Date.from(
                        LocalDate.of(2024, 1, 2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date passee =
                Date.from(
                        LocalDate.of(2024, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        when(tableFaitsRepository.findLastDateIndicateur())
                .thenReturn(List.of()); // vide => invalid

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        greenItService.getIndicateursApplicationGreenIT(
                                ViewGreen.VM, origine, passee));

        verifyNoInteractions(oscarService, greenItMapper);
    }

    @Test
    void getIndicateursApplicationGreenIT_shouldUseAnonymousApplication_whenNull() {
        when(tableFaitsRepository.findLastDateIndicateur())
                .thenReturn(List.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)));

        Date origine =
                Date.from(
                        LocalDate.of(2024, 1, 2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date passee =
                Date.from(
                        LocalDate.of(2024, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        GreenItAppVmProjection projection = mock(GreenItAppVmProjection.class);
        GreenItAppVmProjection projectionHist = mock(GreenItAppVmProjection.class);

        when(projection.getIdApplication()).thenReturn(1);
        when(projectionHist.getIdApplication()).thenReturn(1);

        when(tableFaitsRepository.getGreenItAppVm(origine)).thenReturn(List.of(projection));
        when(tableFaitsRepository.getGreenItAppVm(passee)).thenReturn(List.of(projectionHist));

        when(oscarService.getApplications()).thenReturn(Collections.emptyList());

        GreenVmDto dto = GreenVmDto.builder().applicationId(1).applicationName("anonyme").build();
        when(greenItMapper.buildIndicateurDtoVm(
                        eq(1), isNull(), eq(projection), eq(projectionHist), any(LocalDate.class)))
                .thenReturn(dto);
        when(greenItMapper.mapToView(dto))
                .thenReturn(IndicateurAppGreenVmView.builder().applicationName("anonyme").build());

        List<IndicateurAppGreenBaseView> result =
                greenItService.getIndicateursApplicationGreenIT(ViewGreen.VM, origine, passee);

        assertEquals(1, result.size());
        verify(greenItMapper)
                .buildIndicateurDtoVm(
                        eq(1), isNull(), eq(projection), eq(projectionHist), any(LocalDate.class));
    }
}
