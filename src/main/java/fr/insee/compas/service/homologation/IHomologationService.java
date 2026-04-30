package fr.insee.compas.service.homologation;

import java.util.List;

import fr.insee.compas.dto.HomologationDto;

public interface IHomologationService {
    List<HomologationDto> getAllHomologation();

    List<String> getAppliAbsentesOscar();
}
