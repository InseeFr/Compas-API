package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import fr.insee.compas.model.compas.Notation;

public class UtilsServiceTest {

    private final UtilsService utilsService = new UtilsService();

    @Test
    void testCalculPourcentageCouvertureTest() {
        double result = utilsService.calculPourcentageCouvertureTest(100, 20);
        assertEquals(80.0, result, 0.01, "Le pourcentage attendu est de 80%");
        double result2 = utilsService.calculPourcentageCouvertureTest(100, 100);
        assertEquals(0.0, result2, 0.01, "Le pourcentage attendu est de 0%");
        double result3 = utilsService.calculPourcentageCouvertureTest(0, 0);
        assertEquals(0.0, result3, 0.01, "Le pourcentage attendu est de 0%");
        double result4 = utilsService.calculPourcentageCouvertureTest(50, 60);
        assertEquals(-20.0, result4, 0.01, "Le pourcentage attendu est de -20%");
        double result5 = utilsService.calculPourcentageCouvertureTest(141108, 139768);
        assertEquals(1.0, result5, 0.01, "Le pourcentage attendu est de 1%");
    }

    @Test
    void testConvertPourcentageEnNote() {
        String result;
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

    @Test
    void testConvertNiveauCveEnLettre() {
        String result;
        result = utilsService.convertNiveauCveEnLettre(3.1);
        assertEquals("E", result, "La note attendu est de E");
        result = utilsService.convertNiveauCveEnLettre(2.1);
        assertEquals("D", result, "La note attendu est de D");
        result = utilsService.convertNiveauCveEnLettre(1.1);
        assertEquals("C", result, "La note attendu est de C");
        result = utilsService.convertNiveauCveEnLettre(0.9);
        assertEquals("B", result, "La note attendu est de B");
        result = utilsService.convertNiveauCveEnLettre(0);
        assertEquals("A", result, "La note attendu est de A");
    }

    @Test
    void testGetCalcul() {
        // Calcul attendu : (2 * 1000) + (3 * 100) + (4 * 10) + (5 * 1) + 1 = 2346
        BigDecimal expected = BigDecimal.valueOf(Math.log10(2346));
        BigDecimal actual =
                utilsService.getCalculIndicateurCve(
                        new BigDecimal("2"),
                        new BigDecimal("3"),
                        new BigDecimal("4"),
                        new BigDecimal("5"));

        // Vérification avec une tolérance pour éviter les erreurs de précision
        assertEquals(expected, actual);
    }

    @Test
    void testGetLettreDetteTechnique() {

        assertEquals(
                Notation.A.getGrade(),
                utilsService.getLettreDetteTechnique("2000"),
                "Valeur inférieure à 2100 doit retourner A");
        assertEquals(
                Notation.B.getGrade(),
                utilsService.getLettreDetteTechnique("5000"),
                "Valeur entre 2100 et 8400 doit retourner B");
        assertEquals(
                Notation.C.getGrade(),
                utilsService.getLettreDetteTechnique("10000"),
                "Valeur entre 8400 et 25200 doit retourner C");
        assertEquals(
                Notation.D.getGrade(),
                utilsService.getLettreDetteTechnique("30000"),
                "Valeur entre 25200 et 50400 doit retourner D");
        assertEquals(
                Notation.E.getGrade(),
                utilsService.getLettreDetteTechnique("60000"),
                "Valeur supérieure ou égale à 50400 doit retourner E");
    }
}
