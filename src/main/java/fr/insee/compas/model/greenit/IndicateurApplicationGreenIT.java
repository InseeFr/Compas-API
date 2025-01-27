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
public class IndicateurApplicationGreenIT {
    private Integer applicationId;
    private String applicationName;
    private Integer ramAllocated;
    private BigDecimal ramMaxi;
    private Integer cpuAllocated;
    private BigDecimal cpuMaxi;
    private Integer diskAllocated;
    private BigDecimal diskUsed;
    private Integer conso;
    private Integer nbVm;
}
