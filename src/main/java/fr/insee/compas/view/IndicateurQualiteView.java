package fr.insee.compas.view;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicateurQualiteView extends AbstractIndicateurLettreGlobale {
    private Integer moduleId;
    private Integer applicationId;
    private String moduleName;
    private String applicationName;
    private String sndi;
    private String domaineSndi;
    private String domaineFonctionnel;
    private String nbLigneCode;
    private String nbLigneCodeNonTeste;

    private String detteTechnique;
    private String pourcentageCouvertureTestUnitaire;
    private String fiabilite;

    private String detteTechniquePast;
    private String pourcentageCouvertureTestUnitairePast;
    private String fiabilitePast;

    private String lettreDetteTechnique;
    private String lettreFiabilite;

    private String lettreCouvertureTestUnitaire;

    private String lettreGlobalQualite;

    private double evolutionCouvertureTestUnitaire;
    private double evolutionDetteTechnique;
    private double evolutionFiabilite;

    @Override
    protected List<String> getLettresPourCalcul() {
        return Stream.of(lettreCouvertureTestUnitaire, lettreFiabilite, lettreDetteTechnique)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    protected void setLettreGlobale(String lettre) {
        this.lettreGlobalQualite = lettre;
    }

    public void calculerLettreGlobalQualiteEtEvolution() {
        super.calculerLettreGlobale();
    }
}
