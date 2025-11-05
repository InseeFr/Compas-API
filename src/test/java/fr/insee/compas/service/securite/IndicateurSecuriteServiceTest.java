package fr.insee.compas.service.securite;

import static java.lang.Math.log10;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.IndicateurSecuriteRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.view.IndicateurSecuriteApplicationView;
import fr.insee.compas.view.IndicateurSecuriteModuleView;

@ExtendWith(MockitoExtension.class)
class IndicateurSecuriteServiceTest {

    @Mock IndicateurSecuriteRepository indicateurSecuriteRepository;

    @Mock OscarService oscarService;

    @InjectMocks IndicateurSecuriteService service;

    @Test
    @DisplayName(
            "getIndicateursApplicationView: mappe les lignes et calcule la lettre (cas avec et sans"
                    + " données, app inconnue)")
    void getIndicateursApplicationView_ok() {
        // --- Données brutes renvoyées par le repo: [idApp, c, e, m, f]
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[] {42, null, null, null, null}); // pas de données CVE -> lettre null
        rows.add(new Object[] {7, 1, 2, 3, 4}); // données complètes -> calcule lettre
        rows.add(new Object[] {999, 0, 0, 0, 0}); // app inconnue, CVE=0 -> niveau 0 => lettre A
        given(indicateurSecuriteRepository.findValueBruteApplication()).willReturn(rows);

        // --- Applications depuis Oscar
        Application app42 = mock(Application.class);
        when(app42.getIdApplication()).thenReturn(42);
        when(app42.getAppName()).thenReturn("A42");
        Application app7 = mock(Application.class);
        when(app7.getIdApplication()).thenReturn(7);
        when(app7.getAppName()).thenReturn("A7");
        given(oscarService.getApplications()).willReturn(List.of(app42, app7));

        // --- Appel
        List<IndicateurSecuriteApplicationView> out = service.getIndicateursApplicationView();

        // --- Assert taille
        assertThat(out).hasSize(3);

        // Ligne 1 : 42, pas de données
        IndicateurSecuriteApplicationView v0 = out.get(0);
        assertThat(v0.getApplicationId()).isEqualTo(42);
        assertThat(v0.getApplicationName()).isEqualTo("A42");
        assertThat(v0.getNbCveCritical()).isNull();
        assertThat(v0.getNbCveHigh()).isNull();
        assertThat(v0.getNbCveMedium()).isNull();
        assertThat(v0.getNbCveLow()).isNull();
        assertThat(v0.getLettreSecurite()).isNull();

        // Ligne 2 : 7, données complètes -> lettre E (log10(1235) ~= 3.091)
        IndicateurSecuriteApplicationView v1 = out.get(1);
        assertThat(v1.getApplicationId()).isEqualTo(7);
        assertThat(v1.getApplicationName()).isEqualTo("A7");
        assertThat(v1.getNbCveCritical()).isEqualTo("1");
        assertThat(v1.getNbCveHigh()).isEqualTo("2");
        assertThat(v1.getNbCveMedium()).isEqualTo("3");
        assertThat(v1.getNbCveLow()).isEqualTo("4");
        assertThat(v1.getLettreSecurite()).isEqualTo("E");

        // Ligne 3 : 999, app inconnue, 0/0/0/0 -> niveau 0 => lettre A
        IndicateurSecuriteApplicationView v2 = out.get(2);
        assertThat(v2.getApplicationId()).isEqualTo(999);
        assertThat(v2.getApplicationName()).isNull();
        assertThat(v2.getNbCveCritical()).isEqualTo("0");
        assertThat(v2.getNbCveHigh()).isEqualTo("0");
        assertThat(v2.getNbCveMedium()).isEqualTo("0");
        assertThat(v2.getNbCveLow()).isEqualTo("0");
        assertThat(v2.getLettreSecurite()).isEqualTo("A");
    }

    @Test
    @DisplayName(
            "getIndicateursModuleView: mappe les lignes et calcule (tous présents -> lettre, sinon"
                    + " null)")
    void getIndicateursModuleView_ok() {
        // --- Données brutes renvoyées par le repo: [moduleId, c, e, m, f]
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[] {100, 1, 2, 3, 4}); // complet -> lettre E
        rows.add(new Object[] {200, 1, null, 3, 4}); // partiel -> lettre null
        rows.add(new Object[] {201, null, null, null, null}); // vide, module inconnu -> lettre null
        given(indicateurSecuriteRepository.findValueBruteModule()).willReturn(rows);

        // --- Modules depuis Oscar
        Module m100 = mock(Module.class);
        when(m100.getId()).thenReturn(100);
        when(m100.getIdApplication()).thenReturn(7);
        when(m100.getModName()).thenReturn("M100");
        when(m100.getAppName()).thenReturn("A7");

        Module m200 = mock(Module.class);
        when(m200.getId()).thenReturn(200);
        when(m200.getIdApplication()).thenReturn(8);
        when(m200.getModName()).thenReturn("M200");
        when(m200.getAppName()).thenReturn("A8");

        given(oscarService.getModules()).willReturn(List.of(m100, m200));

        // --- Appel
        List<IndicateurSecuriteModuleView> out = service.getIndicateursModuleView();

        assertThat(out).hasSize(3);

        // 100 -> tous présents -> lettre E
        IndicateurSecuriteModuleView m0 = out.get(0);
        assertThat(m0.getModuleId()).isEqualTo(100);
        assertThat(m0.getApplicationId()).isEqualTo(7);
        assertThat(m0.getModuleName()).isEqualTo("M100");
        assertThat(m0.getApplicationName()).isEqualTo("A7");
        assertThat(m0.getNbCveCritical()).isEqualTo("1");
        assertThat(m0.getNbCveHigh()).isEqualTo("2");
        assertThat(m0.getNbCveMedium()).isEqualTo("3");
        assertThat(m0.getNbCveLow()).isEqualTo("4");
        assertThat(m0.getLettreSecurite()).isEqualTo("E");

        // 200 -> partiel -> lettre null, mais mapping des champs OK
        IndicateurSecuriteModuleView m1 = out.get(1);
        assertThat(m1.getModuleId()).isEqualTo(200);
        assertThat(m1.getApplicationId()).isEqualTo(8);
        assertThat(m1.getModuleName()).isEqualTo("M200");
        assertThat(m1.getApplicationName()).isEqualTo("A8");
        assertThat(m1.getNbCveCritical()).isEqualTo("1");
        assertThat(m1.getNbCveHigh()).isNull();
        assertThat(m1.getNbCveMedium()).isEqualTo("3");
        assertThat(m1.getNbCveLow()).isEqualTo("4");
        assertThat(m1.getLettreSecurite()).isNull();

        // 201 -> module inconnu
        IndicateurSecuriteModuleView m2 = out.get(2);
        assertThat(m2.getModuleId()).isEqualTo(201);
        assertThat(m2.getApplicationId()).isNull();
        assertThat(m2.getModuleName()).isNull();
        assertThat(m2.getApplicationName()).isNull();
        assertThat(m2.getLettreSecurite()).isNull();
    }

    @Test
    @DisplayName("getCalculIndicateurCve: log10(c*1000 + e*100 + m*10 + f + 1)")
    void getCalculIndicateurCve_formula() {
        double expected = log10(1 * 1000 + 2 * 100 + 3 * 10 + 4 + 1); // log10(1235)
        assertThat(service.getCalculIndicateurCve(1, 2, 3, 4))
                .isCloseTo(expected, org.assertj.core.data.Offset.offset(1e-12));

        // Zéro partout -> log10(1) = 0
        assertThat(service.getCalculIndicateurCve(0, 0, 0, 0))
                .isCloseTo(0.0, org.assertj.core.data.Offset.offset(1e-12));
    }

    @Test
    @DisplayName("convertNiveauCveEnLettre: seuils A/B/C/D/E")
    void convertNiveauCveEnLettre_thresholds() {
        assertThat(service.convertNiveauCveEnLettre(-0.1)).isEqualTo("A");
        assertThat(service.convertNiveauCveEnLettre(0.0)).isEqualTo("A");
        assertThat(service.convertNiveauCveEnLettre(0.0001)).isEqualTo("B");
        assertThat(service.convertNiveauCveEnLettre(1.0)).isEqualTo("C");
        assertThat(service.convertNiveauCveEnLettre(2.0)).isEqualTo("D");
        assertThat(service.convertNiveauCveEnLettre(3.0)).isEqualTo("E");
    }
}
