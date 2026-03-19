package fr.insee.compas.logic.update.greenit.kube;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.KubeOscarView;
import fr.insee.compas.exception.CompasClientException;
import fr.insee.compas.mapper.MetriqueKubeMapper;
import fr.insee.compas.model.greenit.MetriqueKube;
import fr.insee.compas.model.greenit.MetriqueKubeCsvRead;
import fr.insee.compas.model.greenit.util.LectureCsvUtil;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.FichierControlService;

@ExtendWith(MockitoExtension.class)
class KubeMetricsCsvUpdaterTest {

    private KubeMetricsCsvUpdater kubeMetricsCsvUpdater;

    @Mock private FichierControlService fichierControlService;

    @Mock private OscarClient oscarClient;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @Mock private MetriqueKubeMapper metriqueKubeMapper;

    private List<MetriqueKube> metrics;

    @BeforeEach
    void setup() {
        final MetriqueKube metric1 =
                MetriqueKube.builder()
                        .namespace("projet-sirene")
                        .cpuUsed(LectureCsvUtil.process("154,83"))
                        .ramUsed(LectureCsvUtil.process("49,46"))
                        .s3Used(LectureCsvUtil.process("4 788,75"))
                        .pvcUsed(LectureCsvUtil.process("6 390,57"))
                        .build();
        metrics = List.of(metric1);
        kubeMetricsCsvUpdater =
                spy(
                        new KubeMetricsCsvUpdater(
                                oscarClient, tableFaitsRepository, metriqueKubeMapper, metrics));
    }

    @Test
    void testMiseAJourIndicateursGreenIT_OK() {
        final List<KubeOscarView> mockKubes =
                List.of(
                        KubeOscarView.builder().idApplication(123).build(),
                        KubeOscarView.builder().idApplication(124).build());
        Mockito.when(oscarClient.getAllNamespacesOscar()).thenReturn(ResponseEntity.ok(mockKubes));

        assertDoesNotThrow(
                () -> kubeMetricsCsvUpdater.miseAJourIndicateursGreenIT(LocalDate.now()));

        verify(kubeMetricsCsvUpdater, times(1))
                .miseAJourIndicateursApplicationKubeGreenIT(mockKubes, LocalDate.now());
    }

    @Test
    void testMiseAJourIndicateursGreenIT_ErreurBodyNull() {
        // Mock retour OscarClient avec un body null
        Mockito.when(oscarClient.getAllNamespacesOscar()).thenReturn(ResponseEntity.ok(null));
        final LocalDate localDate = LocalDate.now();
        final CompasClientException exception =
                Assertions.assertThrows(
                        CompasClientException.class,
                        () -> {
                            kubeMetricsCsvUpdater.miseAJourIndicateursGreenIT(localDate);
                        });
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(exception.getStatus()).isEqualTo(500);
        softAssertions
                .assertThat(exception.getErrorVM().getMessage())
                .isEqualTo("Erreur retour body Oscar");
        softAssertions.assertAll();
    }

    @Test
    void testMiseAJourIndicateursGreenIT_AvecListeVide() {
        final List<KubeOscarView> emptyList = List.of();
        Mockito.when(oscarClient.getAllNamespacesOscar()).thenReturn(ResponseEntity.ok(emptyList));
        assertDoesNotThrow(
                () -> kubeMetricsCsvUpdater.miseAJourIndicateursGreenIT(LocalDate.now()));

        verify(kubeMetricsCsvUpdater, times(1))
                .miseAJourIndicateursApplicationKubeGreenIT(emptyList, LocalDate.now());
        verify(kubeMetricsCsvUpdater, times(1))
                .miseAJourIndicateursApplicationKubeGreenIT(emptyList, LocalDate.now());
    }

    @Test
    @DisplayName(
            "Mappe les Optionals (present/empty), met à jour metrics et appelle"
                    + " miseAJourIndicateursGreenIT")
    void testMajIndicateursGreenItfromfile() throws Exception {
        // given
        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "metrics.csv",
                        "text/csv",
                        "dummy".getBytes(StandardCharsets.UTF_8));

        // 3 DTO "csv"
        MetriqueKubeCsvRead dto1 = mock(MetriqueKubeCsvRead.class);
        MetriqueKubeCsvRead dto2 = mock(MetriqueKubeCsvRead.class);
        MetriqueKubeCsvRead dto3 = mock(MetriqueKubeCsvRead.class);

        // 2 métriques mappées (dto2 -> Optional.empty pour tester flatMap Optional::stream)
        MetriqueKube k1 = mock(MetriqueKube.class);
        MetriqueKube k3 = mock(MetriqueKube.class);

        // Spy: on remplace loadCSVData(file)
        doReturn(List.of(dto1, dto2, dto3)).when(kubeMetricsCsvUpdater).loadKubeCSVData(file);

        // Mapper: present / empty / present
        when(metriqueKubeMapper.toMetriqueKube(dto1)).thenReturn(Optional.of(k1));
        when(metriqueKubeMapper.toMetriqueKube(dto2)).thenReturn(Optional.empty());
        when(metriqueKubeMapper.toMetriqueKube(dto3)).thenReturn(Optional.of(k3));

        LocalDate date = LocalDate.of(2025, 9, 1);

        // On ne veut pas exécuter la vraie logique interne ici : on vérifie juste l'appel
        doNothing().when(kubeMetricsCsvUpdater).miseAJourIndicateursGreenIT(date);

        // when
        kubeMetricsCsvUpdater.miseAJourIndicateursGreenItFromFile(file, date);

        // then — interactions
        verify(kubeMetricsCsvUpdater).loadKubeCSVData(file);
        verify(metriqueKubeMapper).toMetriqueKube(dto1);
        verify(metriqueKubeMapper).toMetriqueKube(dto2);
        verify(metriqueKubeMapper).toMetriqueKube(dto3);
        verify(kubeMetricsCsvUpdater).miseAJourIndicateursGreenIT(date);
    }
}
