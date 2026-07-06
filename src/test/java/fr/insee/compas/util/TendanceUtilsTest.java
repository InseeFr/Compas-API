package fr.insee.compas.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.compas.Periode;
import fr.insee.compas.service.greenit.GreenItService;

@ExtendWith(MockitoExtension.class)
class TendanceUtilsTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Mock private GreenItService greenItService;

    @InjectMocks private TendanceUtils.GreenPeriodeBuilder greenPeriodeBuilder;

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

    @Test
    void buildPeriodeGreen_bothDatesProvided_returnsExpectedPeriode() {
        Periode expected = new Periode(toDate("01/03/2024"), toDate("01/02/2024"));

        Periode actual = greenPeriodeBuilder.buildPeriodeGreen("01/03/2024", "01/02/2024");

        assertEquals(expected, actual);
    }

    @Test
    void buildPeriodeGreen_noDates_usesTwoLatestValidDates() {
        Set<LocalDate> validDates = new LinkedHashSet<>();
        validDates.add(LocalDate.of(2024, 1, 1));
        validDates.add(LocalDate.of(2024, 2, 1));
        validDates.add(LocalDate.of(2024, 3, 1));

        when(greenItService.getValidDates()).thenReturn(validDates);

        Periode expected =
                new Periode(
                        TendanceUtils.toDate(LocalDate.of(2024, 3, 1)),
                        TendanceUtils.toDate(LocalDate.of(2024, 2, 1)));

        Periode actual = greenPeriodeBuilder.buildPeriodeGreen(null, null);

        assertEquals(expected, actual);
    }

    @Test
    void buildPeriodeGreen_onlyOrigineProvided_usesProvidedOrigineAndPreviousValidDate() {
        Set<LocalDate> validDates = new LinkedHashSet<>();
        validDates.add(LocalDate.of(2024, 1, 1));
        validDates.add(LocalDate.of(2024, 2, 1));
        validDates.add(LocalDate.of(2024, 3, 1));

        when(greenItService.getValidDates()).thenReturn(validDates);

        Periode expected =
                new Periode(
                        TendanceUtils.toDate(LocalDate.of(2024, 3, 1)),
                        TendanceUtils.toDate(LocalDate.of(2024, 2, 1)));

        Periode actual = greenPeriodeBuilder.buildPeriodeGreen("01/03/2024", null);

        assertEquals(expected, actual);
    }

    @Test
    void buildPeriodeGreen_onlyPasseeProvided_usesLatestValidDateAndProvidedPassee() {
        Set<LocalDate> validDates = new LinkedHashSet<>();
        validDates.add(LocalDate.of(2024, 1, 1));
        validDates.add(LocalDate.of(2024, 2, 1));
        validDates.add(LocalDate.of(2024, 3, 1));

        when(greenItService.getValidDates()).thenReturn(validDates);

        Periode expected =
                new Periode(
                        TendanceUtils.toDate(LocalDate.of(2024, 3, 1)),
                        TendanceUtils.toDate(LocalDate.of(2024, 1, 1)));

        Periode actual = greenPeriodeBuilder.buildPeriodeGreen(null, "01/01/2024");

        assertEquals(expected, actual);
    }

    @Test
    void buildPeriodeGreen_singleValidDate_usesSameDateTwice() {
        Set<LocalDate> validDates = Set.of(LocalDate.of(2024, 5, 1));

        when(greenItService.getValidDates()).thenReturn(validDates);

        Periode expected =
                new Periode(
                        TendanceUtils.toDate(LocalDate.of(2024, 5, 1)),
                        TendanceUtils.toDate(LocalDate.of(2024, 5, 1)));

        Periode actual = greenPeriodeBuilder.buildPeriodeGreen(null, null);

        assertEquals(expected, actual);
    }

    @Test
    void buildPeriodeGreen_emptyValidDates_throwsIllegalStateException() {
        when(greenItService.getValidDates()).thenReturn(Set.of());

        assertThrows(
                IllegalStateException.class,
                () -> greenPeriodeBuilder.buildPeriodeGreen(null, null));
    }
}
