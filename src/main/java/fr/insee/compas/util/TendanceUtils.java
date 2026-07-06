package fr.insee.compas.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.compas.Periode;
import fr.insee.compas.service.greenit.GreenItService;

import lombok.RequiredArgsConstructor;
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

        return new Periode(toDate(dateOrigine), toDate(datePassee));
    }

    public static LocalDate parse(String value) {
        return LocalDate.parse(value, FORMATTER);
    }

    public static Date toDate(LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Component
    @RequiredArgsConstructor
    public static class GreenPeriodeBuilder {
        private final GreenItService greenItService;

        public Periode buildPeriodeGreen(String origine, String passee) {
            if (origine != null && passee != null) {
                return new Periode(
                        toDate(TendanceUtils.parse(origine)), toDate(TendanceUtils.parse(passee)));
            }

            Set<LocalDate> validDatesSet = greenItService.getValidDates();
            if (validDatesSet.isEmpty()) {
                throw new IllegalStateException("Aucune date valide disponible pour le GreenIT");
            }

            List<LocalDate> sortedDates = validDatesSet.stream().sorted().toList();

            LocalDate dateOrigine;
            LocalDate datePassee;

            if (origine != null) {
                dateOrigine = TendanceUtils.parse(origine);
            } else {
                dateOrigine = sortedDates.getLast();
            }

            if (passee != null) {
                datePassee = TendanceUtils.parse(passee);
            } else if (sortedDates.size() >= 2) {
                datePassee = sortedDates.get(sortedDates.size() - 2);
            } else {
                datePassee = dateOrigine;
            }

            return new Periode(toDate(dateOrigine), toDate(datePassee));
        }
    }
}
