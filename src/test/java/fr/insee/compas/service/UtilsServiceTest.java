package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UtilsServiceTest {

    private final UtilsService utilsService = new UtilsService();

    @Test
    public void testCalculPourcentageCouvertureTest() {
        double result = utilsService.calculPourcentageCouvertureTest(100, 20);
        assertEquals(80.0, result, 0.01, "Le pourcentage attendu est de 80%");
        double result2 = utilsService.calculPourcentageCouvertureTest(100, 100);
        assertEquals(0.0, result2, 0.01, "Le pourcentage attendu est de 0%");
        double result3 = utilsService.calculPourcentageCouvertureTest(0, 0);
        assertEquals(0.0, result3, 0.01, "Le pourcentage attendu est de 0%");
        double result4 = utilsService.calculPourcentageCouvertureTest(50, 60);
        assertEquals(-20.0, result4, 0.01, "Le pourcentage attendu est de -20%");
    }

    @Test
    public void testconvertPourcentageEnNote() {
        String result = "";
        result = utilsService.convertPourcentageEnNote(95);
        assertEquals("A", result, "La note attendu est de A");
        result = utilsService.convertPourcentageEnNote(80);
        assertEquals("B", result, "La note attendu est de B");
        result = utilsService.convertPourcentageEnNote(65);
        assertEquals("B", result, "La note attendu est de B");
        result = utilsService.convertPourcentageEnNote(60);
        assertEquals("C", result, "La note attendu est de C");
        result = utilsService.convertPourcentageEnNote(50);
        assertEquals("C", result, "La note attendu est de C");
        result = utilsService.convertPourcentageEnNote(40);
        assertEquals("D", result, "La note attendu est de D");
        result = utilsService.convertPourcentageEnNote(20);
        assertEquals("E", result, "La note attendu est de E");
        result = utilsService.convertPourcentageEnNote(0);
        assertEquals("X", result, "La note attendu est de X");
        result = utilsService.convertPourcentageEnNote(-10);
        assertEquals("NR", result, "La note attendu est de NR");
    }
}
