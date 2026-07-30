package fr.insee.compas.repository.projection.maturite;

public interface DerniereMaturiteProjection {
    Integer getIdModule();
    Integer getIdIndicateur();
    String getCommentaire();
    Integer getValeur();
}
