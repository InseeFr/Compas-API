package fr.insee.compas.model.a11y;

import java.time.LocalDate;

import fr.insee.compas.model.compas.Notation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class IndicateursModuleA11Y {

    private Integer idModule;
    private String modName;

    private Integer idApplication;
    private String nameApplication;

    /** Service dev. */
    private String sndi;

    /** Domaine dev. */
    private String domaineSndi;

    private LocalDate dateMajInfosSaisie;
    private Boolean isDeclaration; // presence d'une declaration d'accessibilite
    private int typeAuditId;
    private String typeAuditLibelle;
    private float scoreAudit;
    private LocalDate dateAudit;
    private Notation notation;
    private LocalDate dateDeclaration;

    private String nbIssueAccessibilite;
    private String lettreIssueAccessibilite;
}
