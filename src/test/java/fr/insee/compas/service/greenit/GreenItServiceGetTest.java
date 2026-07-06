package fr.insee.compas.service.greenit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import fr.insee.compas.logic.update.greenit.kube.KubeMetricsCsvUpdater;
import fr.insee.compas.logic.update.greenit.vm.ApplishareMetricsApiUpdater;
import fr.insee.compas.logic.update.greenit.vm.VmMetricsCsvUpdater;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.dto.GreenItAppDto;
import fr.insee.compas.mapper.green.GreenItMapper;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.repository.projection.GreenItAppProjection;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.view.IndicateurApplicationGreenITView;
import org.springframework.web.multipart.MultipartFile;

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
        // Given
        MultipartFile file = mock(MultipartFile.class);
        LocalDate fileDate = LocalDate.of(2026, 6, 22);

        // When
        greenItService.miseAJourVmMetricsGreenItFromFile(file, fileDate);

        // Then
        verify(vmMetricsCsvUpdater).miseAJourIndicateursGreenItFromFile(file, fileDate);
        verifyNoInteractions(kubeMetricsCsvUpdater, applishareMetricsApiUpdater);
        verifyNoMoreInteractions(vmMetricsCsvUpdater);
    }

    @Test
    void miseAJourKubeMetricsGreenItFromFile_shouldDelegateToKubeMetricsCsvUpdater() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        LocalDate fileDate = LocalDate.of(2026, 6, 22);

        // When
        greenItService.miseAJourKubeMetricsGreenItFromFile(file, fileDate);

        // Then
        verify(kubeMetricsCsvUpdater).miseAJourIndicateursGreenItFromFile(file, fileDate);
        verifyNoInteractions(vmMetricsCsvUpdater, applishareMetricsApiUpdater);
        verifyNoMoreInteractions(kubeMetricsCsvUpdater);
    }

    @Test
    void miseAJourApplishareMetricsGreenItFromApi_shouldDelegateToApplishareMetricsApiUpdater() {
        // When
        greenItService.miseAJourApplishareMetricsGreenItFromApi();

        // Then
        verify(applishareMetricsApiUpdater).miseAJourIndicateursGreenItFromApi();
        verifyNoInteractions(vmMetricsCsvUpdater, kubeMetricsCsvUpdater);
        verifyNoMoreInteractions(applishareMetricsApiUpdater);
    }

    @Test
    void getIndicateursApplicationGreenIT_success() {

        when(tableFaitsRepository.findLastDateIndicateur())
                .thenReturn(List.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)));
        // GIVEN
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

        GreenItAppProjection projection = mock(GreenItAppProjection.class);
        GreenItAppProjection projectionHist = mock(GreenItAppProjection.class);

        when(projection.getRamAlloue()).thenReturn(BigDecimal.TEN);
        when(projectionHist.getRamAlloue()).thenReturn(BigDecimal.ONE);

        when(projection.getRamMaxi()).thenReturn(BigDecimal.TEN);
        when(projectionHist.getRamMaxi()).thenReturn(BigDecimal.ONE);

        when(projection.getDisqueAlloue()).thenReturn(BigDecimal.TEN);
        when(projectionHist.getDisqueAlloue()).thenReturn(BigDecimal.ONE);

        when(projection.getDisqueConsomme()).thenReturn(BigDecimal.TEN);
        when(projectionHist.getDisqueConsomme()).thenReturn(BigDecimal.ONE);

        when(projection.getCpuAllouee()).thenReturn(BigDecimal.TEN);
        when(projectionHist.getCpuAllouee()).thenReturn(BigDecimal.ONE);

        when(projection.getCpuMaxi()).thenReturn(BigDecimal.TEN);
        when(projectionHist.getCpuMaxi()).thenReturn(BigDecimal.ONE);

        when(projection.getConsoElec()).thenReturn(BigDecimal.TEN);
        when(projectionHist.getConsoElec()).thenReturn(BigDecimal.ONE);

        when(projection.getNbrVM()).thenReturn(BigDecimal.TEN);
        when(projectionHist.getNbrVM()).thenReturn(BigDecimal.ONE);

        when(projection.getRamConsommee()).thenReturn(10L);
        when(projectionHist.getRamConsommee()).thenReturn(1L);

        when(projection.getCpuConsomme()).thenReturn(BigDecimal.TEN);
        when(projectionHist.getCpuConsomme()).thenReturn(BigDecimal.ONE);

        when(tableFaitsRepository.getGreenItApp(origine)).thenReturn(List.of(projection));

        when(tableFaitsRepository.getGreenItApp(passee)).thenReturn(List.of(projectionHist));

        when(oscarService.getApplications()).thenReturn(List.of(app));

        IndicateurApplicationGreenITView view =
                IndicateurApplicationGreenITView.builder()
                        .applicationId(1)
                        .applicationName("app-test")
                        .build();

        when(greenItMapper.mapToView(any(GreenItAppDto.class))).thenReturn(view);

        // WHEN
        List<IndicateurApplicationGreenITView> result =
                greenItService.getIndicateursApplicationGreenIT(origine, passee);

        // THEN
        assertEquals(1, result.size());
        verify(oscarService).getApplications();
        verify(tableFaitsRepository, times(2)).getGreenItApp(any(Date.class));
        verify(greenItMapper).mapToView(any(GreenItAppDto.class));
    }

    @Test
    void getIndicateursApplicationGreenIT_invalidDates_shouldThrowException() {

        when(tableFaitsRepository.findLastDateIndicateur())
                .thenReturn(List.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)));
        // GIVEN
        Date origine =
                Date.from(
                        LocalDate.of(2024, 1, 2).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Date passee =
                Date.from(
                        LocalDate.of(2024, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        when(tableFaitsRepository.findLastDateIndicateur())
                .thenReturn(List.of()); // vide => invalid

        Application app = Application.builder().idApplication(1).build();
        lenient().when(oscarService.getApplications()).thenReturn(List.of(app));

        // WHEN / THEN
        assertThrows(
                IllegalArgumentException.class,
                () -> greenItService.getIndicateursApplicationGreenIT(origine, passee));
    }

    @Test
    void getIndicateursApplicationGreenIT_shouldUseAnonymousApplication_whenNull() {
        when(tableFaitsRepository.findLastDateIndicateur())
                .thenReturn(List.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)));
        // GIVEN
        Date origine =
                Date.from(
                        LocalDate.of(2024, 1, 2).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Date passee =
                Date.from(
                        LocalDate.of(2024, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        GreenItAppProjection projection = mock(GreenItAppProjection.class);
        GreenItAppProjection projectionHist = mock(GreenItAppProjection.class);

        when(projection.getRamAlloue()).thenReturn(BigDecimal.TEN);
        when(projectionHist.getRamAlloue()).thenReturn(BigDecimal.ONE);

        when(tableFaitsRepository.getGreenItApp(origine)).thenReturn(List.of(projection));

        when(tableFaitsRepository.getGreenItApp(passee)).thenReturn(List.of(projectionHist));

        when(oscarService.getApplications()).thenReturn(Collections.emptyList());

        when(greenItMapper.mapToView(any()))
                .thenReturn(
                        IndicateurApplicationGreenITView.builder()
                                .applicationName("anonyme")
                                .build());

        // WHEN
        List<IndicateurApplicationGreenITView> result =
                greenItService.getIndicateursApplicationGreenIT(origine, passee);

        // THEN
        assertEquals(1, result.size());
    }
}
