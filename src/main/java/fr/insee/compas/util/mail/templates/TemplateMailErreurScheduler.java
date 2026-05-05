package fr.insee.compas.util.mail.templates;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TemplateMailErreurScheduler {
    private static final String BR = "<br/>";

    public String getSubjectTemplateMail() {
        return "[Compas] Erreur lors de l'exécution du scheduler";
    }

    public String getBodyTemplateMail(List<String> authors, List<String> cc, boolean test) {
        StringBuilder sb = new StringBuilder();
        if (test) {
            sb.append("⚠️ MODE TEST — Ce mail est destiné à ")
                    .append(String.join(",", authors))
                    .append(BR);
            sb.append("Avec en cc: ").append(String.join(",", cc)).append(BR);
        }
        sb.append(
                        "Une ou plusieurs erreurs ont été retrouvées durant le job du scheduler."
                                + "Veuillez les corriger le plus rapidement possible !")
                .append(BR);
        sb.append("Vous trouverez en pièce jointe le fichier contenant les erreurs.")
                .append(BR)
                .append(BR);
        sb.append("Cordialement,").append(BR);
        sb.append("L'équipe Compas");
        return sb.toString();
    }
}
