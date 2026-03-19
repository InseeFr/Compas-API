package fr.insee.compas.model.greenit;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class IndicateurGreenIT {
    private Integer ramAllocated;
    private BigDecimal ramMaxi;
    private Integer cpuAllocated;
    private BigDecimal cpuMaxi;
    private Integer diskAllocated;
    private BigDecimal diskUsed;
    private Integer conso;
    private Integer nbVm;
    private Integer cpuUsed;
    private Long ramUsed;
    private Integer s3Used;
    private Integer pvcUsed;
    private Integer nbPodMaxi;
    private Integer ramAllocatedProd;
    private BigDecimal ramMaxiProd;
    private Integer cpuAllocatedProd;
    private BigDecimal cpuMaxiProd;
    private Integer diskAllocatedProd;
    private BigDecimal diskUsedProd;
    private Integer consoProd;
    private Integer nbVmProd;
    private Integer cpuUsedProd;
    private Long ramUsedProd;
    private Integer s3UsedProd;
    private Integer pvcUsedProd;
    private Integer nbPodMaxiProd;
    private LocalDate dateMaj;

    public abstract GreenItScore toGreenItScore(
            BigDecimal score,
            String grade,
            BigDecimal conso,
            BigDecimal pression,
            BigDecimal gaspillage);
}
