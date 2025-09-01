package fr.insee.compas.service.devops;

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

        if (nbContributorCount == -1) {
            return Notation.SO.getGrade();
        } else if (nbContributorCount == -2) {
            return Notation.NR.getGrade();
        } else if (nbContributorCount == 0) {
            return Notation.E.getGrade();
        } else if (nbContributorCount == 1) {
            return Notation.D.getGrade();
        } else if (nbContributorCount == 2) {
            return Notation.C.getGrade();
        } else if (nbContributorCount == 3) {
            return Notation.B.getGrade();
        } else if (nbContributorCount >= 4) {
            return Notation.A.getGrade();
        } else {
            return Notation.NR.getGrade();
        }
    }
}
