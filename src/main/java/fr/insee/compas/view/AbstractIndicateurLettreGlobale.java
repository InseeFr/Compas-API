package fr.insee.compas.view;

import java.util.List;

public abstract class AbstractIndicateurLettreGlobale {

    protected abstract List<String> getLettresPourCalcul();

    protected abstract void setLettreGlobale(String lettre);

    public void calculerLettreGlobale() {
        List<String> lettres = getLettresPourCalcul();

        if (lettres == null || lettres.isEmpty()) {
            setLettreGlobale("NR");
            return;
        }

        int[] valeurs = new int[lettres.size()];
        int count = 0;

        for (String lettre : lettres) {
            int valeur = convertirLettreEnValeur(lettre);
            if (valeur != -1) {
                valeurs[count++] = valeur;
            }
        }

        if (count == 0) {
            setLettreGlobale("NR");
            return;
        }

        double sommeCarres = 0;
        for (int i = 0; i < count; i++) {
            sommeCarres += Math.pow(valeurs[i], 2);
        }

        double moyenne = Math.sqrt(sommeCarres) / Math.sqrt(count);
        int valeurArrondie = (int) Math.round(moyenne);

        setLettreGlobale(convertirValeurEnLettre(valeurArrondie));
    }

    // 🔧 Méthodes utilitaires communes
    protected int convertirLettreEnValeur(String lettre) {
        if (lettre == null) return -1;
        return switch (lettre.toUpperCase()) {
            case "A" -> 1;
            case "B" -> 2;
            case "C" -> 3;
            case "D" -> 4;
            case "E", "X" -> 5;
            case "SO" -> 0;
            default -> -1;
        };
    }

    protected String convertirValeurEnLettre(int valeur) {
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
