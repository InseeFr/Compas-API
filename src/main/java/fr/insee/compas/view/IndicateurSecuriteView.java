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
public class IndicateurSecuriteView extends AbstractIndicateurLettreGlobale {
    private Integer applicationId;
    private Integer moduleId;
    private String nbCveCritical;
    private String nbCveHigh;
    private String nbCveMedium;
    private String nbCveLow;
    private String nbVmNonMaj;
    private String lettreCve;
    private String delaiVmNonMiseAjour;
    private String lettreMajVm;
    private String lettreGlobaleSecurite;

    @Override
    protected List<String> getLettresPourCalcul() {

        return Stream.of(lettreCve, lettreMajVm).filter(Objects::nonNull).toList();
    }

    @Override
    protected void setLettreGlobale(String lettre) {
        this.lettreGlobaleSecurite = lettre;
    }

    public void calculerLettreGlobaleSecurite() {
        super.calculerLettreGlobale();
    }
}
