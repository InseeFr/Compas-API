package fr.insee.compas.service.devops;

import static fr.insee.compas.util.DevopsConstantes.DUPLICATE_OFFSET;

import fr.insee.compas.model.compas.Notation;

import lombok.experimental.UtilityClass;

@UtilityClass
public class IndicateurDevopsLetterUtils {

    public static String calculLettreDistanceCount(String countStr) {
        if (countStr == null) {
            return Notation.NR.getGrade();
        }

        int distanceCount;
        try {
            distanceCount = (int) Double.parseDouble(countStr);
        } catch (NumberFormatException e) {
            return Notation.NR.getGrade();
        }

        if (distanceCount == -1) {
            return Notation.SO.getGrade();
        } else if (distanceCount == -2) {
            return Notation.NR.getGrade();
        } else if (distanceCount >= 0) {
            if (distanceCount <= 30) {
                return Notation.A.getGrade();
            } else if (distanceCount <= 90) {
                return Notation.B.getGrade();
            } else if (distanceCount <= 180) {
                return Notation.C.getGrade();
            } else if (distanceCount <= 360) {
                return Notation.D.getGrade();
            } else {
                return Notation.E.getGrade();
            }
        } else {
            return Notation.NR.getGrade();
        }
    }

    public static String calculLettreDeploymentCount(String countStr) {
        // Cas null ou vide
        if (countStr == null || countStr.trim().isEmpty()) {
            return Notation.NR.getGrade();
        }

        int count;
        try {
            count = (int) Double.parseDouble(countStr.trim());
        } catch (NumberFormatException e) {
            return Notation.NR.getGrade();
        }

        // Cas spéciaux
        if (count == -1) {
            return Notation.SO.getGrade(); // Système out
        }
        if (count == -2) {
            return Notation.NR.getGrade(); // Non renseigné
        }

        // Cas normaux
        if (count >= 4) {
            return Notation.A.getGrade();
        }

        return switch (count) {
            case 3 -> Notation.B.getGrade();
            case 2 -> Notation.C.getGrade();
            case 1 -> Notation.D.getGrade();
            case 0 -> Notation.E.getGrade();
            default -> Notation.NR.getGrade(); // Cas négatif non géré spécifiquement
        };
    }

    public static String calculLettreContributorCount(String countStr) {
        if (countStr == null) {
            return Notation.NR.getGrade();
        }

        int nbContributorCount;
        try {
            nbContributorCount = (int) Double.parseDouble(countStr);
        } catch (NumberFormatException e) {
            return Notation.NR.getGrade();
        }

        boolean isDuplicate = false;

        if (nbContributorCount >= DUPLICATE_OFFSET) {
            isDuplicate = true;
            nbContributorCount -= DUPLICATE_OFFSET;
        }

        String grade = Notation.NR.getGrade();

        if (nbContributorCount == -1) grade = Notation.SO.getGrade();
        if (nbContributorCount == -2) grade = Notation.NR.getGrade();
        if (nbContributorCount == 0) grade = Notation.E.getGrade();
        if (nbContributorCount == 1) grade = Notation.D.getGrade();
        if (nbContributorCount == 2) grade = Notation.C.getGrade();
        if (nbContributorCount == 3) grade = Notation.B.getGrade();
        if (nbContributorCount >= 4) grade = Notation.A.getGrade();

        return isDuplicate ? grade + " d" : grade;
    }
}
