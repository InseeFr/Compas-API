package fr.insee.compas.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicateurDevopsView {

    private Integer moduleId;
    private Integer applicationId;
    private String moduleName;
    private String applicationName;
    private String sndi;
    private String domaineSndi;
    private String domaineFonctionnel;

    private String nbContributorCount;
    private String nbDeploymentCount;
    private String distanceCount;

    private String lettreContributorCount;
    private String lettreDeploymentCount;
    private String lettreDistanceCount;

    //  Nouveau champ calculé
    private String lettreGlobalDevops;

    //  Méthode de calcul
    public void calculerLettreGlobalDevops() {
        int[] valeurs = new int[3];
        int count = 0;

        count += ajouterValeur(lettreContributorCount, valeurs, count);
        count += ajouterValeur(lettreDeploymentCount, valeurs, count);
        count += ajouterValeur(lettreDistanceCount, valeurs, count);

        if (count == 0) {
            lettreGlobalDevops = "NR";
            return;
        }

        double sommeCarres = 0;
        for (int i = 0; i < count; i++) {
            sommeCarres += Math.pow(valeurs[i], 2);
        }

        double moyenne = Math.sqrt(sommeCarres) / Math.sqrt(count);
        int valeurArrondie = (int) Math.round(moyenne);

        lettreGlobalDevops = convertirValeurEnLettre(valeurArrondie);
    }

    // ✅ Méthodes utilitaires
    private int ajouterValeur(String lettre, int[] valeurs, int index) {
        if (lettre == null) {
            return 0;
        }
        int valeur = convertirLettreEnValeur(lettre);
        if (valeur != -1) {
            valeurs[index] = valeur;
            return 1;
        }
        return 0;
    }

    private int convertirLettreEnValeur(String lettre) {
        lettre = lettre.toUpperCase();
        return switch (lettre) {
            case "A" -> 1;
            case "B" -> 2;
            case "C" -> 3;
            case "D" -> 4;
            case "E", "X" -> 5;
            case "SO" -> 0;
            default -> -1; // Invalid
        };
    }

    private String convertirValeurEnLettre(int valeur) {
        return switch (valeur) {
            case 0 -> "SO";
            case 1 -> "A";
            case 2 -> "B";
            case 3 -> "C";
            case 4 -> "D";
            case 5 -> "E";
            default -> "X";
        };
    }
}
