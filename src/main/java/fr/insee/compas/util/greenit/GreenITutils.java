package fr.insee.compas.util.greenit;

import java.math.BigDecimal;

public class GreenITutils {

    private GreenITutils() {
        throw new IllegalStateException("Utility class");
    }

    public static String normalizeString(Number value) {
        return (value != null) ? value.toString() : "";
    }

    public static String normalizeStringAroundGo(Long value) {
        if (value == null) {
            return "";
        }

        long go = Math.round(value / (1024d * 1024 * 1024));
        return String.valueOf(go);
    }

    public static String gestionPourcentageOuSansObjet(BigDecimal b) {
        return b != null ? b.toString() : "SO";
    }
}
