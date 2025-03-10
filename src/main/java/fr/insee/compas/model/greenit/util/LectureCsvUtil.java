package fr.insee.compas.model.greenit.util;

import java.math.BigDecimal;
import java.util.Optional;

public class LectureCsvUtil {

    private LectureCsvUtil() {
        super();
    }

    public static BigDecimal process(String ligne) {

        return Optional.ofNullable(ligne)
                .map(
                        l ->
                                BigDecimal.valueOf(
                                        Double.valueOf(
                                                l.replace(",", ".")
                                                        .replace(" ", "")
                                                        .replace("-", "0"))))
                .orElse(null);
    }
}
