package fr.insee.compas.view.green;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class IndicateurAppGreenKubeView extends IndicateurAppGreenBaseView {
    private String cpuUsed;
    private String cpuUsedHist;
    private String ramUsed;
    private String ramUsedHist;
    private String s3Used;
    private String s3UsedHist;
    private String pvcUsed;
    private String pvcUsedHist;
    private String nbPodMaxi;
    private String nbPodMaxiHist;
    private String cpuUsedProd;
    private String cpuUsedHistProd;
    private String ramUsedProd;
    private String ramUsedHistProd;
    private String s3UsedProd;
    private String s3UsedHistProd;
    private String pvcUsedProd;
    private String pvcUsedHistProd;
    private String nbPodMaxiProd;
    private String nbPodMaxiHistProd;
    private String cpuUsedHp;
    private String cpuUsedHistHp;
    private String ramUsedHp;
    private String ramUsedHistHp;
    private String s3UsedHp;
    private String s3UsedHistHp;
    private String pvcUsedHp;
    private String pvcUsedHistHp;
    private String nbPodMaxiHp;
    private String nbPodMaxiHistHp;
}
