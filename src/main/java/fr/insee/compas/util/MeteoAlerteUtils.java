package fr.insee.compas.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import fr.insee.compas.model.meteo.Meteo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MeteoAlerteUtils {
    private static final Pattern EMAIL_RX =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public record AlerteMailContext(
            String rgaEmail,
            List<Meteo> apps,
            boolean test,
            String emailResponsable,
            String emailAdjResponsable,
            String balfMetier,
            AlerteType type,
            int ageMinJours) {}

    public enum AlerteType {
        NONE,
        RAPPEL,
        RETARD
    }

    /** Classification : NONE (<23j), RAPPEL (23j.. <1 mois), RETARD (≥1 mois ou null). */
    public static AlerteType classify(LocalDate dateMeteo, LocalDate today) {
        if (dateMeteo == null) return AlerteType.RETARD;
        long days = ChronoUnit.DAYS.between(dateMeteo, today);
        if (days < 23) return AlerteType.NONE;
        LocalDate oneMonthAgo = today.minusMonths(1);
        if (!dateMeteo.isAfter(oneMonthAgo)) return AlerteType.RETARD;
        return AlerteType.RAPPEL;
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            log.warn("Email est null");
            return false;
        }
        if (email.isBlank()) {
            log.warn("Email est vide/blanc: '{}'", email);
            return false;
        }

        String trimmed = email.trim();
        boolean matches = EMAIL_RX.matcher(trimmed).matches();

        if (!matches) {
            log.warn(
                    "Email rejeté: '{}' (longueur: {}, après trim: '{}', longueur: {})",
                    email,
                    email.length(),
                    trimmed,
                    trimmed.length());
            log.warn(
                    "Bytes: {}",
                    email.chars()
                            .mapToObj(c -> String.format("%02x", c))
                            .collect(Collectors.joining(" ")));
        }

        return matches;
    }

    public static long daysOld(Meteo m, LocalDate today) {
        if (m.getDate() == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(m.getDate(), today);
    }

    public static String normalizeSndi(String sndi) {
        if (sndi == null || sndi.isBlank()) {
            return "";
        }
        return sndi.trim().toLowerCase().replaceFirst("^sndi\\s+", "");
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
