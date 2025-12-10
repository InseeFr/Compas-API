package fr.insee.compas.service.conversion.strategie;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.compas.Notation;

@Component("detteTechniqueConversion")
public class DetteTechniqueConversion implements IConversionStrategie<String, String> {
    @Override
    public String conversion(String fiabilite) {
        double value = Double.parseDouble(fiabilite);
        if (value < 2100) return Notation.A.getGrade();
        if (value < 8400) return Notation.B.getGrade();
        if (value < 25200) return Notation.C.getGrade();
        if (value < 50400) return Notation.D.getGrade();
        return Notation.E.getGrade();
    }
}
