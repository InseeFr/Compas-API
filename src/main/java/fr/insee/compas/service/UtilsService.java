package fr.insee.compas.service;

import org.springframework.stereotype.Service;

@Service
public class UtilsService {

    public double calculPourcentageCouvertureTest(Integer ligneCode, Integer ligneCodeNonTeste) {
        return ligneCode > 0 ? (1 - ((double) ligneCodeNonTeste / ligneCode)) * 100 : 0.0;
    }

    public String convertPourcentageEnNote(double percentage) {
        if (percentage > 80) {
            return "A";
        } else if (percentage > 60) {
            return "B";
        } else if (percentage > 40) {
            return "C";
        } else if (percentage > 20) {
            return "D";
        } else if (percentage > 0) {
            return "E";
        } else if (percentage == 0) {
            return "X";
        } else {
            return "NR";
        }
    }
}
