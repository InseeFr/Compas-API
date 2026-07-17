package fr.insee.compas.view.green;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class IndicateurAppGreenVmView extends IndicateurAppGreenBaseView {
    private String ramAllocated;
    private String ramAllocatedHist;
    private String ramAllocatedHp;
    private String ramAllocatedHistHp;
    private String ramMaxi;
    private String ramMaxiHist;
    private String ramMaxiHp;
    private String ramMaxiHistHp;
    private String diskAllocated;
    private String diskAllocatedHist;
    private String diskAllocatedHp;
    private String diskAllocatedHistHp;
    private String diskUsed;
    private String diskUsedHist;
    private String diskUsedHp;
    private String diskUsedHistHp;
    private String cpuAllocated;
    private String cpuAllocatedHist;
    private String cpuAllocatedHp;
    private String cpuAllocatedHistHp;
    private String cpuMaxi;
    private String cpuMaxiHist;
    private String cpuMaxiHp;
    private String cpuMaxiHistHp;
    private String conso;
    private String consoHist;
    private String consoHp;
    private String consoHistHp;
    private String asUsed;
    private String asUsedHist;
    private String asUsedHistHp;
    private String asUsedHp;
    private String asAllocated;
    private String asAllocatedHist;
    private String asAllocatedHp;
    private String asAllocatedHistHp;
    private String nbVm;
    private String nbVmHist;
    private String nbVmHp;
    private String nbVmHistHp;
    private String ramAllocatedProd;
    private String ramAllocatedHistProd;
    private String ramMaxiProd;
    private String ramMaxiHistProd;
    private String diskAllocatedProd;
    private String diskAllocatedHistProd;
    private String diskUsedProd;
    private String diskUsedHistProd;
    private String cpuAllocatedProd;
    private String cpuAllocatedHistProd;
    private String cpuMaxiProd;
    private String cpuMaxiHistProd;
    private String consoProd;
    private String consoHistProd;
    private String asUsedProd;
    private String asUsedHistProd;
    private String asAllocatedProd;
    private String asAllocatedHistProd;
    private String nbVmProd;
    private String nbVmHistProd;
}
