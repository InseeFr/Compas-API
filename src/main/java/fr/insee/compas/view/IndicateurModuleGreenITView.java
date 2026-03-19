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
public class IndicateurModuleGreenITView {
    private Integer moduleId;
    private String moduleName;
    private String ramAllocated;
    private String diskUsed;
    private String diskAllocated;
    private String cpuAllocated;
    private String conso;
    private String nbVm;
    private String consoScore;
    private String impactScore;
    private String gaspillageScore;
    private String lettreGreen;
    private LocalDate dateMaj;
}
