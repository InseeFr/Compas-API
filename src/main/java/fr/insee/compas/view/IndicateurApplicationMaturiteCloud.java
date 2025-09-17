package fr.insee.compas.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicateurApplicationMaturiteCloud {
    private Integer applicationId;
    private String maturite;
    private String robustesse;
    private String scoreBenefice;
    private String scoreOrga;
    private String scoreComplexite;
    private String scoreTechnique;
    private String progressionDeploy;
    private String progressionTechnos;
    private String progressionArchi;
    private String progressionMateqip;
    private String progressionDevops;
    private String progressionCloud;
}
