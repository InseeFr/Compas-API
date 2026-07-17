package fr.insee.compas.view.green;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Schema(oneOf = {IndicateurAppGreenKubeView.class, IndicateurAppGreenVmView.class})
@SuperBuilder
@Getter
public class IndicateurAppGreenBaseView {
    private Integer applicationId;
    private String applicationName;
    private String domaineDev;
    private String serviceDev;
    private String domaineFonc;
    private String consoScore;
    private String impactScore;
    private String gaspillageScore;
    private String lettreGreen;
    private LocalDate dateMaj;
}
