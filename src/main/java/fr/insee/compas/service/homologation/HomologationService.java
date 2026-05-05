package fr.insee.compas.service.homologation;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.*;
import org.springframework.stereotype.Service;

import fr.insee.compas.client.view.ApplicationTechnique;
import fr.insee.compas.dto.HomologationDoublonsGristDto;
import fr.insee.compas.dto.HomologationDto;
import fr.insee.compas.model.homologation.Homologation;
import fr.insee.compas.service.OscarService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class HomologationService implements IHomologationService {

    private HomologationMapper homologationMapper;

    private OscarService oscarService;

    public List<HomologationDto> getAllHomologation() {
        List<Homologation> csvList = homologationMapper.getAllHomologationSep();

        // Map pour dédupliquer par (ID Oscar + nomSI)
        Map<String, HomologationDto> uniqueApps = new LinkedHashMap<>();

        csvList.stream()
                .filter(h -> h.getApsOscar() != null && !h.getApsOscar().isBlank())
                .forEach(
                        h -> {
                            String nomApp = h.getApsOscar().trim();

                            ApplicationTechnique app = oscarService.findApplicationByName(nomApp);

                            if (app != null) {
                                String cle =
                                        app.getId()
                                                + "|"
                                                + (h.getNomSI() != null ? h.getNomSI() : "");

                                uniqueApps.putIfAbsent(
                                        cle,
                                        new HomologationDto(
                                                app.getId(),
                                                app.getNom(),
                                                h.getSensitivity(),
                                                h.getStatutHomologation(),
                                                h.getHomologationSI(),
                                                h.getHomologationBeginDate(),
                                                h.getHomologationEndDate(),
                                                h.getHomologationRemarks()));
                            }
                        });

        return new ArrayList<>(uniqueApps.values());
    }

    // liste des applis Oscar absentes dans Grist
    public List<String> getAppliAbsentesOscar() {
        List<String> nomsGrist = getApplicationGrist();

        return nomsGrist.stream()
                .filter(nom -> oscarService.findApplicationByName(nom) == null)
                .toList();
    }

    public List<String> getApplicationGrist() {
        return homologationMapper.getAllHomologationSep().stream()
                .map(Homologation::getApsOscar)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct() //
                .sorted()
                .toList();
    }

    // liste des applis Oscar doublons dans grist
    public List<HomologationDoublonsGristDto> getDoublonsGrist() {
        List<Homologation> homologations = homologationMapper.getAllHomologationSep();

        Map<String, List<Homologation>> groupeParApplication =
                homologations.stream()
                        .filter(h -> h.getApsOscar() != null)
                        .filter(h -> !h.getApsOscar().trim().isEmpty())
                        .collect(Collectors.groupingBy(h -> h.getApsOscar().trim()));

        return groupeParApplication.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(
                        entry -> {
                            String nomApp = entry.getKey();
                            List<Homologation> occurrences = entry.getValue();

                            List<String> listeSI =
                                    occurrences.stream()
                                            .map(Homologation::getNomSI)
                                            .filter(si -> si != null && !si.trim().isEmpty())
                                            .distinct()
                                            .sorted()
                                            .toList();

                            return HomologationDoublonsGristDto.builder()
                                    .nomApplication(nomApp)
                                    .nombreOccurrences(occurrences.size())
                                    .listeSI(listeSI)
                                    .build();
                        })
                .sorted(
                        Comparator.comparing(HomologationDoublonsGristDto::getNombreOccurrences)
                                .reversed())
                .toList();
    }
}
