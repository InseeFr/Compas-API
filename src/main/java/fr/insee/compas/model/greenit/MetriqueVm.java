package fr.insee.compas.model.greenit;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetriqueVm {
    private String vm;
    private BigDecimal diskAllocated;
    private BigDecimal diskUsed;
    private BigDecimal applishareAllocated;
    private BigDecimal applishareUsed;
    private Integer vCpu;
    private BigDecimal cpuAllocated;
    private BigDecimal cpuMaxi;
    private BigDecimal ramAllocated;
    private BigDecimal ramMaxi;
    private BigDecimal conso;
}
