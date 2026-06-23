package fr.insee.compas.repository.projection;

public interface DevopsProjection {
    Integer getIdApplication();

    Integer getIdModule();

    Integer getDistanceCount();

    Integer getNbDeploymentCount();

    Integer getNbContributorCount();
}
