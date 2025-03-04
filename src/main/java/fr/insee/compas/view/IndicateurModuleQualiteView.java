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
    private String domaine;
    private String lettreCouvertureTestUniaire;
    private String pourcentageCouvertureTestUniaire;
    private String nbCveCritical;
    private String nbCveHigh;
    private String nbCveMedium;
    private String nbCveLow;
    private String lettreNiveauCve;
}
