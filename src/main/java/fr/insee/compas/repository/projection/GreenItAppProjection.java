package fr.insee.compas.repository.projection;

import java.math.BigDecimal;

public interface GreenItAppProjection {
    Integer getIdApplication();

    BigDecimal getRamAlloue();

    BigDecimal getRamMaxi();

    BigDecimal getDisqueConsomme();

    BigDecimal getCpuAllouee();

    BigDecimal getCpuMaxi();

    BigDecimal getConsoElec();

    BigDecimal getNbrVM();

    Long getRamConsommee();

    BigDecimal getCpuConsomme();

    BigDecimal getRamAlloueePd();

    BigDecimal getRamMaxiPd();

    BigDecimal getDisqueAllouePd();

    BigDecimal getDisqueAlloue();

    BigDecimal getDisqueConsommePd();

    BigDecimal getCpuAlloueePd();

    BigDecimal getCpuMaxiPd();

    BigDecimal getConsoElecPd();

    BigDecimal getNbrVmPd();

    Long getRamConsommeePd();

    BigDecimal getCpuConsommeePd();

    Long getS3Consomme();

    Long getPvcConsomme();

    BigDecimal getNbPodMaxi();

    Long getS3ConsommePd();

    Long getPvcConsommePd();

    BigDecimal getNbPodMaxiPd();

    BigDecimal getAsConsomme();

    BigDecimal getAsConsommePd();

    BigDecimal getAsAlloue();

    BigDecimal getAsAllouePd();
}
