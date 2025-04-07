package fr.insee.compas.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class IndicateurModuleDeveloppementLogicielView {
    private Integer moduleId;
    private String note;
    private Integer value;
}
