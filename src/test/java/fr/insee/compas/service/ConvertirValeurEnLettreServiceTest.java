package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import fr.insee.compas.service.conversion.ConversionService;
import fr.insee.compas.service.conversion.strategie.NiveauCveConversion;
import fr.insee.compas.service.conversion.strategie.PourcentageEnNoteConversion;

class ConversionServiceTest {

    @Spy private PourcentageEnNoteConversion pourcentageConversion;

    @Spy private NiveauCveConversion niveauCveConversion;

    private ConversionService conversionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        conversionService =
                new ConversionService(
                        pourcentageConversion,
                        niveauCveConversion,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
    }

    @Test
    void testConvertPourcentageEnNote() {
        assertEquals("A", conversionService.convertPourcentageEnNote(95));
        assertEquals("B", conversionService.convertPourcentageEnNote(80));
        assertEquals("B", conversionService.convertPourcentageEnNote(65));
        assertEquals("C", conversionService.convertPourcentageEnNote(60));
        assertEquals("C", conversionService.convertPourcentageEnNote(50));
        assertEquals("D", conversionService.convertPourcentageEnNote(40));
        assertEquals("E", conversionService.convertPourcentageEnNote(20));
        assertEquals("X", conversionService.convertPourcentageEnNote(0));
        assertEquals("NR", conversionService.convertPourcentageEnNote(-10));
    }

    @Test
    void testConvertNiveauCveEnLettre() {
        assertEquals("E", conversionService.convertNiveauCveEnLettre(3.1));
        assertEquals("D", conversionService.convertNiveauCveEnLettre(2.1));
        assertEquals("C", conversionService.convertNiveauCveEnLettre(1.1));
        assertEquals("B", conversionService.convertNiveauCveEnLettre(0.9));
        assertEquals("A", conversionService.convertNiveauCveEnLettre(0));
    }
}
