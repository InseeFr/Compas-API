package fr.insee.compas.model.maturite;

public interface MaturiteIndicateurTableProjection {
    Integer getIdModule();

    Integer getIdApplication();

    Integer getIdIndicateur();

    String getCommentaire();

    Integer getValeur();
}
