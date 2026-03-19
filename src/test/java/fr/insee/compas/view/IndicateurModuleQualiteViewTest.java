package fr.insee.compas.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IndicateurModuleQualiteViewTest {

    @Test
    void testLettresToutesValides() {
        IndicateurQualiteView m =
                IndicateurQualiteView.builder()
                        .lettreDetteTechnique("A")
                        .lettreFiabilite("B")
                        .lettreCouvertureTestUniaire("C")
                        .build();

        m.calculerLettreGlobalQualite();
        assertEquals("B", m.getLettreGlobalQualite());
    }

    @Test
    void testUneLettreX() {
        IndicateurQualiteView m =
                IndicateurQualiteView.builder()
                        .lettreDetteTechnique("A")
                        .lettreFiabilite("X")
                        .lettreCouvertureTestUniaire("B")
                        .build();

        m.calculerLettreGlobalQualite();
        assertEquals("C", m.getLettreGlobalQualite());
    }

    @Test
    void testDeuxLettresX() {
        IndicateurQualiteView m =
                IndicateurQualiteView.builder()
                        .lettreDetteTechnique("X")
                        .lettreFiabilite("X")
                        .lettreCouvertureTestUniaire("C")
                        .build();

        m.calculerLettreGlobalQualite();
        assertEquals("D", m.getLettreGlobalQualite());
    }

    @Test
    void testToutesLettresX() {
        IndicateurQualiteView m =
                IndicateurQualiteView.builder()
                        .lettreDetteTechnique("X")
                        .lettreFiabilite("X")
                        .lettreCouvertureTestUniaire("X")
                        .build();

        m.calculerLettreGlobalQualite();
        assertEquals("E", m.getLettreGlobalQualite());
    }

    @Test
    void testAvecAetE() {
        IndicateurQualiteView m =
                IndicateurQualiteView.builder()
                        .lettreDetteTechnique("A") // 1
                        .lettreFiabilite("E") // 5
                        .lettreCouvertureTestUniaire("X") // 5
                        .build();

        m.calculerLettreGlobalQualite();

        assertEquals("D", m.getLettreGlobalQualite());
    }
}
