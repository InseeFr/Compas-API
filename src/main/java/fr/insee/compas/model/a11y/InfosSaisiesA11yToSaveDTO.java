package fr.insee.compas.model.a11y;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfosSaisiesA11yToSaveDTO {

    private LocalDate dateMajInfosSaisies;
    private boolean isDeclaration; // presence d'une declaration d'accessibilite
    private int idIndicateurTypeAudit; // nomenclature A11Y dans indicateur (51x)
    private float scoreAudit;
    private LocalDate dateAudit;
    private LocalDate dateDeclaration;

    private Integer idModule;
}
