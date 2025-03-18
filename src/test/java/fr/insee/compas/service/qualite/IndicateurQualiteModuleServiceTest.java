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

import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.view.IndicateurModuleQualiteView;

@SpringBootTest
@ActiveProfiles("test")
class IndicateurQualiteModuleServiceTest {

    @MockitoBean OscarService oscarService;

    @Autowired IndicateurQualiteModuleService indicateurService;

    @Test
    @Sql(
            scripts = {"classpath:qualite/data-qualite.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getIndicateurNiveauModuleTestAvecAnalyseSonar() {
        Module module =
                Module.builder()
                        .id(483)
                        .modName("module")
                        .appName("application")
                        .sndi("SNDI")
                        .keySonar("keySonar")
                        .build();

        List<Module> mockModules = List.of(module);
        Mockito.when(oscarService.getModules()).thenReturn(mockModules);

        var listeIndicateurModule = indicateurService.getIndicateurNiveauModule();
        assertThat(listeIndicateurModule).hasSize(1);
        IndicateurModuleQualiteView view = listeIndicateurModule.getFirst();
        assertThat(view.getLettreCouvertureTestUniaire()).isEqualTo("A");
        assertThat(view.getLettreFiabilite()).isEqualTo("A");
        assertThat(view.getLettreNiveauCve()).isEqualTo("A");
        assertThat(view.getLettreDetteTechnique()).isEqualTo("A");
    }

    @Test
    @Sql(
            scripts = {"classpath:qualite/data-qualite.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getIndicateurNiveauModuleTestSansAnalyseSonarNiTrivy() {
        Module module =
                Module.builder()
                        .id(484)
                        .modName("module")
                        .appName("application")
                        .sndi("SNDI")
                        .keySonar("")
                        .build();

        List<Module> mockModules = List.of(module);
        Mockito.when(oscarService.getModules()).thenReturn(mockModules);

        List<IndicateurModuleQualiteView> listeIndicateurModule =
                indicateurService.getIndicateurNiveauModule();
        assertThat(listeIndicateurModule).hasSize(1);
        IndicateurModuleQualiteView view = listeIndicateurModule.getFirst();
        assertThat(view.getLettreCouvertureTestUniaire()).isEqualTo("NR");
        assertThat(view.getLettreFiabilite()).isEqualTo("NR");
        assertThat(view.getLettreNiveauCve()).isEqualTo(null);
    }

    @Test
    @Sql(
            scripts = {"classpath:qualite/data-qualite.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getIndicateurNiveauModuleTestAvecAnalyseSonarSansObjet() {
        Module module =
                Module.builder()
                        .id(484)
                        .modName("ddl - default")
                        .appName("ddl")
                        .sndi("SNDI Paris")
                        .keySonar("Sans objet")
                        .build();

        List<Module> mockModules = List.of(module);
        Mockito.when(oscarService.getModules()).thenReturn(mockModules);

        var listeIndicateurModule = indicateurService.getIndicateurNiveauModule();
        assertThat(listeIndicateurModule).hasSize(1);
        IndicateurModuleQualiteView view = listeIndicateurModule.getFirst();
        assertThat(view.getLettreCouvertureTestUniaire()).isEqualTo("SO");
        assertThat(view.getLettreDetteTechnique()).isEqualTo("SO");
        assertThat(view.getLettreFiabilite()).isEqualTo("SO");
    }

    @Test
    @Sql(
            scripts = {"classpath:qualite/data-qualite-sans-sonar.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getIndicateurNiveauModuleTestSansAnalyseSonar() {
        Module module =
                Module.builder()
                        .id(488)
                        .modName("module")
                        .appName("application")
                        .sndi("SNDI")
                        .keySonar("")
                        .build();

        List<Module> mockModules = List.of(module);
        Mockito.when(oscarService.getModules()).thenReturn(mockModules);

        var listeIndicateurModule = indicateurService.getIndicateurNiveauModule();
        assertThat(listeIndicateurModule).hasSize(1);
        IndicateurModuleQualiteView view = listeIndicateurModule.getFirst();
        assertThat(view.getLettreCouvertureTestUniaire()).isEqualTo("NR");
        assertThat(view.getLettreFiabilite()).isEqualTo("NR");
        assertThat(view.getLettreDetteTechnique()).isEqualTo("NR");
    }
}
