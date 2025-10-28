package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConvertirValeurEnLettreServiceTest {

    private final ConvertirValeurEnLettreService convertirValeurEnLettreService =
            new ConvertirValeurEnLettreService();

    @Test
    void testConvertPourcentageEnNote() {
        String result;
        result = convertirValeurEnLettreService.convertPourcentageEnNote(95);
        assertEquals("A", result, "La note attendu est de A");
        result = convertirValeurEnLettreService.convertPourcentageEnNote(80);
        assertEquals("B", result, "La note attendu est de B");
        result = convertirValeurEnLettreService.convertPourcentageEnNote(65);
        assertEquals("B", result, "La note attendu est de B");
        result = convertirValeurEnLettreService.convertPourcentageEnNote(60);
        assertEquals("C", result, "La note attendu est de C");
        result = convertirValeurEnLettreService.convertPourcentageEnNote(50);
        assertEquals("C", result, "La note attendu est de C");
        result = convertirValeurEnLettreService.convertPourcentageEnNote(40);
        assertEquals("D", result, "La note attendu est de D");
        result = convertirValeurEnLettreService.convertPourcentageEnNote(20);
        assertEquals("E", result, "La note attendu est de E");
        result = convertirValeurEnLettreService.convertPourcentageEnNote(0);
        assertEquals("X", result, "La note attendu est de X");
        result = convertirValeurEnLettreService.convertPourcentageEnNote(-10);
        assertEquals("NR", result, "La note attendu est de NR");
    }

    @Test
    void testConvertNiveauCveEnLettre() {
        String result;
        result = convertirValeurEnLettreService.convertNiveauCveEnLettre(3.1);
        assertEquals("E", result, "La note attendu est de E");
        result = convertirValeurEnLettreService.convertNiveauCveEnLettre(2.1);
        assertEquals("D", result, "La note attendu est de D");
        result = convertirValeurEnLettreService.convertNiveauCveEnLettre(1.1);
        assertEquals("C", result, "La note attendu est de C");
        result = convertirValeurEnLettreService.convertNiveauCveEnLettre(0.9);
        assertEquals("B", result, "La note attendu est de B");
        result = convertirValeurEnLettreService.convertNiveauCveEnLettre(0);
        assertEquals("A", result, "La note attendu est de A");
    }
}
