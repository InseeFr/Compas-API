package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.VmOscarView;
import fr.insee.compas.exception.CompasClientException;
import fr.insee.compas.mapper.MetriqueVmMapper;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.greenit.MetriqueVm;
import fr.insee.compas.model.greenit.util.LectureCsvUtil;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.greenit.GreenItService;
import fr.insee.compas.util.ScoreUtils;

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
        final MetriqueVm metric1 =
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
        final MetriqueVm metric2 =
                MetriqueVm.builder()
                        .vm("pdsir4esli002")
                        .diskAllocated(LectureCsvUtil.process("154,83"))
                        .diskUsed(LectureCsvUtil.process("49,46"))
                        .ramAllocated(LectureCsvUtil.process("4 788,75"))
                        .ramMaxi(LectureCsvUtil.process("6 390,57"))
                        .cpuAllocated(LectureCsvUtil.process("4"))
                        .cpuMaxi(LectureCsvUtil.process("3,9"))
                        .build();

        metrics = metrics = Arrays.asList(metric1, metric2);

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
                        VmOscarView.builder().idApplication(124).build());
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

    @Test
    void testMiseAJourIndicateursModuleGreenIT_AvecListeVide_neFaitRien() {
        List<VmOscarView> vmOscars = List.of();

        greenItService.miseAJourIndicateursModuleGreenIT(vmOscars, LocalDate.now());
        verify(tableFaitsRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void testMiseAJourIndicateursModuleGreenIT_GroupementParModule() {
        // 2 modules différents : 10 et 20
        VmOscarView vm1 =
                VmOscarView.builder()
                        .idModule(10)
                        .idApplication(100)
                        .plateforme("pd")
                        .nom("vm1")
                        .build();

        VmOscarView vm2 =
                VmOscarView.builder()
                        .idModule(10)
                        .idApplication(100)
                        .plateforme("bt")
                        .nom("vm2")
                        .build();

        VmOscarView vm3 =
                VmOscarView.builder()
                        .idModule(20)
                        .idApplication(200)
                        .plateforme("pd")
                        .nom("vm3")
                        .build();

        List<VmOscarView> vmOscars = List.of(vm1, vm2, vm3);

        LocalDate fileDate = LocalDate.of(2025, 1, 1);

        try (MockedStatic<ScoreUtils> scoreUtilsMock = Mockito.mockStatic(ScoreUtils.class)) {

            scoreUtilsMock
                    .when(() -> ScoreUtils.isPlateformeProd(Mockito.anyString()))
                    .thenReturn(true);

            greenItService.miseAJourIndicateursModuleGreenIT(vmOscars, fileDate);
        }

        /* on 2 modules (10 et 20), on appelle 1 fois peuplerIndicateurs pour la prod et le global : donc 4 appels de la méthode
         * on multiplie par 8 indicateurs,  ça fait 32 appels de save */
        verify(tableFaitsRepository, times(32)).save(Mockito.any(TableFaits.class));
    }

    @Test
    void testMiseAJourIndicateursModuleGreenIT_IgnoreLesVmSansModule() {
        VmOscarView vmAvecModule =
                VmOscarView.builder()
                        .idModule(10)
                        .idApplication(100)
                        .plateforme("pd")
                        .nom("vmAvecModule")
                        .build();

        VmOscarView vmSansModule =
                VmOscarView.builder()
                        .idModule(null)
                        .idApplication(100)
                        .plateforme("pd")
                        .nom("vmSansModule")
                        .build();

        List<VmOscarView> vmOscars = List.of(vmAvecModule, vmSansModule);

        LocalDate fileDate = LocalDate.of(2025, 1, 1);

        try (MockedStatic<ScoreUtils> scoreUtilsMock = Mockito.mockStatic(ScoreUtils.class)) {

            scoreUtilsMock
                    .when(() -> ScoreUtils.isPlateformeProd(Mockito.anyString()))
                    .thenReturn(true);

            greenItService.miseAJourIndicateursModuleGreenIT(vmOscars, fileDate);
        }

        /*
         * Ici il n’y a qu’un module : 10 on appelle 2 fois peuplerIndicateurs
         * nb save() = 2 * 8 indicateurs = 16
         */
        verify(tableFaitsRepository, times(16)).save(Mockito.any(TableFaits.class));
    }
}
