package fr.insee.compas.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import fr.insee.compas.model.compas.Notation;

public class IndicateurViewUtil {

    private IndicateurViewUtil() {}

    public static String getGradeFromConsommationElectrique(Integer consommationElectrique) {
        if (consommationElectrique == null) {
            return null;
        }
        return getGradeFromConsommationElectrique(new BigDecimal(consommationElectrique));
    }

    public static String getGradeFromConsommationElectrique(BigDecimal consommationElectrique) {
        if (consommationElectrique == null) {
            return null;
        }

        return switch (consommationElectrique
                .divide(new BigDecimal("300"), RoundingMode.UP)
                .intValue()) {
            case 0 -> // 0 <= consommationElectrique <= 300
                    Notation.A.getGrade();
            case 1 -> // 301 <= consommationElectrique <= 600
                    Notation.B.getGrade();
            case 2 -> // 601 <= consommationElectrique <= 900
                    Notation.C.getGrade();
            default -> // consommationElectrique > 900
                    Notation.D.getGrade();
        };
    }
}
