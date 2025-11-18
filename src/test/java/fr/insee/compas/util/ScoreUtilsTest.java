package fr.insee.compas.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ScoreUtilsTest {

    @Test
    void testNormalizeStandardCase() {
        final double result = ScoreUtils.normalize(5.0, 0.0, 10.0);
        assertEquals(0.5, result, 1e-6);
    }

    @Test
    void testNormalizeValueIsNaN() {
        final double result = ScoreUtils.normalize(Double.NaN, 0.0, 10.0);
        assertEquals(0.0, result, 1e-6);
    }

    @Test
    void testNormalizeMinIsNaN() {
        final double result = ScoreUtils.normalize(5.0, Double.NaN, 10.0);
        assertEquals(0.0, result, 1e-6);
    }

    @Test
    void testNormalizeMaxIsNaN() {
        final double result = ScoreUtils.normalize(5.0, 0.0, Double.NaN);
        assertEquals(0.0, result, 1e-6);
    }

    @Test
    void testNormalizeMaxEqualsMin() {
        final double result = ScoreUtils.normalize(5.0, 5.0, 5.0);
        assertEquals(0.0, result, 1e-6);
    }

    @Test
    void testNormalizeNegativeRange() {
        final double result = ScoreUtils.normalize(-5.0, -10.0, 0.0);
        assertEquals(0.5, result, 1e-6);
    }

    @ParameterizedTest(name = "[OK] \"{0}\" -> true")
    @DisplayName(
            "isPlateformeProd: doit retourner true quand la chaîne commence par \"pd\" (casse"
                    + " sensible)")
    @ValueSource(
            strings = {
                "pd",
                "pd-",
                "pd1",
                "pd_dev",
                "pdt",
                "pdpd",
                "pd " // espaces après : toujours true car commence par "pd"
            })
    void returnsTrue_whenStartsWithPd(String input) {
        assertTrue(ScoreUtils.isPlateformeProd(input));
    }

    @ParameterizedTest(name = "[KO] \"{0}\" -> false")
    @DisplayName(
            "isPlateformeProd: doit retourner false sinon (y compris casse différente, espaces"
                    + " devant, etc.)")
    @ValueSource(
            strings = {
                "",
                "p",
                "xpd",
                " pd", // espace devant -> ne commence pas par "pd"
                "PD", // casse différente -> false avec startsWith("pd")
                "pD",
                "Pd",
                "prod", // ne commence pas par "pd"
                "rp d" // bruit quelconque
            })
    void returnsFalse_otherwise(String input) {
        assertFalse(ScoreUtils.isPlateformeProd(input));
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("isPlateformeProd: null -> false")
    void returnsFalse_whenNull(String input) {
        assertFalse(ScoreUtils.isPlateformeProd(input));
    }
}
