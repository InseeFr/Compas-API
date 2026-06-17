package fr.insee.compas.model.compas;

import lombok.Getter;

@Getter
public enum IndicateurSonar {
    LINES_TO_COVER("lines_to_cover", IndicateurType.NBR_LIGNE),
    UNCOVERED_LINES("", IndicateurType.NBR_LIGNE_TEST),
    SQALE_INDEX("sqale_index", IndicateurType.DETTE_TECH),
    FIABILITE("reliability_rating", IndicateurType.FIABILITE);

    private final String key;
    private final IndicateurType indicateurType;

    IndicateurSonar(String key, IndicateurType indicateurType) {
        this.key = key;
        this.indicateurType = indicateurType;
    }
}
