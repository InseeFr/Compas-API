package fr.insee.compas.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.Notation;

@Service
public class UtilsService {

    public double calculPourcentageCouvertureTest(Integer ligneCode, Integer ligneCodeNonTeste) {
        return ligneCode > 0 ? (1 - ((double) ligneCodeNonTeste / ligneCode)) * 100 : 0.0;
    }

    public String convertPourcentageEnNote(double percentage) {
        if (percentage > 80) {
            return Notation.A.getGrade();
        } else if (percentage > 60) {
            return Notation.B.getGrade();
        } else if (percentage > 40) {
            return Notation.C.getGrade();
        } else if (percentage > 20) {
            return Notation.D.getGrade();
        } else if (percentage > 0) {
            return Notation.E.getGrade();
        } else if (percentage == 0) {
            return Notation.X.getGrade();
        } else {
            return "NR";
        }
    }

    public String convertNiveauCveEnLettre(double niveau) {
        if (niveau >= 3) {
            return Notation.E.getGrade();
        } else if (niveau >= 2) {
            return Notation.D.getGrade();
        } else if (niveau >= 1) {
            return Notation.C.getGrade();
        } else if (niveau > 0) {
            return Notation.B.getGrade();
        } else {
            return Notation.A.getGrade();
        }
    }

    public BigDecimal getCalculIndicateurCve(
            BigDecimal c, BigDecimal e, BigDecimal m, BigDecimal f) {
        BigDecimal somme =
                c.multiply(BigDecimal.valueOf(1000))
                        .add(e.multiply(BigDecimal.valueOf(100)))
                        .add(m.multiply(BigDecimal.valueOf(10)))
                        .add(f.multiply(BigDecimal.valueOf(1)))
                        .add(BigDecimal.valueOf(1));
        return BigDecimal.valueOf(Math.log10(somme.doubleValue()));
    }
}
