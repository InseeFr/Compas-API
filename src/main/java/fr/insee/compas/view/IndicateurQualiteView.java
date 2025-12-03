package fr.insee.compas.view;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String pourcentageCouvertureTestUniaire;
    private String detteTechnique;
    private String fiabilite;
    private String lettreDetteTechnique;
    private String lettreFiabilite;
    private String lettreCouvertureTestUniaire;

    private String lettreGlobalQualite;

    @Override
    protected List<String> getLettresPourCalcul() {
        return Stream.of(lettreCouvertureTestUniaire, lettreFiabilite, lettreDetteTechnique)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    protected void setLettreGlobale(String lettre) {
        this.lettreGlobalQualite = lettre;
    }

    public void calculerLettreGlobalQualite() {
        super.calculerLettreGlobale();
    }
}
