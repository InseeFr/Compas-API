package fr.insee.compas.service.conversion.strategie;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.compas.Notation;

@Component("pourcentageEnNoteConversion")
public class PourcentageEnNoteConversion implements IConversionStrategie<String, Double> {

    @Override
    public String conversion(Double pourcentage) {
        if (pourcentage == null) return Notation.NR.getGrade();
        double value = pourcentage;

        if (value == 0) return Notation.X.getGrade();
        if (value < 0 || Double.isNaN(value)) return Notation.NR.getGrade();
        if (value > 80) return Notation.A.getGrade();
        if (value > 60) return Notation.B.getGrade();
        if (value > 40) return Notation.C.getGrade();
        if (value > 20) return Notation.D.getGrade();
        return Notation.E.getGrade();
    }
    ;
}
