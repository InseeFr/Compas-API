package fr.insee.compas.model.meteo;

import java.util.List;
import java.util.Optional;

import fr.insee.compas.util.MeteoAlerteUtils;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MailAlerteMeteo {
    List<Meteo> appsMeteo;
    MeteoAlerteUtils.AlerteType type;
    Boolean isTest;
    Optional<String> responsableEmail;
    Optional<String> responsableAdjEmail;
}
