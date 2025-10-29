package fr.insee.compas.service.devops;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import fr.insee.compas.model.compas.Notation;

class IndicateurDevopsLetterUtilsTest {

    // ------ calculLettreDistanceCount ------

    @Test
    void testCalculLettreDistanceCount_SO_NR() {
        assertEquals(
                Notation.SO.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDistanceCount("-1"));
        assertEquals(
                Notation.NR.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDistanceCount("-2"));
    }

    @Test
    void testCalculLettreDistanceCount_ClasseA() {
        assertEquals(
                Notation.A.getGrade(), IndicateurDevopsLetterUtils.calculLettreDistanceCount("0"));
        assertEquals(
                Notation.A.getGrade(), IndicateurDevopsLetterUtils.calculLettreDistanceCount("30"));
        assertEquals(
                Notation.A.getGrade(), IndicateurDevopsLetterUtils.calculLettreDistanceCount("15"));
    }

    @Test
    void testCalculLettreDistanceCount_ClasseB() {
        assertEquals(
                Notation.B.getGrade(), IndicateurDevopsLetterUtils.calculLettreDistanceCount("31"));
        assertEquals(
                Notation.B.getGrade(), IndicateurDevopsLetterUtils.calculLettreDistanceCount("90"));
        assertEquals(
                Notation.B.getGrade(), IndicateurDevopsLetterUtils.calculLettreDistanceCount("50"));
    }

    @Test
    void testCalculLettreDistanceCount_ClasseC() {
        assertEquals(
                Notation.C.getGrade(), IndicateurDevopsLetterUtils.calculLettreDistanceCount("91"));
        assertEquals(
                Notation.C.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDistanceCount("180"));
        assertEquals(
                Notation.C.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDistanceCount("150"));
    }

    @Test
    void testCalculLettreDistanceCount_ClasseD() {
        assertEquals(
                Notation.D.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDistanceCount("181"));
        assertEquals(
                Notation.D.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDistanceCount("360"));
        assertEquals(
                Notation.D.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDistanceCount("300"));
    }

    @Test
    void testCalculLettreDistanceCount_ClasseE() {
        assertEquals(
                Notation.E.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDistanceCount("361"));
        assertEquals(
                Notation.E.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDistanceCount("500"));
    }

    @Test
    void testCalculLettreDistanceCount_NullOrInvalid() {
        assertEquals(
                Notation.NR.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDistanceCount(null));
        assertEquals(
                Notation.NR.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDistanceCount("abc"));
        assertEquals(
                Notation.NR.getGrade(), IndicateurDevopsLetterUtils.calculLettreDistanceCount(""));
    }

    // ------ calculLettreDeploymentCount ------

    @Test
    void testCalculLettreDeploymentCount_SO_NR() {
        assertEquals(
                Notation.SO.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDeploymentCount("-1"));
        assertEquals(
                Notation.NR.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDeploymentCount("-2"));
    }

    @Test
    void testCalculLettreDeploymentCount_ClasseE() {
        assertEquals(
                Notation.E.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDeploymentCount("0"));
    }

    @Test
    void testCalculLettreDeploymentCount_ClasseD() {
        assertEquals(
                Notation.D.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDeploymentCount("1"));
    }

    @Test
    void testCalculLettreDeploymentCount_ClasseC() {
        assertEquals(
                Notation.C.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDeploymentCount("2"));
    }

    @Test
    void testCalculLettreDeploymentCount_ClasseB() {
        assertEquals(
                Notation.B.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDeploymentCount("3"));
    }

    @Test
    void testCalculLettreDeploymentCount_ClasseA() {
        assertEquals(
                Notation.A.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDeploymentCount("4"));
        assertEquals(
                Notation.A.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDeploymentCount("50"));
        assertEquals(
                Notation.A.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDeploymentCount("1000"));
    }

    @Test
    void testCalculLettreDeploymentCount_NullOrInvalid() {
        assertEquals(
                Notation.NR.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDeploymentCount(null));
        assertEquals(
                Notation.NR.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDeploymentCount("abc"));
        assertEquals(
                Notation.NR.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreDeploymentCount(""));
    }

    // ------ calculLettreContributorCount ------

    @Test
    void testCalculLettreContributorCount_SO_NR() {
        assertEquals(
                Notation.SO.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreContributorCount("-1"));
        assertEquals(
                Notation.NR.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreContributorCount("-2"));
    }

    @Test
    void testCalculLettreContributorCount_A_B_C_D_E() {
        assertEquals(
                Notation.E.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreContributorCount("0"));
        assertEquals(
                Notation.D.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreContributorCount("1"));
        assertEquals(
                Notation.C.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreContributorCount("2"));
        assertEquals(
                Notation.B.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreContributorCount("3"));
        assertEquals(
                Notation.A.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreContributorCount("4"));
        assertEquals(
                Notation.A.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreContributorCount("7"));
    }

    @Test
    void testCalculLettreContributorCount_NullOrInvalid() {
        assertEquals(
                Notation.NR.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreContributorCount(null));
        assertEquals(
                Notation.NR.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreContributorCount("notanumber"));
        assertEquals(
                Notation.NR.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreContributorCount(""));
    }

    @Test
    void testCalculLettreContributorCount_ValeursNegativesNonGerees() {
        // Cas de valeurs négatives non prévues (-3, -10, etc.)
        assertEquals(
                Notation.NR.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreContributorCount("-3"));
        assertEquals(
                Notation.NR.getGrade(),
                IndicateurDevopsLetterUtils.calculLettreContributorCount("-10"));
    }

    @Test
    void testCalculLettreContributorCount_DuplicateOffset_Normal() {
        // Valeurs valides avec offset (1000 + valeur)
        assertEquals(
                Notation.E.getGrade() + " d",
                IndicateurDevopsLetterUtils.calculLettreContributorCount("1000")); // 0 + offset
        assertEquals(
                Notation.D.getGrade() + " d",
                IndicateurDevopsLetterUtils.calculLettreContributorCount("1001")); // 1 + offset
        assertEquals(
                Notation.C.getGrade() + " d",
                IndicateurDevopsLetterUtils.calculLettreContributorCount("1002")); // 2 + offset
        assertEquals(
                Notation.B.getGrade() + " d",
                IndicateurDevopsLetterUtils.calculLettreContributorCount("1003")); // 3 + offset
        assertEquals(
                Notation.A.getGrade() + " d",
                IndicateurDevopsLetterUtils.calculLettreContributorCount("1004")); // 4 + offset
    }
}
