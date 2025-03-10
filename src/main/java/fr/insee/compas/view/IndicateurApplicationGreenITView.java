package fr.insee.compas.view;

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
    private String lettreGreen;
}
