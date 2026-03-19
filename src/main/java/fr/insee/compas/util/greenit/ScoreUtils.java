package fr.insee.compas.util.greenit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScoreUtils {

    private static final double EPSILON = 1e-6;

    private ScoreUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static double normalize(double value, double min, double max) {
        if (Double.isNaN(value)
                || Double.isNaN(min)
                || Double.isNaN(max)
                || Math.abs(max - min) < EPSILON) {
            log.debug("gestion du dénominateur Nan");
            return 0.0;
        }
        return (value - min) / (max - min);
    }

    public static boolean isCloseToZero(double value) {
        return Math.abs(value) < EPSILON;
    }

    public static boolean isPlateformeProd(String plateforme) {

        return (plateforme != null && plateforme.startsWith("pd"));
    }
}
