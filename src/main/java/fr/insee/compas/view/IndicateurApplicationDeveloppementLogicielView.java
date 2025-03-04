package fr.insee.compas.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class IndicateurApplicationDeveloppementLogicielView {

    private Integer applicationId;
    private String noteDistance;
    private Integer valueDistance;
}
