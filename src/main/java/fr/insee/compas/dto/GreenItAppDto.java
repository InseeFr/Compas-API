package fr.insee.compas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import fr.insee.compas.model.greenit.GreenItScore;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GreenItAppDto {
    private Integer applicationId;
    private String applicationName;
    private String domaineDev;
    private String serviceDev;
    private String domaineFonc;
    private BigDecimal ramAllocated;
    private BigDecimal ramAllocatedHist;
    private BigDecimal ramMaxi;
    private BigDecimal ramMaxiHist;
    private BigDecimal diskAllocated;
    private BigDecimal diskAllocatedHist;
    private BigDecimal diskUsed;
    private BigDecimal diskUsedHist;
    private BigDecimal cpuAllocated;
    private BigDecimal cpuAllocatedHist;
    private BigDecimal cpuMaxi;
    private BigDecimal cpuMaxiHist;
    private BigDecimal conso;
    private BigDecimal consoHist;
    private BigDecimal asUsed;
    private BigDecimal asUsedHist;
    private BigDecimal asAllocated;
    private BigDecimal asAllocatedHist;
    private BigDecimal nbVm;
    private BigDecimal nbVmHist;
    private BigDecimal cpuUsed;
    private BigDecimal cpuUsedHist;
    private Long ramUsed;
    private Long ramUsedHist;
    private Long s3Used;
    private Long s3UsedHist;
    private Long pvcUsed;
    private Long pvcUsedHist;
    private BigDecimal nbPodMaxi;
    private BigDecimal nbPodMaxiHist;
    private BigDecimal ramAllocatedProd;
    private BigDecimal ramAllocatedHistProd;
    private BigDecimal ramMaxiProd;
    private BigDecimal ramMaxiHistProd;
    private BigDecimal diskAllocatedProd;
    private BigDecimal diskAllocatedHistProd;
    private BigDecimal diskUsedProd;
    private BigDecimal diskUsedHistProd;
    private BigDecimal cpuAllocatedProd;
    private BigDecimal cpuAllocatedHistProd;
    private BigDecimal cpuMaxiProd;
    private BigDecimal cpuMaxiHistProd;
    private BigDecimal consoProd;
    private BigDecimal consoHistProd;
    private BigDecimal asUsedProd;
    private BigDecimal asUsedHistProd;
    private BigDecimal asAllocatedProd;
    private BigDecimal asAllocatedHistProd;
    private BigDecimal nbVmProd;
    private BigDecimal nbVmHistProd;
    private BigDecimal cpuUsedProd;
    private BigDecimal cpuUsedHistProd;
    private Long ramUsedProd;
    private Long ramUsedHistProd;
    private Long s3UsedProd;
    private Long s3UsedHistProd;
    private Long pvcUsedProd;
    private Long pvcUsedHistProd;
    private BigDecimal nbPodMaxiProd;
    private BigDecimal nbPodMaxiHistProd;
    private BigDecimal consoScore;
    private BigDecimal consoHistScore;
    private LocalDate dateMaj;

    public GreenItScore toGreenItScore(
            BigDecimal score,
            String grade,
            BigDecimal conso,
            BigDecimal pression,
            BigDecimal gaspillage) {
        return new GreenItScore(applicationId, null, score, grade, conso, pression, gaspillage);
    }
}
