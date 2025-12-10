package fr.insee.compas.service.conversion.strategie;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.compas.Notation;

@Component("vmConversion")
public class VMConversion implements IConversionStrategie<String, Double> {
    @Override
    public String conversion(Double label) {
        if (label >= 20) return Notation.E.getGrade();
        else if (label >= 12) return Notation.D.getGrade();
        else if (label >= 5) return Notation.C.getGrade();
        else if (label > 0) return Notation.B.getGrade();
        else return Notation.A.getGrade();
    }
}
