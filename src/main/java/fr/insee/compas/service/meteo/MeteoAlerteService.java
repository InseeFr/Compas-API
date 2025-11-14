package fr.insee.compas.service.meteo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.compas.client.configuration.oauth.ApiRhAuthentification;
import fr.insee.compas.model.mail.Mail;
import fr.insee.compas.model.meteo.Meteo;
import fr.insee.compas.service.spoc.RgaResolverService;
import fr.insee.compas.service.spoc.SpocService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeteoAlerteService {

    private static final ZoneId TZ_PARIS = ZoneId.of("Europe/Paris");
    private static final Pattern EMAIL_RX = Pattern.compile("^(?>[^@\\s]+)@[^@\\s]+\\.[^@\\s]+$");
    private static final String BR = "<br/>";

    private final MeteoAffichageService meteoAffichageService;
    private final RgaResolverService rgaResolverService;
    private final SpocService spocService;
    private final ApiRhAuthentification apiRhAuthentification;

    /** Clé de regroupement : un couple (RGA, BALF métier). */
    private record Dest(String rgaEmail, String balfMetier) {}

    private enum AlerteType {
        NONE,
        RAPPEL,
        RETARD
    }

    /**
     * Envoie des mails par couple (RGA, BALF métier) : - RAPPEL : dernière météo >= 23 jours et < 1
     * mois - RETARD : dernière météo >= 1 mois (ou absente) Le paramètre ageMinJours sert à
     * préfiltrer (ex. 23).
     */
    public void envoyerAlertesRga(int ageMinJours, boolean test) {
        List<Meteo> toutes = meteoAffichageService.listerApplicationsMeteoAvecAgeMin(ageMinJours);
        Map<Dest, List<Meteo>> parDest = groupByDest(toutes);

        if (parDest.isEmpty()) {
            log.info("Aucune alerte à envoyer (0 couple RGA/BALF concerné).");
            return;
        }

        LocalDate today = LocalDate.now(TZ_PARIS);
        parDest.forEach((dest, apps) -> processDest(dest, apps, today, test));
    }

    /** Regroupe les applis par couple (RGA, BALF métier). */
    private Map<Dest, List<Meteo>> groupByDest(List<Meteo> meteo) {
        return meteo.stream()
                .map(
                        m -> {
                            String rga = resolveRgaEmail(m.getIdApplication());
                            String balf =
                                    rgaResolverService.resolveBalfMetierByApplicationId(
                                            m.getIdApplication());
                            // On garde le couple même si la BALF est invalide/null (mail partira au
                            // RGA seul)
                            return new AbstractMap.SimpleEntry<>(new Dest(rga, balf), m);
                        })
                .filter(e -> isValidEmail(e.getKey().rgaEmail())) // RGA obligatoire et valide
                .collect(
                        Collectors.groupingBy(
                                Map.Entry::getKey,
                                Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    /**
     * Traite un couple (RGA, BALF) : classe en RAPPEL/RETARD et envoie les mails correspondants.
     */
    private void processDest(Dest dest, List<Meteo> apps, LocalDate today, boolean test) {
        Map<AlerteType, List<Meteo>> parType =
                apps.stream().collect(Collectors.groupingBy(m -> classify(m.getDate(), today)));

        List<Meteo> rappelApps = parType.getOrDefault(AlerteType.RAPPEL, List.of());
        List<Meteo> retardApps = parType.getOrDefault(AlerteType.RETARD, List.of());

        if (rappelApps.isEmpty() && retardApps.isEmpty()) return;

        Optional<String> responsableEmailOpt = findResponsableEmailForApps(apps);

        sendIfNotEmpty(test, dest, responsableEmailOpt, rappelApps, AlerteType.RAPPEL);
        sendIfNotEmpty(test, dest, responsableEmailOpt, retardApps, AlerteType.RETARD);
    }

    private void sendIfNotEmpty(
            boolean test,
            Dest dest,
            Optional<String> responsableEmailOpt,
            List<Meteo> apps,
            AlerteType type) {
        if (!apps.isEmpty()) {
            sendOne(test, dest, responsableEmailOpt, apps, type);
        }
    }

    /**
     * Envoie un mail (rappel ou retard) pour un sous-ensemble d’apps d’un même couple (RGA, BALF).
     */
    private void sendOne(
            boolean test,
            Dest dest,
            Optional<String> responsableEmailOpt,
            List<Meteo> subsetApps,
            AlerteType type) {

        String emailRga = dest.rgaEmail();
        String balfMetier = dest.balfMetier(); // peut être null / invalide

        String subject = buildSubject(subsetApps, type);
        String body =
                buildBody(
                        emailRga,
                        subsetApps,
                        test,
                        responsableEmailOpt.orElse(null),
                        balfMetier,
                        type);

        // Destinataires
        List<String> to = new ArrayList<>();
        List<String> cc = new ArrayList<>();

        if (test) {
            to.addAll(spocService.getDefaultReceivers());
        } else {
            // TO = RGA uniquement
            to.add(emailRga);

            // CC = responsable
            responsableEmailOpt.filter(this::isValidEmail).ifPresent(cc::add);

            // CC = BALF métier
            if (isValidEmail(balfMetier)) {
                cc.add(balfMetier);
            }
        }

        to = to.stream().distinct().toList();
        cc = cc.stream().distinct().toList();

        Mail mail = new Mail(subject, body, to, cc);
        spocService.sendMail(mail);
    }

    /** Classification : NONE (<23j), RAPPEL (23j.. <1 mois), RETARD (≥1 mois ou null). */
    private AlerteType classify(LocalDate dateMeteo, LocalDate today) {
        if (dateMeteo == null) return AlerteType.RETARD;
        long days = ChronoUnit.DAYS.between(dateMeteo, today);
        if (days < 23) return AlerteType.NONE;
        LocalDate oneMonthAgo = today.minusMonths(1);
        if (!dateMeteo.isAfter(oneMonthAgo)) return AlerteType.RETARD; // date <= today-1mois
        return AlerteType.RAPPEL;
    }

    private Optional<String> findResponsableEmailForApps(List<Meteo> apps) {
        String sndiNom = apps.getFirst().getSndi();
        String hie = mapSndiToHieCode(sndiNom);
        return (hie == null || apiRhAuthentification == null)
                ? Optional.empty()
                : apiRhAuthentification.findResponsableEmailByUnite(hie);
    }

    private String mapSndiToHieCode(String sndiNom) {
        if (sndiNom == null) return null;
        String s = sndiNom.trim().toLowerCase();
        if (s.contains("paris")) return "HIE2002561";
        if (s.contains("orléans") || s.contains("orleans")) return "HIE2002560";
        if (s.contains("nantes")) return "HIE2002580";
        if (s.contains("lille") || s.contains("lilles")) return "HIE2000803";
        return null;
    }

    private String resolveRgaEmail(Integer idApplication) {
        return rgaResolverService.resolveRgaEmailByApplicationId(idApplication);
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_RX.matcher(email).matches();
    }

    private String buildSubject(List<Meteo> apps, AlerteType type) {
        int n = apps.size();
        String prefix =
                (type == AlerteType.RETARD)
                        ? "[COMPAS] Météo en retard — "
                        : "[COMPAS] Rappel météo — ";
        return prefix + n + " application" + (n > 1 ? "s" : "");
    }

    private String buildBody(
            String rgaEmail,
            List<Meteo> apps,
            boolean test,
            String emailResponsable,
            String balfMetier,
            AlerteType type) {

        LocalDate today = LocalDate.now(TZ_PARIS);
        StringBuilder sb = new StringBuilder();

        if (test) {
            sb.append("⚠️ MODE TEST — Ce mail est destiné à '")
                    .append(escape(rgaEmail))
                    .append("'")
                    .append(BR);
            if (emailResponsable != null && !emailResponsable.isBlank()) {
                sb.append("Responsable (non destinataire en test) : ")
                        .append(escape(emailResponsable))
                        .append(BR);
            }
            if (balfMetier != null && !balfMetier.isBlank()) {
                sb.append("BALF métier (non destinataire en test) : ")
                        .append(escape(balfMetier))
                        .append(BR);
            }
            sb.append(BR);
        }

        if (type == AlerteType.RETARD) {
            sb.append(
                            "La saisie de la météo de vos applications ci-dessous est en retard"
                                    + " (≥ 1 mois).")
                    .append(BR)
                    .append(BR);
        } else {
            sb.append(
                            "Vos applications ci-dessous ont une météo à bientôt mettre à jour"
                                    + " (≥ 23 jours).")
                    .append(BR)
                    .append(BR);
        }

        apps.stream()
                .sorted(Comparator.comparing((Meteo m) -> daysOld(m, today)).reversed())
                .forEach(
                        m -> {
                            long age = daysOld(m, today);
                            String d = (m.getDate() != null) ? m.getDate().toString() : "N/A";
                            sb.append("- ")
                                    .append(escape(m.getAppName()))
                                    .append(" — dernière météo : ")
                                    .append(escape(d))
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
                .append("L’équipe COMPAS")
                .append(BR);

        return sb.toString();
    }

    private long daysOld(Meteo m, LocalDate today) {
        if (m.getDate() == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(m.getDate(), today);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
