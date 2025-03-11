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

    /** Service dev. */
    private String sndi;

    /** Domaine dev. */
    private String domaineSndi;

    private LocalDate dateMajInfosSaisie;
    private boolean isDeclaration; // presence d'une declaration d'accessibilite
    private int TypeAuditId;
    private String TypeAuditLibelle;
    private float ScoreAudit;
    private LocalDate DateAudit;
    private Notation notation;
}
