package fr.insee.compas.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;

import fr.insee.compas.model.compas.Periode;

class TendanceUtilsTest {
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Test
    void buildPeriodeSuccess() throws ParseException {
        Date origine = sdf.parse("01/01/2020");
        Date passee = sdf.parse("01/01/2019");
        Periode periode = new Periode(origine, passee);
        Periode periode1 = TendanceUtils.buildPeriode("01/01/2020", "01/01/2019");
        assertEquals(periode, periode1);
    }

    @Test
    void throwException() {
        assertThrows(
                ParseException.class, () -> TendanceUtils.buildPeriode("01/01-2020", "01/01/2019"));
    }
}
