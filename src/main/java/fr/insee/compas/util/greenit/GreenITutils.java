package fr.insee.compas.util.greenit;

import java.math.BigDecimal;
import java.util.function.Function;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GreenITutils {

    public enum ViewGreen {
        KUBE,
        VM
    }

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

    public static String normalizeStringToHourCpu(BigDecimal value) {
        if (value == null) {
            return null;
        }
        long h = Math.round(value.doubleValue() / 3600d);
        return String.valueOf(h);
    }

    public static <P, T> T safeGet(P projection, Function<P, T> getter) {
        return projection == null ? null : getter.apply(projection);
    }

    public static Long subtractExactSafe(Long a, Long b) {
        if (a == null || b == null) {
            return null;
        }
        return Math.subtractExact(a, b);
    }

    public static BigDecimal subtractSafe(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return null;
        }
        return a.subtract(b);
    }
}
