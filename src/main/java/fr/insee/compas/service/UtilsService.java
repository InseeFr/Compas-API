package fr.insee.compas.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.sonar.RecuperationMeasures;

@Service
public class UtilsService {

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

        return RecuperationMeasures.concatenationMeasures(measures1, measures2);
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
