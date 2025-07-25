package fr.insee.compas.model.greenit.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ScoreGreenUtilsTest {

    @Test
    void testGradeBoundaries() {
        assertEquals("A", ScoreGreenUtils.gradeFromScore(BigDecimal.valueOf(0.1)));
        assertEquals("B", ScoreGreenUtils.gradeFromScore(BigDecimal.valueOf(0.3)));
        assertEquals("C", ScoreGreenUtils.gradeFromScore(BigDecimal.valueOf(0.5)));
        assertEquals("D", ScoreGreenUtils.gradeFromScore(BigDecimal.valueOf(0.7)));
        assertEquals("E", ScoreGreenUtils.gradeFromScore(BigDecimal.valueOf(0.9)));
    }
}
