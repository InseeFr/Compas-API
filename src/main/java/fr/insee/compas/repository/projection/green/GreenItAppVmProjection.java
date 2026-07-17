package fr.insee.compas.repository.projection.green;

import java.math.BigDecimal;

public interface GreenItAppVmProjection extends GreenItAppBaseProjection {

    BigDecimal getRamAlloue();

    BigDecimal getRamMaxi();

    BigDecimal getDisqueConsomme();

    BigDecimal getCpuAllouee();

    BigDecimal getCpuMaxi();

    BigDecimal getConsoElec();

    BigDecimal getNbrVM();

    BigDecimal getRamAlloueePd();

    BigDecimal getRamMaxiPd();

    BigDecimal getDisqueAllouePd();

    BigDecimal getDisqueAlloue();

    BigDecimal getDisqueConsommePd();

    BigDecimal getCpuAlloueePd();

    BigDecimal getCpuMaxiPd();

    BigDecimal getConsoElecPd();

    BigDecimal getNbrVmPd();

    BigDecimal getAsConsomme();

    BigDecimal getAsConsommePd();

    BigDecimal getAsAlloue();

    BigDecimal getAsAllouePd();
}
