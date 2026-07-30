package fr.insee.compas.repository.projection.maturite;

public interface MaturiteIndicateurTableProjection {
    Integer getIdModule();

    Integer getIdApplication();

    Integer getIdIndicateur();

    String getCommentaire();

    Integer getValeur();
}
