package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.VmOscarView;
import fr.insee.compas.exception.CompasClientException;
import fr.insee.compas.mapper.MetriqueVmMapper;
import fr.insee.compas.model.greenit.MetriqueVm;
import fr.insee.compas.model.greenit.util.LectureCsvUtil;
import fr.insee.compas.repository.TableFaitsRepository;

@ExtendWith(MockitoExtension.class)
class GreenItServiceMajTest {

    private GreenItService greenItService;

    @Mock private FichierControlService fichierControlService;

    @Mock private OscarClient oscarClient;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @Mock private MetriqueVmMapper metriqueVmMapper;

    private List<MetriqueVm> metrics;

    @BeforeEach
    void setup() {
        final MetriqueVm metric =
                MetriqueVm.builder()
                        .vm("pdsir4esli001")
                        .diskAllocated(LectureCsvUtil.process("154,83"))
                        .diskUsed(LectureCsvUtil.process("49,46"))
                        .ramAllocated(LectureCsvUtil.process("4 788,75"))
                        .ramMaxi(LectureCsvUtil.process("6 390,57"))
                        .cpuAllocated(LectureCsvUtil.process("4"))
                        .cpuMaxi(LectureCsvUtil.process("3,9"))
                        .conso(LectureCsvUtil.process("1,13"))
                        .build();

        metrics = List.of(metric);

        greenItService =
                spy(
                        new GreenItService(
                                oscarClient, metriqueVmMapper, tableFaitsRepository, metrics));
    }

    @Test
    void testMiseAJourIndicateursGreenIT_OK() {
        final List<VmOscarView> mockVms =
                List.of(
                        VmOscarView.builder().idApplication(123).build(),
                        VmOscarView.builder().idApplication(123).build());
        Mockito.when(oscarClient.getAllVmOscar()).thenReturn(ResponseEntity.ok(mockVms));

        assertDoesNotThrow(() -> greenItService.miseAJourIndicateursGreenIT(LocalDate.now()));

        verify(greenItService, times(1))
                .miseAJourIndicateursApplicationGreenIT(mockVms, LocalDate.now());
        verify(greenItService, times(1))
                .miseAJourIndicateursModuleGreenIT(mockVms, LocalDate.now());
    }

    @Test
    void testMiseAJourIndicateursGreenIT_ErreurBodyNull() {
        // Mock retour OscarClient avec un body null
        Mockito.when(oscarClient.getAllVmOscar()).thenReturn(ResponseEntity.ok(null));
        final LocalDate localDate = LocalDate.now();
        final CompasClientException exception =
                Assertions.assertThrows(
                        CompasClientException.class,
                        () -> {
                            greenItService.miseAJourIndicateursGreenIT(localDate);
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
        final List<VmOscarView> emptyList = List.of();
        Mockito.when(oscarClient.getAllVmOscar()).thenReturn(ResponseEntity.ok(emptyList));
        assertDoesNotThrow(() -> greenItService.miseAJourIndicateursGreenIT(LocalDate.now()));

        verify(greenItService, times(1))
                .miseAJourIndicateursApplicationGreenIT(emptyList, LocalDate.now());
        verify(greenItService, times(1))
                .miseAJourIndicateursModuleGreenIT(emptyList, LocalDate.now());
    }
}
