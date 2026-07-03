package fr.insee.compas.logic.update.greenit.vm;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import fr.insee.compas.client.HyperXClient;
import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.ApplicationOscarView;
import fr.insee.compas.client.view.ApplishareHyperXView;
import fr.insee.compas.exception.CompasClientException;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.TableFaitsRepository;

@ExtendWith(MockitoExtension.class)
class ApplishareMetricsApiUpdaterTest {
    @Mock private OscarClient oscarClient;
    @Mock private HyperXClient hyperXClient;

    @Mock private TableFaitsRepository tableFaitsRepository;
    @InjectMocks private ApplishareMetricsApiUpdater applishareMetricsApiUpdater;

    @Captor private ArgumentCaptor<List<TableFaits>> tablesFaitsCaptor;

    @Test
    void miseAJourIndicateursApplishare_shouldThrowExceptionWhenOscarBodyIsNull() {
        // Given
        when(oscarClient.getAllApplicationOscar()).thenReturn(ResponseEntity.ok(null));

        ApplishareHyperXView hyperXView = new ApplishareHyperXView();
        hyperXView.setApplication("APP1");

        // When / Then
        assertThatThrownBy(
                        () ->
                                applishareMetricsApiUpdater.miseAJourIndicateursApplishare(
                                        List.of(hyperXView)))
                .isInstanceOf(CompasClientException.class);

        verify(oscarClient).getAllApplicationOscar();
        verifyNoInteractions(tableFaitsRepository);
    }

    @Test
    void testMiseAJourIndicateursApplishareGreenIT_OK() {
        final List<ApplishareHyperXView> mockHyperXs =
                List.of(
                        ApplishareHyperXView.builder()
                                .application("sirene4")
                                .tailleApplishareGo(BigDecimal.valueOf(520))
                                .tailleApplishareTotGo(BigDecimal.valueOf(820))
                                .build(),
                        ApplishareHyperXView.builder()
                                .application("esane2")
                                .tailleApplishareGo(BigDecimal.valueOf(320))
                                .tailleApplishareTotGo(BigDecimal.valueOf(800))
                                .build());

        Mockito.when(hyperXClient.getApplishareHyperX()).thenReturn(ResponseEntity.ok(mockHyperXs));
        Mockito.when(oscarClient.getAllApplicationOscar()).thenReturn(mockApplisOscar());
        applishareMetricsApiUpdater.miseAJourIndicateursGreenItFromApi();
        verify(hyperXClient, times(1)).getApplishareHyperX();
        verify(oscarClient, times(1)).getAllApplicationOscar();

        verify(tableFaitsRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testMiseAJourIndicateursApplishareGreenIT_ErreurBodyNull() {
        // Mock retour OscarClient avec un body null
        Mockito.when(hyperXClient.getApplishareHyperX()).thenReturn(ResponseEntity.ok(null));
        final CompasClientException exception =
                Assertions.assertThrows(
                        CompasClientException.class,
                        () -> {
                            applishareMetricsApiUpdater.miseAJourIndicateursGreenItFromApi();
                        });
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(exception.getStatus()).isEqualTo(500);
        softAssertions
                .assertThat(exception.getErrorVM().getMessage())
                .isEqualTo("Erreur retour body Hyperx");
        softAssertions.assertAll();
    }

    @Test
    void testMiseAJourIndicateursGreenIT_AvecListeVide() {
        final List<ApplicationOscarView> emptyApplisOscarList = List.of();
        final List<ApplishareHyperXView> emptyApplisHyperxList = List.of();
        Mockito.when(hyperXClient.getApplishareHyperX())
                .thenReturn(ResponseEntity.ok(emptyApplisHyperxList));
        assertDoesNotThrow(() -> applishareMetricsApiUpdater.miseAJourIndicateursGreenItFromApi());
        verify(hyperXClient, times(1)).getApplishareHyperX();
        // on s'arrête à HyperX, sans exception
        verifyNoInteractions(oscarClient);
        verifyNoInteractions(tableFaitsRepository);
    }

    private ResponseEntity<List<ApplicationOscarView>> mockApplisOscar() {
        final ApplicationOscarView applicationOscarView = new ApplicationOscarView();
        applicationOscarView.setId(123);
        applicationOscarView.setNom("sirene4");
        applicationOscarView.setNomTechnique("Sirene 4");
        applicationOscarView.setDescription("le répertoire des entreprises et des établissements");
        final ApplicationOscarView applicationOscarView2 = new ApplicationOscarView();
        applicationOscarView2.setId(12);
        applicationOscarView2.setNom("esane2");
        return new ResponseEntity<List<ApplicationOscarView>>(
                List.of(applicationOscarView, applicationOscarView2), HttpStatus.ACCEPTED);
    }

    @Test
    void
            miseAJourIndicateursApplishare_shouldMatchHyperXApplicationUsingOscarApplishareAlternativeName() {
        // Given
        List<ApplishareHyperXView> hyperXViews =
                List.of(
                        ApplishareHyperXView.builder()
                                .application("APP_HYPERX")
                                .plateforme("PD")
                                .tailleApplishareGo(new BigDecimal("10"))
                                .tailleApplishareTotGo(new BigDecimal("100"))
                                .build());
        ApplicationOscarView.ApplicationNomAlternatif nomAlternatifApplishare =
                ApplicationOscarView.ApplicationNomAlternatif.builder()
                        .source("applishare")
                        .nomAlternatif("APP_HYPERX")
                        .build();

        ApplicationOscarView applicationOscar =
                ApplicationOscarView.builder()
                        .id(123)
                        .nom("NOM_OSCAR_DIFFERENT")
                        .applicationNomAlternatifs(List.of(nomAlternatifApplishare))
                        .build();

        when(oscarClient.getAllApplicationOscar())
                .thenReturn(ResponseEntity.ok(List.of(applicationOscar)));

        // When
        applishareMetricsApiUpdater.miseAJourIndicateursApplishare(hyperXViews);

        // Then
        verify(tableFaitsRepository).saveAll(tablesFaitsCaptor.capture());

        List<TableFaits> savedTablesFaits = tablesFaitsCaptor.getValue();

        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(savedTablesFaits).hasSize(4);
        softAssertions
                .assertThat(savedTablesFaits)
                .extracting(TableFaits::getIdApplication)
                .containsOnly(123);
    }
}
