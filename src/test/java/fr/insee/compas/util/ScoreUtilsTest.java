package fr.insee.compas.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
}
