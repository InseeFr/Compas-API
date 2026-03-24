package fr.insee.compas.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import fr.insee.compas.dto.meteo.DemandeCreationStrategieCloud;

public final class UtilsDemande {
    private static final ZoneId PARIS_ZONE = ZoneId.of("Europe/Paris");

    private UtilsDemande() {}

    public static void validateDemande(DemandeCreationStrategieCloud demande) {

        if (demande == null) {
            throw new IllegalArgumentException("La demande ne peut pas être nulle");
        }

        if (demande.getIdsModule() == null || demande.getIdsModule().isEmpty()) {
            throw new IllegalArgumentException("La liste des IDs de modules ne peut pas être vide");
        }

        if (demande.getDate() == null) {
            throw new IllegalArgumentException("La date ne peut pas être nulle");
        }

        if (demande.getAvancement() == null) {
            throw new IllegalArgumentException("L'avancement ne peut pas être nul");
        }

        if (demande.getEnvCibleProd() == null) {
            throw new IllegalArgumentException("L'environnement cible ne peut pas être nul");
        }

        if (demande.getAvancement().compareTo(BigDecimal.ONE) < 0
                || demande.getAvancement().compareTo(BigDecimal.valueOf(3)) > 0) {

            throw new IllegalArgumentException(
                    "L'avancement doit être compris entre 1 et 3, valeur reçue: "
                            + demande.getAvancement());
        }
    }

    public static LocalDate parseDate(String createdAt) {
        return Optional.ofNullable(createdAt)
                .map(Instant::parse)
                .map(instant -> instant.atZone(PARIS_ZONE).toLocalDate())
                .orElse(null);
    }
}
