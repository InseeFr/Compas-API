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

    public ConversionService(
            @Qualifier("pourcentageEnNoteConversion")
                    PourcentageEnNoteConversion pourcentageStrategie,
            @Qualifier("cveConversion") NiveauCveConversion niveauCveStrategie,
            @Qualifier("detteTechniqueConversion") DetteTechniqueConversion detteStrategie,
            @Qualifier("issueConversion") IssueAccessibiliteConversion issueStrategie) {
        this.pourcentageConversion = pourcentageStrategie;
        this.niveauCveConversion = niveauCveStrategie;
        this.detteConversion = detteStrategie;
        this.issueConversion = issueStrategie;
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
}
