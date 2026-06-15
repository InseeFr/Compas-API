package fr.insee.compas.util.mail.templates;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.meteo.Meteo;
import fr.insee.compas.util.MeteoAlerteUtils;
import fr.insee.compas.util.MeteoAlerteUtils.AlerteType;

@Component
public class TemplateAlerteMeteo {

    private static final ZoneId TZ_PARIS = ZoneId.of("Europe/Paris");
    private static final String BR = "<br/>";

    public String getSubjectTemplate(List<Meteo> apps, AlerteType type) {
        int n = apps.size();
        String prefix =
                (type == AlerteType.RETARD)
                        ? "[COMPAS] Météo en retard — "
                        : "[COMPAS] Rappel météo — ";
        return prefix + n + " application" + (n > 1 ? "s" : "");
    }

    public String getTemplateBody(MeteoAlerteUtils.AlerteMailContext ctx) {
        LocalDate today = LocalDate.now(TZ_PARIS);
        StringBuilder sb = new StringBuilder();

        if (ctx.test()) {
            sb.append("⚠️ MODE TEST — Ce mail est destiné à '")
                    .append(MeteoAlerteUtils.escape(ctx.rgaEmail()))
                    .append("'")
                    .append(BR);
            if (ctx.emailResponsable() != null && !ctx.emailResponsable().isBlank()) {
                sb.append("Responsable (non destinataire en test) : ")
                        .append(MeteoAlerteUtils.escape(ctx.emailResponsable()))
                        .append(BR);
            }
            if (ctx.emailAdjResponsable() != null && !ctx.emailAdjResponsable().isBlank()) {
                sb.append("Responsable Adjoint (non destinataire en test) : ")
                        .append(MeteoAlerteUtils.escape(ctx.emailAdjResponsable()))
                        .append(BR);
            }
            if (ctx.balfMetier() != null && !ctx.balfMetier().isBlank()) {
                sb.append("BALF métier (non destinataire en test) : ")
                        .append(MeteoAlerteUtils.escape(ctx.balfMetier()))
                        .append(BR);
            }
            sb.append(BR);
        }

        if (ctx.type() == AlerteType.RETARD) {
            sb.append(
                            "La saisie de la météo de vos applications ci-dessous est en retard (≥"
                                    + " 1 mois).")
                    .append(BR)
                    .append(BR);
        } else {
            sb.append("Vos applications ci-dessous ont une météo à bientôt mettre à jour (≥ ")
                    .append(ctx.ageMinJours())
                    .append(" jours).")
                    .append(BR)
                    .append(BR);
        }

        ctx.apps().stream()
                .sorted(
                        Comparator.comparing((Meteo m) -> MeteoAlerteUtils.daysOld(m, today))
                                .reversed())
                .forEach(
                        m -> {
                            long age = MeteoAlerteUtils.daysOld(m, today);
                            String d = (m.getDate() != null) ? m.getDate().toString() : "N/A";
                            sb.append("- ")
                                    .append(MeteoAlerteUtils.escape(m.getAppName()))
                                    .append(" — dernière météo : ")
                                    .append(MeteoAlerteUtils.escape(d))
                                    .append(" (")
                                    .append(age)
                                    .append(" jours)")
                                    .append(BR);
                        });

        sb.append(BR)
                .append(
                        "Merci de mettre à jour la météo dans Compas à cette adresse :"
                                + " https://tableau-de-bord-applications.insee.fr.")
                .append(BR)
                .append(BR)
                .append("Cordialement,")
                .append(BR)
                .append("L'équipe COMPAS")
                .append(BR);

        return sb.toString();
    }
}
