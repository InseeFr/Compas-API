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

import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.view.IndicateurDevopsView;

@ExtendWith(MockitoExtension.class)
class IndicatorDevopsModuleServiceTest {

    @Mock private OscarService oscarService;

    @Mock private TableFaitsService tableFaitsService;

    @InjectMocks private IndicatorDevopsModuleService service;

    private Date dateReference;
    private Date datePassee;

    @BeforeEach
    void setUp() {
        dateReference = new Date();
        datePassee = new Date(dateReference.getTime() - 86400000);
    }

    @Test
    @DisplayName("Doit construire les indicateurs DevOps d'un module")
    void doitConstruireLesIndicateursDuModule() {

        Module module = new Module();
        module.setId(1);
        module.setModName("Module A");
        module.setIdApplication(100);
        module.setAppName("COMPAS");
        module.setSndi("SNDI");
        module.setDomaineSndi("Domaine");
        module.setDomaineFonctionnel("Fonctionnel");

        IndicateurDevopsView current =
                IndicateurDevopsView.builder()
                        .distanceCount("100")
                        .nbDeploymentCount("50")
                        .nbContributorCount("10")
                        .build();

        IndicateurDevopsView past =
                IndicateurDevopsView.builder()
                        .distanceCount("80")
                        .nbDeploymentCount("40")
                        .nbContributorCount("8")
                        .build();

        when(oscarService.getModules()).thenReturn(List.of(module));

        when(tableFaitsService.getIndicateurModuleDevops(dateReference))
                .thenReturn(Map.of(1, current));

        when(tableFaitsService.getIndicateurModuleDevops(datePassee)).thenReturn(Map.of(1, past));

        List<IndicateurDevopsView> result =
                service.getIndicateurNiveauModule(dateReference, datePassee, false);

        assertThat(result).hasSize(1);

        IndicateurDevopsView view = result.get(0);

        assertThat(view.getModuleId()).isEqualTo(1);
        assertThat(view.getModuleName()).isEqualTo("Module A");

        assertThat(view.getApplicationId()).isEqualTo(100);
        assertThat(view.getApplicationName()).isEqualTo("COMPAS");

        assertThat(view.getDistanceCount()).isEqualTo("100");
        assertThat(view.getPastDistanceCount()).isEqualTo("80");

        assertThat(view.getNbDeploymentCount()).isEqualTo("50");
        assertThat(view.getPastNbDeploymentCount()).isEqualTo("40");

        assertThat(view.getNbContributorCount()).isEqualTo("10");
        assertThat(view.getPastNbContributorCount()).isEqualTo("8");

        assertThat(view.getDiffDistanceCount()).isEqualTo(20);
        assertThat(view.getDiffNbDeploymentCount()).isEqualTo(10);
        assertThat(view.getDiffNbContributorCount()).isEqualTo(2);

        assertThat(view.getLettreGlobalDevops()).isNotNull();
    }

    @Test
    @DisplayName("Doit retourner des valeurs nulles lorsqu'aucun indicateur n'existe")
    void doitRetournerDesValeursNullesLorsquAucunIndicateurExiste() {

        Module module = new Module();
        module.setId(1);
        module.setModName("Module A");

        when(oscarService.getModules()).thenReturn(List.of(module));

        when(tableFaitsService.getIndicateurModuleDevops(dateReference)).thenReturn(Map.of());

        when(tableFaitsService.getIndicateurModuleDevops(datePassee)).thenReturn(Map.of());

        List<IndicateurDevopsView> result =
                service.getIndicateurNiveauModule(dateReference, datePassee, false);

        assertThat(result).hasSize(1);

        IndicateurDevopsView view = result.get(0);

        assertThat(view.getModuleId()).isEqualTo(1);

        assertThat(view.getDistanceCount()).isNull();
        assertThat(view.getNbDeploymentCount()).isNull();
        assertThat(view.getNbContributorCount()).isNull();

        assertThat(view.getDiffNbContributorCount()).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    @DisplayName("Doit calculer la lettre globale en mode synthétique")
    void doitCalculerLaLettreGlobaleEnModeSynthetique() {

        Module module = new Module();
        module.setId(1);
        module.setModName("Module A");

        IndicateurDevopsView current =
                IndicateurDevopsView.builder()
                        .distanceCount("100")
                        .nbDeploymentCount("50")
                        .nbContributorCount("1")
                        .build();

        when(oscarService.getModules()).thenReturn(List.of(module));

        when(tableFaitsService.getIndicateurModuleDevops(dateReference))
                .thenReturn(Map.of(1, current));

        when(tableFaitsService.getIndicateurModuleDevops(datePassee)).thenReturn(Map.of());

        List<IndicateurDevopsView> result =
                service.getIndicateurNiveauModule(dateReference, datePassee, true);

        assertThat(result)
                .singleElement()
                .satisfies(view -> assertThat(view.getLettreGlobalDevops()).isNotNull());
    }

    @Test
    @DisplayName("Doit retourner un résultat pour chaque module Oscar")
    void doitRetournerUnResultatPourChaqueModuleOscar() {

        Module module1 = new Module();
        module1.setId(1);

        Module module2 = new Module();
        module2.setId(2);

        when(oscarService.getModules()).thenReturn(List.of(module1, module2));

        when(tableFaitsService.getIndicateurModuleDevops(dateReference)).thenReturn(Map.of());

        when(tableFaitsService.getIndicateurModuleDevops(datePassee)).thenReturn(Map.of());

        List<IndicateurDevopsView> result =
                service.getIndicateurNiveauModule(dateReference, datePassee, false);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Doit gérer le cas où seul l'indicateur courant existe")
    void doitGererLeCasOuSeulLIndicateurCourantExiste() {

        Module module = new Module();
        module.setId(1);

        IndicateurDevopsView current =
                IndicateurDevopsView.builder().nbContributorCount("12").build();

        when(oscarService.getModules()).thenReturn(List.of(module));

        when(tableFaitsService.getIndicateurModuleDevops(dateReference))
                .thenReturn(Map.of(1, current));

        when(tableFaitsService.getIndicateurModuleDevops(datePassee)).thenReturn(Map.of());

        List<IndicateurDevopsView> result =
                service.getIndicateurNiveauModule(dateReference, datePassee, false);

        assertThat(result.get(0).getDiffNbContributorCount()).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    @DisplayName("Doit gérer le cas où seul l'indicateur passé existe")
    void doitGererLeCasOuSeulLIndicateurPasseExiste() {

        Module module = new Module();
        module.setId(1);

        IndicateurDevopsView past = IndicateurDevopsView.builder().nbContributorCount("8").build();

        when(oscarService.getModules()).thenReturn(List.of(module));

        when(tableFaitsService.getIndicateurModuleDevops(dateReference)).thenReturn(Map.of());

        when(tableFaitsService.getIndicateurModuleDevops(datePassee)).thenReturn(Map.of(1, past));

        List<IndicateurDevopsView> result =
                service.getIndicateurNiveauModule(dateReference, datePassee, false);

        assertThat(result.get(0).getDiffNbContributorCount()).isEqualTo(Integer.MIN_VALUE);
    }
}
