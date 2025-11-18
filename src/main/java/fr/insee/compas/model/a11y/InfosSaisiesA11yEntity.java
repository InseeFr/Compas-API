package fr.insee.compas.model.a11y;

import java.time.LocalDate;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "infos_saisies_a11y")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfosSaisiesA11yEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateMajInfosSaisies;
    private boolean isDeclaration; // presence d'une declaration d'accessibilite
    private int idIndicateurTypeAudit; // nomenclature A11Y dans indicateur (51x)
    private float scoreAudit;
    private LocalDate dateAudit;
    private Integer idModule;
}
