package fr.insee.compas.service.conversion.strategie;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.compas.Notation;

@Component("issueConversion")
public class IssueAccessibiliteConversion implements IConversionStrategie<String, String> {
    @Override
    public String conversion(String nbIssues) {
        double value = Double.parseDouble(nbIssues);
        if (value == 0) return Notation.A.getGrade();
        if (value < 100) return Notation.B.getGrade();
        if (value < 1000) return Notation.C.getGrade();
        if (value < 5000) return Notation.D.getGrade();
        return Notation.E.getGrade();
    }
}
