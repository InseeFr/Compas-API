package fr.insee.compas.service.maturitecloud.indicateur;

import java.util.List;

import fr.insee.compas.dto.DemandeCreationStrategieCloud;

public interface ICloudCreation {
    List<Long> creerStrategieCloud(DemandeCreationStrategieCloud demande);
}
