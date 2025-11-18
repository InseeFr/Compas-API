package fr.insee.compas.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.IndicateurSonar;
import fr.insee.compas.model.sonar.Measure;
import fr.insee.compas.model.sonar.RecuperationMeasures;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UtilsService {

    private static final Logger log = LoggerFactory.getLogger(UtilsService.class);

    public double calculPourcentageCouvertureTest(Integer ligneCode, Integer ligneCodeNonTeste) {
        return ligneCode > 0
                ? Math.round((1 - ((double) ligneCodeNonTeste / ligneCode)) * 100)
                : 0.0;
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
                            log.error("Erreur de conversion numérique pour le metric " + metric);
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

    public String extractRepoPath(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isBlank()) return null;

        final String gitlabDomain = "gitlab.insee.fr/";
        String cleanUrl = sourceUrl.replaceFirst("^https?://", "");

        int idx = cleanUrl.indexOf(gitlabDomain);
        if (idx < 0) return null;

        String repoPath =
                cleanUrl.substring(idx + gitlabDomain.length())
                        .replaceFirst("\\.git$", "")
                        .replaceFirst("/$", "");

        return repoPath.isBlank() ? null : repoPath;
    }
}
