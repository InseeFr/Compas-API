package fr.insee.compas.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicateurModuleQualiteView {
    private Integer moduleId;
    private String moduleName;
    private String applicationName;
    private String sndi;
    private String domaineSndi;
    private String domaineFonctionnel;
    private String nbLigneCode;
    private String nbLigneCodeNonTeste;
    private String pourcentageCouvertureTestUniaire;
    private String nbCveCritical;
    private String nbCveHigh;
    private String nbCveMedium;
    private String nbCveLow;
    private String detteTechnique;
    private String fiabilite;
    private String lettreNiveauCve;
    private String lettreDetteTechnique;
    private String lettreFiabilite;
    private String lettreCouvertureTestUniaire;
}
