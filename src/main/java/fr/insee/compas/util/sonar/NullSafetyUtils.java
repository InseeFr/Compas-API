package fr.insee.compas.util.sonar;

import java.util.function.Supplier;

public class NullSafetyUtils {

    private NullSafetyUtils() {}

    public static <R> R getOrDefault(Supplier<R> value, R defaultValue) {
        try {
            return value.get();
        } catch (NullPointerException e) {
            return defaultValue;
        }
    }
}
