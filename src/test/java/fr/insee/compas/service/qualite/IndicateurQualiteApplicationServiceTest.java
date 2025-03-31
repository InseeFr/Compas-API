package fr.insee.compas.service.qualite;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.view.IndicateurApplicationQualiteView;

@SpringBootTest
@ActiveProfiles("test")
class IndicateurQualiteApplicationServiceTest {

    @MockitoBean OscarService oscarService;

    @Autowired IndicateurQualiteApplicationService indicateurService;

    @Test
    @Sql(
            scripts = {"classpath:qualite/data-qualite.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getIndicateurNiveauApplicationTestAvecAnalyseSonar() {
        Application app =
                Application.builder()
                        .idApplication(195)
                        .appName("application")
                        .sndi("SNDI")
                        .build();

        List<Application> mockApp = List.of(app);
        Mockito.when(oscarService.getApplications()).thenReturn(mockApp);

        var listeIndicateurModule = indicateurService.getIndicateurNiveauApplication();
        assertThat(listeIndicateurModule).hasSize(1);
        IndicateurApplicationQualiteView view = listeIndicateurModule.getFirst();
        assertThat(view.getLettreCouvertureTestUniaire()).isEqualTo("A");
        assertThat(view.getLettreFiabilite()).isEqualTo("A");
        assertThat(view.getLettreNiveauCve()).isEqualTo("A");
    }

    @Test
    @Sql(
            scripts = {"classpath:qualite/data-qualite-sans-sonar.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getIndicateurNiveauModuleTestAvecAnalyseSonarSansObjet() {
        Application app =
                Application.builder()
                        .idApplication(197)
                        .appName("application")
                        .sndi("SNDI")
                        .build();

        List<Application> mockApp = List.of(app);
        Mockito.when(oscarService.getApplications()).thenReturn(mockApp);

        var listeIndicateurModule = indicateurService.getIndicateurNiveauApplication();
        assertThat(listeIndicateurModule).hasSize(1);
        var view = listeIndicateurModule.getFirst();
        assertThat(view.getLettreCouvertureTestUniaire()).isEqualTo("SO");
        assertThat(view.getLettreNiveauCve()).isEqualTo("E");
    }
}
