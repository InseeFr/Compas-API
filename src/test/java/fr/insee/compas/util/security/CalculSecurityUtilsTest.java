package fr.insee.compas.util.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import fr.insee.compas.repository.projection.SecuriteProjection;

class CalculSecurityUtilsTest {

    @Test
    void getCalculIndicateurCveSuccess() {
        int somme = 10 * 1000 + 9 * 100 + 8 * 10 + 7 + 1;
        double sommeResult = CalculSecurityUtils.getCalculIndicateurCve(10, 9, 8, 7);
        assertEquals(Math.log10(somme), sommeResult);
    }

    @Test
    void getCalculIndicateurCveFail() {
        assertThrows(
                NullPointerException.class,
                () -> CalculSecurityUtils.getCalculIndicateurCve(10, 9, null, 7));
    }

    @Test
    void hasCveOnProjectSuccess() {
        SecuriteProjection securiteProjection =
                new SecuriteProjection() {
                    public Integer getId() {
                        return 1;
                    }

                    public Integer getNbCveCritical() {
                        return 10;
                    }

                    public Integer getNbCveHigh() {
                        return 2;
                    }

                    public Integer getNbCveLow() {
                        return 0;
                    }

                    public Integer getNbCveMedium() {
                        return 0;
                    }

                    public Integer getNbVmNonMaj() {
                        return 0;
                    }

                    public Integer getDelaiMaj() {
                        return 0;
                    }
                };
        assertTrue(CalculSecurityUtils.hasCveOnProjection(securiteProjection));
    }

    @Test
    void hasCveOnProjectFalse() {
        SecuriteProjection securiteProjection =
                new SecuriteProjection() {
                    public Integer getId() {
                        return 1;
                    }

                    public Integer getNbCveCritical() {
                        return null;
                    }

                    public Integer getNbCveHigh() {
                        return null;
                    }

                    public Integer getNbCveLow() {
                        return null;
                    }

                    public Integer getNbCveMedium() {
                        return null;
                    }

                    public Integer getNbVmNonMaj() {
                        return null;
                    }

                    public Integer getDelaiMaj() {
                        return null;
                    }
                };
        assertFalse(CalculSecurityUtils.hasCveOnProjection(securiteProjection));
    }

    @Test
    void returnStringOfAnIntegerSuccess() {
        assertEquals("10", CalculSecurityUtils.returnStringOfAnInteger(10));
    }

    @Test
    void returnStringOfAnIntegerIfNull() {
        assertEquals("", CalculSecurityUtils.returnStringOfAnInteger(null));
    }
}
