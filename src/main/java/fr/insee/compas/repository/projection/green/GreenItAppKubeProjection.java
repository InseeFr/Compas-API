package fr.insee.compas.repository.projection.green;

import java.math.BigDecimal;

public interface GreenItAppKubeProjection extends GreenItAppBaseProjection {
    Long getRamConsommee();

    Long getRamConsommeePd();

    Long getS3Consomme();

    Long getPvcConsomme();

    BigDecimal getNbPodMaxi();

    BigDecimal getNbPodMaxiPd();

    Long getS3ConsommePd();

    Long getPvcConsommePd();

    BigDecimal getCpuConsomme();

    BigDecimal getCpuConsommeePd();
}
