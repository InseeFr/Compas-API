package fr.insee.compas.util.security;

import java.util.Optional;

import fr.insee.compas.repository.projection.SecuriteProjection;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CalculSecurityUtils {

    public static double getCalculIndicateurCve(Integer c, Integer e, Integer m, Integer f) {
        int somme = c * 1000 + e * 100 + m * 10 + f + 1;
        return Math.log10(somme);
    }

    public static boolean hasCveOnProjection(SecuriteProjection securiteProjection) {
        return securiteProjection.getNbCveCritical() != null
                || securiteProjection.getNbCveHigh() != null
                || securiteProjection.getNbCveMedium() != null
                || securiteProjection.getNbCveLow() != null;
    }

    public static String returnStringOfAnInteger(Integer value) {
        return Optional.ofNullable(value).map(String::valueOf).orElse("");
    }
}
