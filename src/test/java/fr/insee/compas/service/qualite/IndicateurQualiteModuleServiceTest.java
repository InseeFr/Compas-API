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
import fr.insee.compas.view.IndicateurQualiteView;

@SpringBootTest
@ActiveProfiles("test")
class IndicateurQualiteModuleServiceTest {

    @MockitoBean private OscarService oscarService;

    @Autowired private IndicateurQualiteModuleService indicateurService;

    @Test
    @Sql(
            scripts = {"classpath:qualite/data-qualite.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getIndicateurNiveauModuleTestAvecAnalyseSonarAvecCveA() {
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
        IndicateurQualiteView view = listeIndicateurModule.getFirst();
        assertThat(view.getLettreCouvertureTestUniaire()).isEqualTo("A");
        assertThat(view.getLettreFiabilite()).isEqualTo("A");
        assertThat(view.getLettreDetteTechnique()).isEqualTo("A");
    }

    @Test
    @Sql(
            scripts = {"classpath:qualite/data-qualite.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getIndicateurNiveauModuleTestAvecAnalyseSonarAvecCveE() {
        Module module =
                Module.builder()
                        .id(484)
                        .modName("module")
                        .appName("application")
                        .sndi("SNDI")
                        .keySonar("keySonar")
                        .build();

        List<Module> mockModules = List.of(module);
        Mockito.when(oscarService.getModules()).thenReturn(mockModules);

        var listeIndicateurModule = indicateurService.getIndicateurNiveauModule();
        assertThat(listeIndicateurModule).hasSize(1);
        IndicateurQualiteView view = listeIndicateurModule.getFirst();
        assertThat(view.getLettreCouvertureTestUniaire()).isEqualTo("A");
        assertThat(view.getLettreFiabilite()).isEqualTo("A");
        assertThat(view.getLettreDetteTechnique()).isEqualTo("A");
    }

    @Test
    @Sql(
            scripts = {"classpath:qualite/data-qualite.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getIndicateurNiveauModuleTestSansAnalyseSonarNiTrivy() {
        Module module =
                Module.builder()
                        .id(485)
                        .modName("module")
                        .appName("application")
                        .sndi("SNDI")
                        .keySonar("")
                        .build();

        List<Module> mockModules = List.of(module);
        Mockito.when(oscarService.getModules()).thenReturn(mockModules);

        List<IndicateurQualiteView> listeIndicateurModule =
                indicateurService.getIndicateurNiveauModule();
        assertThat(listeIndicateurModule).hasSize(1);
        IndicateurQualiteView view = listeIndicateurModule.getFirst();
        assertThat(view.getLettreCouvertureTestUniaire()).isEqualTo("NR");
        assertThat(view.getLettreFiabilite()).isEqualTo("NR");
    }

    @Test
    @Sql(
            scripts = {"classpath:qualite/data-qualite.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getIndicateurNiveauModuleTestAvecAnalyseSonarSansObjet() {
        Module module =
                Module.builder()
                        .id(488)
                        .modName("module")
                        .appName("application")
                        .sndi("SNDI")
                        .keySonar("Sans objet")
                        .build();

        List<Module> mockModules = List.of(module);
        Mockito.when(oscarService.getModules()).thenReturn(mockModules);

        var listeIndicateurModule = indicateurService.getIndicateurNiveauModule();
        assertThat(listeIndicateurModule).hasSize(1);
        IndicateurQualiteView view = listeIndicateurModule.getFirst();
        assertThat(view.getLettreCouvertureTestUniaire()).isEqualTo("SO");
        assertThat(view.getLettreDetteTechnique()).isEqualTo("SO");
        assertThat(view.getLettreFiabilite()).isEqualTo("SO");
    }

    @Test
    @Sql(
            scripts = {"classpath:qualite/data-qualite-sonar-so.sql"},
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
        IndicateurQualiteView view = listeIndicateurModule.getFirst();
        assertThat(view.getLettreCouvertureTestUniaire()).isEqualTo("NR");
        assertThat(view.getLettreFiabilite()).isEqualTo("NR");
        assertThat(view.getLettreDetteTechnique()).isEqualTo("NR");
    }
}
