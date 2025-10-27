package fr.insee.compas.util;

import java.math.BigDecimal;

public class GreenITutils {

    private GreenITutils() {
        throw new IllegalStateException("Utility class");
    }

    public static String normalizeString(Number value) {
        return (value != null) ? value.toString() : "";
    }

    public static String gestionPourcentageOuSansObjet(BigDecimal b) {
        return b != null ? b.toString() : "SO";
    }
}
