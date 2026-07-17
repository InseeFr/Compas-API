package fr.insee.compas.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.dto.green.GreenItBaseDto;
import fr.insee.compas.dto.green.GreenKubeDto;
import fr.insee.compas.dto.green.GreenVmDto;
import fr.insee.compas.mapper.green.GreenItMapper;
import fr.insee.compas.model.greenit.GreenItScore;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.repository.projection.green.GreenItAppKubeProjection;
import fr.insee.compas.repository.projection.green.GreenItAppVmProjection;
import fr.insee.compas.service.greenit.score.GreenItComputeScore;
import fr.insee.compas.view.green.IndicateurAppGreenBaseView;
import fr.insee.compas.view.green.IndicateurAppGreenKubeView;
import fr.insee.compas.view.green.IndicateurAppGreenVmView;

@ExtendWith(MockitoExtension.class)
class GreenItMapperTest {

    @Mock private GreenItComputeScore greenItComputeScore;

    private GreenItMapper greenItMapper;

    private static final LocalDate DATE = LocalDate.of(2026, 7, 9);

    @BeforeEach
    void setUp() {
        greenItMapper = new GreenItMapper(greenItComputeScore);
    }

    // ---------------------------------------------------------------
    // mapToView - dispatch
    // ---------------------------------------------------------------

    @Nested
    class MapToView {

        @Test
        void should_throw_when_dto_type_is_unknown() {
            GreenItBaseDto unknownDto = new GreenItBaseDto() {};

            assertThatThrownBy(() -> greenItMapper.mapToView(unknownDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Type de DTO GreenIT inconnu");
        }

        @Test
        void should_build_vm_view_with_score_when_dto_is_vm() {
            GreenVmDto vmDto =
                    GreenVmDto.builder()
                            .applicationId(1)
                            .applicationName("app-vm")
                            .serviceDev("SNDI")
                            .domaineDev("DOM-DEV")
                            .domaineFonc("DOM-FONC")
                            .ramAllocated(BigDecimal.valueOf(1024L))
                            .cpuAllocated(BigDecimal.valueOf(3600L))
                            .dateMaj(DATE)
                            .build();

            GreenItScore score =
                    GreenItScore.builder()
                            .grade("A")
                            .impact(new BigDecimal("1.2345"))
                            .score(new BigDecimal("2.3456"))
                            .gaspillage(new BigDecimal("0.5678"))
                            .build();

            when(greenItComputeScore.computeAppScore(vmDto)).thenReturn(score);

            IndicateurAppGreenBaseView result = greenItMapper.mapToView(vmDto);

            assertThat(result).isInstanceOf(IndicateurAppGreenVmView.class);
            IndicateurAppGreenVmView vmView = (IndicateurAppGreenVmView) result;
            assertThat(vmView.getApplicationId()).isEqualTo(1);
            assertThat(vmView.getApplicationName()).isEqualTo("app-vm");
            assertThat(vmView.getLettreGreen()).isEqualTo("A");
            assertThat(vmView.getImpactScore()).isEqualTo("1.235"); // setScale(3, UP)
            assertThat(vmView.getConsoScore()).isEqualTo("2.346");
            assertThat(vmView.getGaspillageScore()).isEqualTo("0.568");
            assertThat(vmView.getDateMaj()).isEqualTo(DATE);
        }

        @Test
        void should_build_kube_view_without_score_when_dto_is_kube() {
            GreenKubeDto kubeDto =
                    GreenKubeDto.builder()
                            .applicationId(2)
                            .applicationName("app-kube")
                            .cpuUsed(BigDecimal.valueOf(3600L))
                            .ramUsed(2L * 1024 * 1024 * 1024)
                            .dateMaj(DATE)
                            .build();

            IndicateurAppGreenBaseView result = greenItMapper.mapToView(kubeDto);

            assertThat(result).isInstanceOf(IndicateurAppGreenKubeView.class);
            IndicateurAppGreenKubeView kubeView = (IndicateurAppGreenKubeView) result;
            assertThat(kubeView.getApplicationId()).isEqualTo(2);
            // Score en attente côté métier : doit rester null, ne rien calculer
            assertThat(kubeView.getLettreGreen()).isNull();
            assertThat(kubeView.getImpactScore()).isNull();
            assertThat(kubeView.getConsoScore()).isNull();
            assertThat(kubeView.getGaspillageScore()).isNull();

            // greenItComputeScore ne doit jamais être appelé pour le Kube
            org.mockito.Mockito.verifyNoInteractions(greenItComputeScore);
        }
    }

    // ---------------------------------------------------------------
    // buildIndicateurKubeDto - null safety
    // ---------------------------------------------------------------

    @Nested
    class BuildIndicateurKubeDto {

        @Mock private Application application;

        @Mock private GreenItAppKubeProjection projection;

        @Mock private GreenItAppKubeProjection projectionHist;

        @Test
        void should_map_all_fields_when_projections_are_present() {
            when(application.getAppName()).thenReturn("app-kube");
            when(application.getSndi()).thenReturn("SNDI");
            when(application.getDomaineSndi()).thenReturn("DOM-DEV");
            when(application.getDomaineFonctionnel()).thenReturn("DOM-FONC");

            when(projection.getCpuConsomme()).thenReturn(BigDecimal.valueOf(100L));
            when(projection.getCpuConsommeePd()).thenReturn(BigDecimal.valueOf(200L));
            when(projection.getRamConsommee()).thenReturn(300L);
            when(projection.getRamConsommeePd()).thenReturn(400L);
            when(projection.getS3Consomme()).thenReturn(500L);
            when(projection.getS3ConsommePd()).thenReturn(600L);
            when(projection.getPvcConsomme()).thenReturn(700L);
            when(projection.getPvcConsommePd()).thenReturn(800L);
            when(projection.getNbPodMaxi()).thenReturn(BigDecimal.valueOf(9L));
            when(projection.getNbPodMaxiPd()).thenReturn(BigDecimal.valueOf(10L));

            when(projectionHist.getCpuConsomme()).thenReturn(BigDecimal.valueOf(1100L));
            when(projectionHist.getCpuConsommeePd()).thenReturn(BigDecimal.valueOf(1200L));
            when(projectionHist.getRamConsommee()).thenReturn(1300L);
            when(projectionHist.getRamConsommeePd()).thenReturn(1400L);
            when(projectionHist.getS3Consomme()).thenReturn(1500L);
            when(projectionHist.getS3ConsommePd()).thenReturn(1600L);
            when(projectionHist.getPvcConsomme()).thenReturn(1700L);
            when(projectionHist.getPvcConsommePd()).thenReturn(1800L);
            when(projectionHist.getNbPodMaxi()).thenReturn(BigDecimal.valueOf(19L));
            when(projectionHist.getNbPodMaxiPd()).thenReturn(BigDecimal.valueOf(20L));

            GreenKubeDto dto =
                    greenItMapper.buildIndicateurKubeDto(
                            42, application, projection, projectionHist, DATE);

            assertThat(dto.getApplicationId()).isEqualTo(42);
            assertThat(dto.getApplicationName()).isEqualTo("app-kube");
            assertThat(dto.getCpuUsed()).isEqualTo(BigDecimal.valueOf(100));
            assertThat(dto.getCpuUsedHist()).isEqualTo(BigDecimal.valueOf(1100));
            assertThat(dto.getCpuUsedProd()).isEqualTo(BigDecimal.valueOf(200));
            assertThat(dto.getCpuUsedHistProd()).isEqualTo(BigDecimal.valueOf(1200));
            assertThat(dto.getRamUsed()).isEqualTo(300L);
            assertThat(dto.getRamUsedHist()).isEqualTo(1300L);
            assertThat(dto.getRamUsedProd()).isEqualTo(400L);
            assertThat(dto.getRamUsedHistProd()).isEqualTo(1400L);
            assertThat(dto.getS3Used()).isEqualTo(500L);
            assertThat(dto.getS3UsedHist()).isEqualTo(1500L);
            assertThat(dto.getPvcUsed()).isEqualTo(700L);
            assertThat(dto.getPvcUsedHist()).isEqualTo(1700L);
            assertThat(dto.getNbPodMaxi()).isEqualTo(BigDecimal.valueOf(9));
            assertThat(dto.getNbPodMaxiHist()).isEqualTo(BigDecimal.valueOf(19));
            assertThat(dto.getNbPodMaxiProd()).isEqualTo(BigDecimal.valueOf(10));
            assertThat(dto.getNbPodMaxiHistProd()).isEqualTo(BigDecimal.valueOf(20));
            assertThat(dto.getDateMaj()).isEqualTo(DATE);
        }

        @Test
        void should_not_throw_and_leave_hist_fields_null_when_projectionHist_is_null() {
            GreenKubeDto dto =
                    greenItMapper.buildIndicateurKubeDto(42, application, projection, null, DATE);

            assertThat(dto.getCpuUsedHist()).isNull();
            assertThat(dto.getCpuUsedHistProd()).isNull();
            assertThat(dto.getRamUsedHist()).isNull();
            assertThat(dto.getS3UsedHist()).isNull();
            assertThat(dto.getPvcUsedHist()).isNull();
            assertThat(dto.getNbPodMaxiHist()).isNull();
            assertThat(dto.getNbPodMaxiHistProd()).isNull();
        }

        @Test
        void should_not_throw_and_leave_current_fields_null_when_projection_is_null() {
            GreenKubeDto dto =
                    greenItMapper.buildIndicateurKubeDto(
                            42, application, null, projectionHist, DATE);

            assertThat(dto.getCpuUsed()).isNull();
            assertThat(dto.getRamUsed()).isNull();
            assertThat(dto.getS3Used()).isNull();
            assertThat(dto.getPvcUsed()).isNull();
            assertThat(dto.getNbPodMaxi()).isNull();
        }

        @Test
        void should_use_anonyme_when_application_is_null() {
            GreenKubeDto dto =
                    greenItMapper.buildIndicateurKubeDto(
                            42, null, projection, projectionHist, DATE);

            assertThat(dto.getApplicationName()).isEqualTo("anonyme");
            assertThat(dto.getServiceDev()).isNull();
            assertThat(dto.getDomaineDev()).isNull();
            assertThat(dto.getDomaineFonc()).isNull();
        }
    }

    // ---------------------------------------------------------------
    // buildIndicateurDtoVm - null safety
    // ---------------------------------------------------------------

    @Nested
    class BuildIndicateurDtoVm {

        @Mock private Application application;

        @Mock private GreenItAppVmProjection projection;

        @Mock private GreenItAppVmProjection projectionHist;

        @Test
        void should_map_all_fields_when_projections_are_present() {
            when(application.getAppName()).thenReturn("app-vm");

            when(projection.getRamAlloue()).thenReturn(BigDecimal.valueOf(100L));
            when(projection.getRamAlloueePd()).thenReturn(BigDecimal.valueOf(200L));
            when(projection.getRamMaxi()).thenReturn(BigDecimal.valueOf(300L));
            when(projection.getRamMaxiPd()).thenReturn(BigDecimal.valueOf(400L));

            when(projectionHist.getRamAlloue()).thenReturn(BigDecimal.valueOf(1100L));
            when(projectionHist.getRamAlloueePd()).thenReturn(BigDecimal.valueOf(1200L));
            when(projectionHist.getRamMaxi()).thenReturn(BigDecimal.valueOf(1300L));
            when(projectionHist.getRamMaxiPd()).thenReturn(BigDecimal.valueOf(1400L));

            GreenVmDto dto =
                    greenItMapper.buildIndicateurDtoVm(
                            7, application, projection, projectionHist, DATE);

            assertThat(dto.getRamAllocated()).isEqualTo(BigDecimal.valueOf(100));
            assertThat(dto.getRamAllocatedHist()).isEqualTo(BigDecimal.valueOf(1100));
            assertThat(dto.getRamAllocatedProd()).isEqualTo(BigDecimal.valueOf(200));
            assertThat(dto.getRamAllocatedHistProd()).isEqualTo(BigDecimal.valueOf(1200));
            assertThat(dto.getRamMaxi()).isEqualTo(BigDecimal.valueOf(300));
            assertThat(dto.getRamMaxiHist()).isEqualTo(BigDecimal.valueOf(1300));
            assertThat(dto.getRamMaxiProd()).isEqualTo(BigDecimal.valueOf(400));
            assertThat(dto.getRamMaxiHistProd()).isEqualTo(BigDecimal.valueOf(1400));
        }

        @Test
        void should_not_throw_when_projectionHist_is_null() {
            GreenVmDto dto =
                    greenItMapper.buildIndicateurDtoVm(7, application, projection, null, DATE);

            assertThat(dto.getRamAllocatedHist()).isNull();
            assertThat(dto.getAsAllocatedHist()).isNull();
            assertThat(dto.getConsoHist()).isNull();
            assertThat(dto.getCpuAllocatedHist()).isNull();
            assertThat(dto.getDiskAllocatedHist()).isNull();
            assertThat(dto.getNbVmHist()).isNull();
        }

        @Test
        void should_not_throw_when_projection_is_null() {
            GreenVmDto dto =
                    greenItMapper.buildIndicateurDtoVm(7, application, null, projectionHist, DATE);

            assertThat(dto.getRamAllocated()).isNull();
            assertThat(dto.getAsAllocated()).isNull();
            assertThat(dto.getConso()).isNull();
        }
    }
}
