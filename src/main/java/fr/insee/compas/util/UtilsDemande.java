package fr.insee.compas.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

public final class UtilsDemande {
    private static final ZoneId PARIS_ZONE = ZoneId.of("Europe/Paris");

    private UtilsDemande() {}

    public static LocalDate parseDate(String createdAt) {
        return Optional.ofNullable(createdAt)
                .map(Instant::parse)
                .map(instant -> instant.atZone(PARIS_ZONE).toLocalDate())
                .orElse(null);
    }
}
