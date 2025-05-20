package fr.insee.compas.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.IndicateurSonar;
import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.sonar.Measure;
import fr.insee.compas.model.sonar.RecuperationMeasures;

@Service
public class UtilsService {

    public double calculPourcentageCouvertureTest(Integer ligneCode, Integer ligneCodeNonTeste) {
        return ligneCode > 0
                ? Math.round((1 - ((double) ligneCodeNonTeste / ligneCode)) * 100)
                : 0.0;
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

    public String getLettreDetteTechnique(String fiabilite) {
        double value = Double.parseDouble(fiabilite);
        if (value < 2100) {
            return Notation.A.getGrade();
        } else if (value < 8400) {
            return Notation.B.getGrade();
        } else if (value < 25200) {
            return Notation.C.getGrade();
        } else if (value < 50400) {
            return Notation.D.getGrade();
        } else {
            return Notation.E.getGrade();
        }
    }

    public String convertirChiffreEnLettre(BigDecimal value) {
        return Character.toString((char) ('A' + value.intValue() - 1));
    }

    public static RecuperationMeasures concatenationMeasures(
            RecuperationMeasures measures1, RecuperationMeasures measures2) {

        if (measures1 == null
                || measures1.getComponent() == null
                || measures1.getComponent().getMeasures() == null
                || measures1.getComponent().getMeasures().isEmpty()) {
            return measures2;
        }

        Map<String, Measure> measureMap1 =
                measures1.getComponent().getMeasures().stream()
                        .collect(Collectors.toMap(Measure::getMetric, m -> m));
        if (measures2 != null
                && measures2.getComponent() != null
                && measures2.getComponent().getMeasures() != null) {
            for (Measure m2 : measures2.getComponent().getMeasures()) {
                String metric = m2.getMetric();
                Measure m1 = measureMap1.get(metric);
                if (m1 != null) {
                    if (IndicateurSonar.FIABILITE.name().equalsIgnoreCase(metric)
                            || "reliability_rating".equalsIgnoreCase(metric)) {
                        // Comparer les lettres et garder la plus grande
                        String value1 = m1.getValue();
                        String value2 = m2.getValue();
                        String max = value1.compareToIgnoreCase(value2) >= 0 ? value1 : value2;
                        m1.setValue(max);
                    } else {
                        try {
                            double sum =
                                    Double.parseDouble(m1.getValue())
                                            + Double.parseDouble(m2.getValue());
                            m1.setValue(String.valueOf(sum));
                        } catch (NumberFormatException e) {
                            // Ignorer ou logger si une valeur n'est pas un nombre
                            System.err.println(
                                    "Erreur de conversion numérique pour le metric " + metric);
                        }
                    }
                } else {
                    // Ajoute le measure s'il n'existait pas dans measures1
                    measures1
                            .getComponent()
                            .getMeasures()
                            .add(new Measure(m2.getMetric(), m2.getValue()));
                }
            }
        }

        return measures1;
    }
}
