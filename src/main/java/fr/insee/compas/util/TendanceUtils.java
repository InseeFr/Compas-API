package fr.insee.compas.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import fr.insee.compas.model.compas.Periode;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TendanceUtils {
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public static Periode buildPeriode(String origine, String passee) throws ParseException {
        Date dateOrigine = origine == null ? new Date() : sdf.parse(origine);

        Date datePassee =
                passee == null
                        ? Date.from(
                                LocalDate.now()
                                        .minusMonths(1)
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .toInstant())
                        : sdf.parse(passee);

        return new Periode(dateOrigine, datePassee);
    }
}
