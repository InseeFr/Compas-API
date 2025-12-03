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
public class IndicateurDevopsView extends AbstractIndicateurLettreGlobale {

    private Integer moduleId;
    private Integer applicationId;
    private String moduleName;
    private String applicationName;
    private String sndi;
    private String domaineSndi;
    private String domaineFonctionnel;

    private String nbContributorCount;
    private String nbDeploymentCount;
    private String distanceCount;

    private String lettreContributorCount;
    private String lettreDeploymentCount;
    private String lettreDistanceCount;
    //  Nouveau champ calculé
    private String lettreGlobalDevops;

    @Override
    protected List<String> getLettresPourCalcul() {
        return Stream.of(lettreContributorCount, lettreDeploymentCount, lettreDistanceCount)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    protected void setLettreGlobale(String lettre) {}

    public void calculerLettreGlobalDevops() {
        super.calculerLettreGlobale();
    }
}
