package fr.insee.compas.service.conversion.strategie;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.compas.Notation;

@Component("cveConversion")
public class NiveauCveConversion implements IConversionStrategie<String, Double> {

    @Override
    public String conversion(Double niveauCve) {
        if (niveauCve == null) return Notation.NR.getGrade();
        double niveau = niveauCve;

        if (Double.isNaN(niveau) || Double.isInfinite(niveau)) return Notation.NR.getGrade();
        if (niveau >= 3) return Notation.E.getGrade();
        if (niveau >= 2) return Notation.D.getGrade();
        if (niveau >= 1) return Notation.C.getGrade();
        if (niveau > 0) return Notation.B.getGrade();
        return Notation.A.getGrade();
    }
}
