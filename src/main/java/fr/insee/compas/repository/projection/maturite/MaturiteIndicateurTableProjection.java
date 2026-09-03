package fr.insee.compas.repository.projection.maturite;

import java.math.BigDecimal;

public interface MaturiteIndicateurTableProjection {
    Integer getIdModule();

    Integer getIdApplication();

    Integer getIdIndicateur();

    String getCommentaire();

    BigDecimal getValeur();
}
