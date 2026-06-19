package fr.insee.compas.repository.projection;

public interface SecuriteProjection {
    Integer getId();

    Integer getNbCveCritical();

    Integer getNbCveHigh();

    Integer getNbCveLow();

    Integer getNbCveMedium();

    Integer getNbVmNonMaj();

    Integer getDelaiMaj();
}
