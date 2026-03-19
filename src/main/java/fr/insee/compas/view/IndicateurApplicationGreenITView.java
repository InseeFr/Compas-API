package fr.insee.compas.view;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicateurApplicationGreenITView {
    private Integer applicationId;
    private String applicationName;
    private String ramAllocated;
    private String ramMaxi;
    private String diskAllocated;
    private String diskUsed;
    private String cpuAllocated;
    private String cpuMaxi;
    private String conso;
    private String nbVm;
    private String cpuUsed;
    private String ramUsed;
    private String s3Used;
    private String pvcUsed;
    private String nbPodMaxi;
    private String ramAllocatedProd;
    private String ramMaxiProd;
    private String diskAllocatedProd;
    private String diskUsedProd;
    private String cpuAllocatedProd;
    private String cpuMaxiProd;
    private String consoProd;
    private String nbVmProd;
    private String cpuUsedProd;
    private String ramUsedProd;
    private String s3UsedProd;
    private String pvcUsedProd;
    private String nbPodMaxiProd;
    private String consoScore;
    private String impactScore;
    private String gaspillageScore;
    private String lettreGreen;
    private LocalDate dateMaj;
}
