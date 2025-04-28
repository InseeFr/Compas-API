package fr.insee.compas.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicateurQualiteView {
    private Integer moduleId;
    private Integer applicationId;
    private String moduleName;
    private String applicationName;
    private String sndi;
    private String domaineSndi;
    private String domaineFonctionnel;
    private String nbLigneCode;
    private String nbLigneCodeNonTeste;
    private String pourcentageCouvertureTestUniaire;
    private String nbCveCritical;
    private String nbCveHigh;
    private String nbCveMedium;
    private String nbCveLow;
    private String detteTechnique;
    private String fiabilite;
    private String lettreNiveauCve;
    private String lettreDetteTechnique;
    private String lettreFiabilite;
    private String lettreCouvertureTestUniaire;

    //  Nouveau champ calculé
    private String lettreGlobalQualite;

    //  Méthode de calcul
    public void calculerLettreGlobalQualite() {
        int[] valeurs = new int[3];
        int count = 0;

        count += ajouterValeur(lettreDetteTechnique, valeurs, count);
        count += ajouterValeur(lettreFiabilite, valeurs, count);
        count += ajouterValeur(lettreCouvertureTestUniaire, valeurs, count);

        if (count == 0) {
            lettreGlobalQualite = "NR";
            return;
        }

        double sommeCarres = 0;
        for (int i = 0; i < count; i++) {
            sommeCarres += Math.pow(valeurs[i], 2);
        }

        double moyenne = Math.sqrt(sommeCarres) / Math.sqrt(count);
        int valeurArrondie = (int) Math.round(moyenne);

        lettreGlobalQualite = convertirValeurEnLettre(valeurArrondie);
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
