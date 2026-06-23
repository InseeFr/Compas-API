package fr.insee.compas.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import fr.insee.compas.model.compas.Periode;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TendanceUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static Periode buildPeriode(String origine, String passee) {
        LocalDate dateOrigine =
                origine == null ? LocalDate.now() : LocalDate.parse(origine, FORMATTER);

        LocalDate datePassee =
                passee == null
                        ? LocalDate.now().minusMonths(1)
                        : LocalDate.parse(passee, FORMATTER);

        // Conversion en Date si Periode l'exige encore
        return new Periode(toDate(dateOrigine), toDate(datePassee));
    }

    private static Date toDate(LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
