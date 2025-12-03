package fr.insee.compas.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class AbstractIndicateurLettreTest {

    @Test
    void testCalculerLettreGlobaleAvecLettresValides() {
        var lettres = new AbstractIndicateurLettreTestImpl(List.of("A", "C", "B"));
        var lettres2 = new AbstractIndicateurLettreTestImpl(List.of("SO", "SO", "SO"));
        lettres.calculerLettreGlobale();
        assertEquals("B", lettres.getLettreGlobale());
        lettres2.calculerLettreGlobale();
        assertEquals("SO", lettres2.getLettreGlobale());
    }

    @Test
    void testCalculerLettreGlobaleAvecValeursVides() {
        var lettres = new AbstractIndicateurLettreTestImpl(List.of());
        lettres.calculerLettreGlobale();

        assertEquals("NR", lettres.getLettreGlobale());
    }

    @Test
    void testCalculerLettreGlobaleAvecNull() {
        var lettres = new AbstractIndicateurLettreTestImpl(null);
        lettres.calculerLettreGlobale();

        assertEquals("NR", lettres.getLettreGlobale());
    }

    @Test
    void testConversionLettreValeur() {
        var lettres = new AbstractIndicateurLettreTestImpl(List.of());
        assertEquals(1, lettres.convertirLettreEnValeur("A"));
        assertEquals(5, lettres.convertirLettreEnValeur("E"));
        assertEquals(0, lettres.convertirLettreEnValeur("SO"));
        assertEquals(-1, lettres.convertirLettreEnValeur("Z"));
    }
}
