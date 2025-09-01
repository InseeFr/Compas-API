package fr.insee.compas.util;

import lombok.Getter;

@Getter
public enum IndicatorSpecialValue {
    SO(-1), // Not Applicable
    NR(-2); // Not Reported

    private final int code;

    IndicatorSpecialValue(int code) {
        this.code = code;
    }
}
