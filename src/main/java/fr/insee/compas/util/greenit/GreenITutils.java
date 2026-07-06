package fr.insee.compas.util.greenit;

import java.math.BigDecimal;
import java.util.function.ToLongFunction;

import fr.insee.compas.repository.projection.GreenItAppProjection;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GreenITutils {

    public static String normalizeString(Number value) {
        return (value != null) ? value.toString() : null;
    }

    public static String normalizeStringAroundGo(Long value) {
        if (value == null) {
            return null;
        }

        long go = Math.round(value / (1024d * 1024 * 1024));
        return String.valueOf(go);
    }

    public static BigDecimal orZeroHistBigDecimal(
            GreenItAppProjection proj,
            java.util.function.Function<GreenItAppProjection, BigDecimal> getter) {
        return proj != null ? orZeroBigDecimal(getter.apply(proj)) : orZeroBigDecimal(null);
    }

    public static Long orZeroHistLong(
            GreenItAppProjection proj, ToLongFunction<GreenItAppProjection> getter) {
        if (proj == null) {
            return orZero(null);
        }
        try {
            return getter.applyAsLong(proj);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static BigDecimal orZeroBigDecimal(BigDecimal value) {
        return value;
    }

    public static Long orZero(Long value) {
        return value;
    }
}
