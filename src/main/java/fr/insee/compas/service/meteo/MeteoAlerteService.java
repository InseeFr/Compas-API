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

    private enum AlerteType {
        NONE,
        RAPPEL,
        RETARD
    }

    /**
     * Envoie des mails par RGA : - RAPPEL : dernière météo >= 23 jours et < 1 mois - RETARD :
     * dernière météo >= 1 mois (ou absente) Le paramètre ageMinJours sert à préfiltrer (ex. 23).
     */
    public void envoyerAlertesRga(int ageMinJours, boolean test) {
        List<Meteo> toutes = meteoAffichageService.listerApplicationsMeteoAvecAgeMin(ageMinJours);
        Map<String, List<Meteo>> parEmailRga = groupByRgaEmail(toutes);

        if (parEmailRga.isEmpty()) {
            log.info("Aucune alerte à envoyer (0 RGA concernés).");
            return;
        }

        LocalDate today = LocalDate.now(TZ_PARIS);
        parEmailRga.forEach((emailRga, apps) -> processRga(emailRga, apps, today, test));
    }

    private Map<String, List<Meteo>> groupByRgaEmail(List<Meteo> meteo) {
        return meteo.stream()
                .map(m -> new AbstractMap.SimpleEntry<>(resolveRgaEmail(m.getIdApplication()), m))
                .filter(e -> isValidEmail(e.getKey()))
                .collect(
                        Collectors.groupingBy(
                                Map.Entry::getKey,
                                Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    private void processRga(String emailRga, List<Meteo> apps, LocalDate today, boolean test) {
        Map<AlerteType, List<Meteo>> parType =
                apps.stream().collect(Collectors.groupingBy(m -> classify(m.getDate(), today)));

        List<Meteo> rappelApps = parType.getOrDefault(AlerteType.RAPPEL, List.of());
        List<Meteo> retardApps = parType.getOrDefault(AlerteType.RETARD, List.of());

        if (rappelApps.isEmpty() && retardApps.isEmpty()) return;

        Optional<String> responsableEmailOpt = findResponsableEmailForApps(apps);

        sendIfNotEmpty(test, emailRga, responsableEmailOpt, rappelApps, AlerteType.RAPPEL);
        sendIfNotEmpty(test, emailRga, responsableEmailOpt, retardApps, AlerteType.RETARD);
    }

    private Optional<String> findResponsableEmailForApps(List<Meteo> apps) {
        String sndiNom = apps.getFirst().getSndi();
        String hie = mapSndiToHieCode(sndiNom);
        return (hie == null || apiRhAuthentification == null)
                ? Optional.empty()
                : apiRhAuthentification.findResponsableEmailByUnite(hie);
    }

    private void sendIfNotEmpty(
            boolean test,
            String emailRga,
            Optional<String> responsableEmailOpt,
            List<Meteo> apps,
            AlerteType type) {
        if (!apps.isEmpty()) {
            sendOne(test, emailRga, responsableEmailOpt, apps, type);
        }
    }

    /** Envoie un mail (rappel ou retard) pour un sous-ensemble d’apps d’un même RGA. */
    private void sendOne(
            boolean test,
            String emailRga,
            Optional<String> responsableEmailOpt,
            List<Meteo> subsetApps,
            AlerteType type) {

        // Agrège toutes les BALF métier des applis concernées
        Set<String> balfsMetier =
                subsetApps.stream()
                        .map(
                                m ->
                                        rgaResolverService.resolveBalfMetierByApplicationId(
                                                m.getIdApplication()))
                        .filter(this::isValidEmail)
                        .collect(Collectors.toSet());

        String subject = buildSubject(subsetApps, type);
        String body =
                buildBody(
                        emailRga,
                        subsetApps,
                        test,
                        responsableEmailOpt.orElse(null),
                        balfsMetier,
                        type);

        // Destinataires
        List<String> receivers = new ArrayList<>();
        if (test) {
            receivers.addAll(spocService.getDefaultReceivers()); // redirection test
        } else {
            receivers.add(emailRga); // RGA
            responsableEmailOpt
                    .filter(this::isValidEmail)
                    .ifPresent(receivers::add); // CC responsable
            receivers.addAll(balfsMetier); // CC BALF(s)
        }
        // dédoublonnage
        receivers = receivers.stream().distinct().toList();

        Mail mail = new Mail(subject, body, receivers);
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
            Set<String> balfsMetier,
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
            if (!balfsMetier.isEmpty()) {
                sb.append("BALF(s) métier (non destinataire(s) en test) : ")
                        .append(escape(String.join(", ", balfsMetier)))
                        .append(BR);
            }
            sb.append(BR);
        }

        if (type == AlerteType.RETARD) {
            sb.append("<b>Vos applications ci-dessous ont une météo en RETARD (≥ 1 mois).</b>")
                    .append(BR)
                    .append(BR);
        } else {
            sb.append(
                            "<b>RAPPEL : vos applications ci-dessous ont une météo à mettre à jour"
                                    + " (≥ 23 jours).</b>")
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
                .append("Merci de mettre à jour la météo dans Compas.")
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
