package fr.insee.compas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.insee.compas.model.sonar.Component;
import fr.insee.compas.model.sonar.Measure;
import fr.insee.compas.model.sonar.RecuperationMeasures;

class UtilsServiceTest {

    private final UtilsService utilsService = new UtilsService();

    @Test
    void testCalculPourcentageCouvertureTest() {
        final double result = utilsService.calculPourcentageCouvertureTest(100, 20);
        assertEquals(80.0, result, 0.01, "Le pourcentage attendu est de 80%");
        final double result2 = utilsService.calculPourcentageCouvertureTest(100, 100);
        assertEquals(0.0, result2, 0.01, "Le pourcentage attendu est de 0%");
        final double result3 = utilsService.calculPourcentageCouvertureTest(0, 0);
        assertEquals(0.0, result3, 0.01, "Le pourcentage attendu est de 0%");
        final double result4 = utilsService.calculPourcentageCouvertureTest(50, 60);
        assertEquals(-20.0, result4, 0.01, "Le pourcentage attendu est de -20%");
        final double result5 = utilsService.calculPourcentageCouvertureTest(141108, 139768);
        assertEquals(1.0, result5, 0.01, "Le pourcentage attendu est de 1%");
    }

    @Test
    void testGetCalcul() {
        // Calcul attendu : (2 * 1000) + (3 * 100) + (4 * 10) + (5 * 1) + 1 = 2346
        final BigDecimal expected = BigDecimal.valueOf(Math.log10(2346));
        final BigDecimal actual =
                utilsService.getCalculIndicateurCve(
                        new BigDecimal("2"),
                        new BigDecimal("3"),
                        new BigDecimal("4"),
                        new BigDecimal("5"));

        // Vérification avec une tolérance pour éviter les erreurs de précision
        assertEquals(expected, actual);
    }

    @Test
    void testConcatenationMeasures() {
        // Création du premier ensemble de mesures
        Measure m1 = new Measure();
        m1.setMetric("lines_to_cover");
        m1.setValue("100");

        Measure m2 = new Measure();
        m2.setMetric("uncovered_lines");
        m2.setValue("20");

        Measure m3 = new Measure();
        m3.setMetric("sqale_index");
        m3.setValue("300");

        Measure m4 = new Measure();
        m4.setMetric("reliability_rating");
        m4.setValue("A");

        Component c1 = new Component();
        c1.setMeasures(Arrays.asList(m1, m2, m3, m4));
        RecuperationMeasures measures1 = new RecuperationMeasures();
        measures1.setComponent(c1);

        // Création du deuxième ensemble de mesures
        Measure m5 = new Measure();
        m5.setMetric("lines_to_cover");
        m5.setValue("150");

        Measure m6 = new Measure();
        m6.setMetric("uncovered_lines");
        m6.setValue("30");

        Measure m7 = new Measure();
        m7.setMetric("sqale_index");
        m7.setValue("200");

        Measure m8 = new Measure();
        m8.setMetric("reliability_rating");
        m8.setValue("B");

        Component c2 = new Component();
        c2.setMeasures(Arrays.asList(m5, m6, m7, m8));
        RecuperationMeasures measures2 = new RecuperationMeasures();
        measures2.setComponent(c2);

        // Appel à la méthode à tester
        RecuperationMeasures result = UtilsService.concatenationMeasures(measures1, measures2);

        // Vérifications
        assertEquals("250.0", getValueByMetric(result, "lines_to_cover"));
        assertEquals("50.0", getValueByMetric(result, "uncovered_lines"));
        assertEquals("500.0", getValueByMetric(result, "sqale_index"));
        assertEquals("B", getValueByMetric(result, "reliability_rating"));
    }

    private String getValueByMetric(RecuperationMeasures measures, String metric) {
        return measures.getComponent().getMeasures().stream()
                .filter(m -> m.getMetric().equals(metric))
                .findFirst()
                .map(Measure::getValue)
                .orElse(null);
    }

    @ParameterizedTest(name = "given URL={0} when extractRepoPath then returns {1}")
    @CsvSource({
        "https://gitlab.insee.fr/group/projet, group/projet",
        "http://gitlab.insee.fr/group/projet, group/projet",
        "https://gitlab.insee.fr/group/projet/, group/projet",
        "https://gitlab.insee.fr/group/projet.git, group/projet"
    })
    void givenVariousGitlabUrls_whenExtractRepoPath_thenReturnCorrectPath(
            String url, String expectedPath) {
        // When
        String repoPath = utilsService.extractRepoPath(url);

        // Then
        assertThat(repoPath).isEqualTo(expectedPath);
    }
}
