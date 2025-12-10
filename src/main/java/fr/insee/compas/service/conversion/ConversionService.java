package fr.insee.compas.service.conversion;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.insee.compas.service.conversion.strategie.*;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConversionService {
    private final IConversionStrategie<String, Double> pourcentageConversion;
    private final IConversionStrategie<String, Double> niveauCveConversion;
    private final IConversionStrategie<String, String> detteConversion;
    private final IConversionStrategie<String, String> issueConversion;
    private final IConversionStrategie<String, Double> vmConversion;

    public ConversionService(
            @Qualifier("pourcentageEnNoteConversion")
                    IConversionStrategie<String, Double> pourcentageConversion,
            @Qualifier("cveConversion") IConversionStrategie<String, Double> niveauCveConversion,
            @Qualifier("detteTechniqueConversion")
                    IConversionStrategie<String, String> detteConversion,
            @Qualifier("issueConversion") IConversionStrategie<String, String> issueConversion,
            @Qualifier("vmConversion") IConversionStrategie<String, Double> vmConversion) {
        this.pourcentageConversion = pourcentageConversion;
        this.niveauCveConversion = niveauCveConversion;
        this.detteConversion = detteConversion;
        this.issueConversion = issueConversion;
        this.vmConversion = vmConversion;
    }

    public String convertPourcentageEnNote(double percentage) {
        return pourcentageConversion.conversion(percentage);
    }

    public String convertNiveauCveEnLettre(double niveau) {
        return niveauCveConversion.conversion(niveau);
    }

    public String convertDetteTechnique(String fiabilite) {
        return detteConversion.conversion(fiabilite);
    }

    public String convertIssueAccessebilite(String nbIssues) {
        return issueConversion.conversion(nbIssues);
    }

    public String convertNbVmNonMiseAJour(double niveau) {
        return vmConversion.conversion(niveau);
    }
}
