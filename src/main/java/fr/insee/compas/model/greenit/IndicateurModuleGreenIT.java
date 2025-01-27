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
public class IndicateurModuleGreenIT {
    private Integer moduleId;
    private String moduleName;
    private BigDecimal ramMaxi;
    private Integer ramAllocated;
    private BigDecimal cpuMaxi;
    private Integer cpuAllocated;
    private BigDecimal diskUsed;
    private Integer diskAllocated;
    private Integer conso;
    private Integer nbVm;
}
