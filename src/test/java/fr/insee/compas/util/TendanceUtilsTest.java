package fr.insee.compas.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;

import fr.insee.compas.model.compas.Periode;

class TendanceUtilsTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static Date toDate(String date) {
        return Date.from(
                LocalDate.parse(date, FORMATTER).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void buildPeriodeSuccess() {
        Date origine = toDate("01/01/2020");
        Date passee = toDate("01/01/2019");
        Periode expected = new Periode(origine, passee);

        Periode actual = TendanceUtils.buildPeriode("01/01/2020", "01/01/2019");

        assertEquals(expected, actual);
    }

    @Test
    void buildPeriode_nullOrigine_usesToday() {
        Periode periode = TendanceUtils.buildPeriode(null, "01/01/2019");

        Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        assertEquals(today, periode.origine());
    }

    @Test
    void buildPeriode_nullPassee_usesLastMonth() {
        Periode periode = TendanceUtils.buildPeriode("01/01/2020", null);

        Date lastMonth =
                Date.from(
                        LocalDate.now()
                                .minusMonths(1)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant());
        assertEquals(lastMonth, periode.passee());
    }

    @Test
    void buildPeriode_invalidFormat_throwsDateTimeParseException() {
        assertThrows(
                DateTimeParseException.class,
                () -> TendanceUtils.buildPeriode("01/01-2020", "01/01/2019"));
    }
}
