package fr.insee.compas.dto.green;

import static fr.insee.compas.util.greenit.GreenITutils.subtractSafe;

import java.math.BigDecimal;
import java.time.LocalDate;

import fr.insee.compas.model.greenit.GreenItScore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class GreenVmDto extends GreenItBaseDto {
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
    private LocalDate dateMaj;

    public BigDecimal getRamAllocatedHp() {
        return subtractSafe(ramAllocated, ramAllocatedProd);
    }

    public BigDecimal getRamAllocatedHpHist() {
        return subtractSafe(ramAllocatedHist, ramAllocatedHistProd);
    }

    public BigDecimal getRamMaxiHp() {
        return subtractSafe(ramMaxi, ramMaxiProd);
    }

    public BigDecimal getRamMaxiHpHist() {
        return subtractSafe(ramMaxiHist, ramMaxiHistProd);
    }

    public BigDecimal getDiskAllocatedHp() {
        return subtractSafe(diskAllocated, diskAllocatedProd);
    }

    public BigDecimal getDiskAllocatedHpHist() {
        return subtractSafe(diskAllocatedHist, diskAllocatedHistProd);
    }

    public BigDecimal getDiskUsedHp() {
        return subtractSafe(diskUsed, diskUsedProd);
    }

    public BigDecimal getDiskUsedHpHist() {
        return subtractSafe(diskUsedHist, diskUsedHistProd);
    }

    public BigDecimal getCpuAllocatedHp() {
        return subtractSafe(cpuAllocated, cpuAllocatedProd);
    }

    public BigDecimal getCpuAllocatedHpHist() {
        return subtractSafe(cpuAllocatedHist, cpuAllocatedHistProd);
    }

    public BigDecimal getCpuMaxiHp() {
        return subtractSafe(cpuMaxi, cpuMaxiProd);
    }

    public BigDecimal getCpuMaxiHpHist() {
        return subtractSafe(cpuMaxiHist, cpuMaxiHistProd);
    }

    public BigDecimal getConsoHp() {
        return subtractSafe(conso, consoProd);
    }

    public BigDecimal getConsoHpHist() {
        return subtractSafe(consoHist, consoHistProd);
    }

    public BigDecimal getAsUsedHp() {
        return subtractSafe(asUsed, asUsedProd);
    }

    public BigDecimal getAsUsedHpHist() {
        return subtractSafe(asUsedHist, asUsedHistProd);
    }

    public BigDecimal getAsAllocatedHp() {
        return subtractSafe(asAllocated, asAllocatedProd);
    }

    public BigDecimal getAsAllocatedHpHist() {
        return subtractSafe(asAllocatedHist, asAllocatedHistProd);
    }

    public BigDecimal getNbVmHp() {
        return subtractSafe(nbVm, nbVmProd);
    }

    public BigDecimal getNbVmHpHist() {
        return subtractSafe(nbVmHist, nbVmHistProd);
    }

    public GreenItScore toGreenItScoreVm(
            BigDecimal score,
            String grade,
            BigDecimal conso,
            BigDecimal pression,
            BigDecimal gaspillage) {
        return toGreenItScore(score, grade, conso, pression, gaspillage);
    }
}
