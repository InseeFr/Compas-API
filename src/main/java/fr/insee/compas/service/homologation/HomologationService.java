package fr.insee.compas.service.homologation;

import java.util.*;

import org.springframework.http.*;
import org.springframework.stereotype.Service;

import fr.insee.compas.dto.HomologationDto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class HomologationService implements IHomologationService {

    private HomologationMapper homologationMapper;

    public List<HomologationDto> getAllHomologation() {
        Map<String, Integer> applicationMap = homologationMapper.getApplicationMap();
        return homologationMapper.getAllHomologationSep().stream()
                .filter(h -> h.getApsOscar() != null)
                .map(
                        h -> {
                            Integer id = applicationMap.get(h.getApsOscar().trim());
                            if (id == null) {
                                log.warn(
                                        "Application non trouvée dans Oscar : {}",
                                        h.getApsOscar().trim());
                                return null;
                            }
                            return new HomologationDto(
                                    id,
                                    h.getApsOscar(),
                                    h.getSensitivity(),
                                    h.getStatutHomologation(),
                                    h.getHomologationSI(),
                                    h.getHomologationBeginDate(),
                                    h.getHomologationEndDate(),
                                    h.getHomologationRemarks());
                        })
                .filter(Objects::nonNull)
                .toList();
    }

    public List<String> getAppliAbsentesOscar() {
        Map<String, Integer> applicationMap = homologationMapper.getApplicationMap();
        return homologationMapper.getAllHomologationSep().stream()
                .filter(h -> h.getApsOscar() != null)
                .map(h -> h.getApsOscar().trim())
                .filter(apsOscar -> !applicationMap.containsKey(apsOscar))
                .distinct()
                .sorted()
                .toList();
    } // retrait doublons
}
