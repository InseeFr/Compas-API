package fr.insee.compas.view;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DevOps indicator view.
 *
 * <p>The global letter is computed from the individual letters (contributors, deployments,
 * distance) via {@link AbstractIndicateurLettreGlobale}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicateurDevopsView extends AbstractIndicateurLettreGlobale {

    // Identifiers
    private Integer moduleId;
    private Integer applicationId;

    // Labels / Names
    private String moduleName;
    private String applicationName;
    private String sndi;
    private String domaineSndi;
    private String domaineFonctionnel;

    // Raw values (counts)
    private String nbContributorCount;
    private String nbDeploymentCount;
    private String distanceCount;

    // Letters for each indicator
    private String lettreContributorCount;
    private String lettreDeploymentCount;
    private String lettreDistanceCount;

    // Global DevOps letter
    private String lettreGlobalDevops;

    /**
     * Indicates whether the indicator should be computed in synthetic mode.
     *
     * <p>In synthetic mode, the contributor letter is excluded from the global letter computation.
     */
    @JsonIgnore private boolean synthetique;

    @Override
    protected List<String> getLettresPourCalcul() {
        // Choose which letters to include depending on synthetic mode.
        Stream<String> lettersStream =
                synthetique
                        ? Stream.of(lettreDeploymentCount, lettreDistanceCount)
                        : Stream.of(
                                lettreContributorCount, lettreDeploymentCount, lettreDistanceCount);

        // Only keep non-null, non-empty trimmed values.
        return lettersStream
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    protected void setLettreGlobale(String letter) {
        this.lettreGlobalDevops = letter;
    }

    /**
     * Computes the global DevOps letter depending on the mode.
     *
     * @param synthetique if true, the contributor letter is not included.
     */
    public void calculerLettreGlobalDevops(boolean synthetique) {
        this.synthetique = synthetique;
        super.calculerLettreGlobale();
    }
}
