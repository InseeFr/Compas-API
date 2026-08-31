package fr.insee.compas.repository.projection.maturite;

import java.math.BigDecimal;

public interface MaturiteCloudProjection {
    Integer getIdApplication();

    String getMaturite();

    BigDecimal getScoreMaturite();

    BigDecimal getDeltaMaturite();

    BigDecimal getDeltaOpportunite();

    BigDecimal getRobustesse();

    BigDecimal getScoreBenefice();

    BigDecimal getScoreOrga();

    BigDecimal getScoreComplexite();

    BigDecimal getScoreTechnique();

    BigDecimal getScoreOpportunite();

    BigDecimal getProgressionDeploy();

    BigDecimal getProgressionTechnos();

    BigDecimal getProgressionArchi();

    BigDecimal getProgressionMateqip();

    BigDecimal getProgressionDevops();

    BigDecimal getProgressionCloud();
}
