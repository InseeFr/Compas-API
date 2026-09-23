package fr.insee.compas.model.analyzer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComptageCve {
    private Integer nombreCveCritique;
    private Integer nombreCveMajeur;
    private Integer nombreCveMoyenne;
    private Integer nombreCveFaible;
}
