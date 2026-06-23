package fr.insee.compas.service.devops;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.view.IndicateurDevopsView;

@ExtendWith(MockitoExtension.class)
class IndicatorDevopsApplicationServiceTest {

    @Mock private OscarService oscarService;

    @Mock private TableFaitsService tableFaitsService;

    @InjectMocks private IndicatorDevopsApplicationService service;

    private Date currentDate;
    private Date pastDate;

    @BeforeEach
    void setUp() {
        currentDate = new Date();
        pastDate = new Date(currentDate.getTime() - 86400000);
    }

    @Test
    @DisplayName("Should build application indicators")
    void shouldBuildApplicationIndicators() {

        Application app = new Application();
        app.setIdApplication(1);
        app.setAppName("COMPAS");
        app.setSndi("SNDI");
        app.setDomaineSndi("Domaine");
        app.setDomaineFonctionnel("Fonctionnel");

        IndicateurDevopsView current =
                IndicateurDevopsView.builder()
                        .distanceCount("100")
                        .nbDeploymentCount("50")
                        .nbContributorCount("10")
                        .build();

        IndicateurDevopsView past =
                IndicateurDevopsView.builder()
                        .distanceCount("90")
                        .nbDeploymentCount("40")
                        .nbContributorCount("8")
                        .build();

        when(oscarService.getApplications()).thenReturn(List.of(app));

        when(tableFaitsService.getIndicateurApplicationDevops(currentDate))
                .thenReturn(Map.of(1, current));

        when(tableFaitsService.getIndicateurApplicationDevops(pastDate))
                .thenReturn(Map.of(1, past));

        List<IndicateurDevopsView> result =
                service.getIndicateurNiveauApplication(currentDate, pastDate, false);

        assertThat(result).hasSize(1);

        IndicateurDevopsView view = result.get(0);

        assertThat(view.getApplicationId()).isEqualTo(1);
        assertThat(view.getApplicationName()).isEqualTo("COMPAS");

        assertThat(view.getDistanceCount()).isEqualTo("100");
        assertThat(view.getPastDistanceCount()).isEqualTo("90");

        assertThat(view.getNbDeploymentCount()).isEqualTo("50");
        assertThat(view.getPastNbDeploymentCount()).isEqualTo("40");

        assertThat(view.getNbContributorCount()).isEqualTo("10");
        assertThat(view.getPastNbContributorCount()).isEqualTo("8");

        assertThat(view.getDiffDistanceCount()).isEqualTo(10);
        assertThat(view.getDiffNbDeploymentCount()).isEqualTo(10);
        assertThat(view.getDiffNbContributorCount()).isEqualTo(2);

        assertThat(view.getLettreGlobalDevops()).isNotNull();
    }

    @Test
    @DisplayName("Should return empty indicators when application has no metrics")
    void shouldHandleMissingIndicators() {

        Application app = new Application();
        app.setIdApplication(1);
        app.setAppName("COMPAS");

        when(oscarService.getApplications()).thenReturn(List.of(app));

        when(tableFaitsService.getIndicateurApplicationDevops(currentDate)).thenReturn(Map.of());

        when(tableFaitsService.getIndicateurApplicationDevops(pastDate)).thenReturn(Map.of());

        List<IndicateurDevopsView> result =
                service.getIndicateurNiveauApplication(currentDate, pastDate, false);

        assertThat(result).hasSize(1);

        IndicateurDevopsView view = result.get(0);

        assertThat(view.getApplicationId()).isEqualTo(1);

        assertThat(view.getDistanceCount()).isNull();
        assertThat(view.getNbDeploymentCount()).isNull();
        assertThat(view.getNbContributorCount()).isNull();

        assertThat(view.getDiffNbContributorCount()).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    @DisplayName("Should calculate global letter in synthetic mode")
    void shouldCalculateGlobalLetterInSyntheticMode() {

        Application app = new Application();
        app.setIdApplication(1);

        IndicateurDevopsView current =
                IndicateurDevopsView.builder()
                        .distanceCount("100")
                        .nbDeploymentCount("50")
                        .nbContributorCount("1")
                        .build();

        when(oscarService.getApplications()).thenReturn(List.of(app));

        when(tableFaitsService.getIndicateurApplicationDevops(currentDate))
                .thenReturn(Map.of(1, current));

        when(tableFaitsService.getIndicateurApplicationDevops(pastDate)).thenReturn(Map.of());

        List<IndicateurDevopsView> result =
                service.getIndicateurNiveauApplication(currentDate, pastDate, true);

        assertThat(result)
                .singleElement()
                .satisfies(view -> assertThat(view.getLettreGlobalDevops()).isNotNull());
    }

    @Test
    @DisplayName("Should build one result per application")
    void shouldReturnAllApplications() {

        Application app1 = new Application();
        app1.setIdApplication(1);

        Application app2 = new Application();
        app2.setIdApplication(2);

        when(oscarService.getApplications()).thenReturn(List.of(app1, app2));

        when(tableFaitsService.getIndicateurApplicationDevops(currentDate)).thenReturn(Map.of());

        when(tableFaitsService.getIndicateurApplicationDevops(pastDate)).thenReturn(Map.of());

        List<IndicateurDevopsView> result =
                service.getIndicateurNiveauApplication(currentDate, pastDate, false);

        assertThat(result).hasSize(2);
    }
}
