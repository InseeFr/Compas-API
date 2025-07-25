package fr.insee.compas.model.greenit.util;

import java.math.BigDecimal;

public class ScoreGreenUtils {

    private ScoreGreenUtils() {}

    public static String gradeFromScore(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(0.2)) < 0) {
            return "A";
        }
        if (score.compareTo(BigDecimal.valueOf(0.4)) < 0) {
            return "B";
        }

        if (score.compareTo(BigDecimal.valueOf(0.6)) < 0) {
            return "C";
        }

        if (score.compareTo(BigDecimal.valueOf(0.8)) < 0) {
            return "D";
        }
        return "E";
    }
}
