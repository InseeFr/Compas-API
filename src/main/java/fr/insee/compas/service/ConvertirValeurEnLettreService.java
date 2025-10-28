package fr.insee.compas.service;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.Notation;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConvertirValeurEnLettreService {

    public String convertPourcentageEnNote(double percentage) {
        if (percentage > 80) {
            return Notation.A.getGrade();
        } else if (percentage > 60) {
            return Notation.B.getGrade();
        } else if (percentage > 40) {
            return Notation.C.getGrade();
        } else if (percentage > 20) {
            return Notation.D.getGrade();
        } else if (percentage > 0) {
            return Notation.E.getGrade();
        } else if (percentage == 0) {
            return Notation.X.getGrade();
        } else {
            return "NR";
        }
    }

    public String convertNiveauCveEnLettre(double niveau) {
        if (niveau >= 3) {
            return Notation.E.getGrade();
        } else if (niveau >= 2) {
            return Notation.D.getGrade();
        } else if (niveau >= 1) {
            return Notation.C.getGrade();
        } else if (niveau > 0) {
            return Notation.B.getGrade();
        } else {
            return Notation.A.getGrade();
        }
    }

    public String getLettreDetteTechnique(String fiabilite) {
        double value = Double.parseDouble(fiabilite);
        if (value < 2100) {
            return Notation.A.getGrade();
        } else if (value < 8400) {
            return Notation.B.getGrade();
        } else if (value < 25200) {
            return Notation.C.getGrade();
        } else if (value < 50400) {
            return Notation.D.getGrade();
        } else {
            return Notation.E.getGrade();
        }
    }

    public String getLettreIssueAccessebilite(String nbIssues) {
        double value = Double.parseDouble(nbIssues);
        if (value == 0) {
            return Notation.A.getGrade();
        } else if (value < 100) {
            return Notation.B.getGrade();
        } else if (value < 1000) {
            return Notation.C.getGrade();
        } else if (value < 5000) {
            return Notation.D.getGrade();
        } else {
            return Notation.E.getGrade();
        }
    }
}
