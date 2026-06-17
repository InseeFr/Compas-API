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
                        .lettreCouvertureTestUnitaire("C")
                        .build();

        m.calculerLettreGlobalQualiteEtEvolution();
        assertEquals("B", m.getLettreGlobalQualite());
    }

    @Test
    void testUneLettreX() {
        IndicateurQualiteView m =
                IndicateurQualiteView.builder()
                        .lettreDetteTechnique("A")
                        .lettreFiabilite("X")
                        .lettreCouvertureTestUnitaire("B")
                        .build();

        m.calculerLettreGlobalQualiteEtEvolution();
        assertEquals("C", m.getLettreGlobalQualite());
    }

    @Test
    void testDeuxLettresX() {
        IndicateurQualiteView m =
                IndicateurQualiteView.builder()
                        .lettreDetteTechnique("X")
                        .lettreFiabilite("X")
                        .lettreCouvertureTestUnitaire("C")
                        .build();

        m.calculerLettreGlobalQualiteEtEvolution();
        assertEquals("D", m.getLettreGlobalQualite());
    }

    @Test
    void testToutesLettresX() {
        IndicateurQualiteView m =
                IndicateurQualiteView.builder()
                        .lettreDetteTechnique("X")
                        .lettreFiabilite("X")
                        .lettreCouvertureTestUnitaire("X")
                        .build();

        m.calculerLettreGlobalQualiteEtEvolution();
        assertEquals("E", m.getLettreGlobalQualite());
    }

    @Test
    void testAvecAetE() {
        IndicateurQualiteView m =
                IndicateurQualiteView.builder()
                        .lettreDetteTechnique("A") // 1
                        .lettreFiabilite("E") // 5
                        .lettreCouvertureTestUnitaire("X") // 5
                        .build();

        m.calculerLettreGlobalQualiteEtEvolution();

        assertEquals("D", m.getLettreGlobalQualite());
    }
}
