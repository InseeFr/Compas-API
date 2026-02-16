package fr.insee.compas.service.meteo.alerte;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import fr.insee.compas.model.mail.Mail;
import fr.insee.compas.model.mail.ReceiverMailProperties;
import fr.insee.compas.model.meteo.MailAlerteMeteo;
import fr.insee.compas.model.meteo.Meteo;
import fr.insee.compas.service.spoc.RgaResolverService;
import fr.insee.compas.service.spoc.SpocService;
import fr.insee.compas.util.MeteoAlerteUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MeteoExtendAlerteService {

    private final ReceiverMailProperties receiverMailProperties;
    private final RgaResolverService rgaResolverService;
    private final TemplateAlerteMeteo templateAlerteMeteo;
    private final SpocService spocService;

    protected MeteoExtendAlerteService(
            ReceiverMailProperties receiverMailProperties,
            RgaResolverService rgaResolverService,
            TemplateAlerteMeteo templateAlerteMeteo,
            SpocService spocService) {
        this.receiverMailProperties = receiverMailProperties;
        this.rgaResolverService = rgaResolverService;
        this.templateAlerteMeteo = templateAlerteMeteo;
        this.spocService = spocService;
    }

    protected record Destinataire(String rgaEmail, String balfMetier) {}

    protected record MailRecipients(List<String> to, List<String> cc) {}

    /** Regroupe les applications par couple (RGA, BALF métier). Filtre les emails invalides. */
    protected Map<Destinataire, List<Meteo>> grouperAppsParRGAEtBalf(List<Meteo> meteos) {
        if (meteos == null || meteos.isEmpty()) {
            log.debug("Aucune météo à grouper");
            return Collections.emptyMap();
        }

        return meteos.stream()
                .map(this::mapToDestinataireEntry)
                .filter(entry -> isValidDestinataire(entry.getKey()))
                .collect(
                        Collectors.groupingBy(
                                Map.Entry::getKey,
                                Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    private Map.Entry<Destinataire, Meteo> mapToDestinataireEntry(Meteo meteo) {
        String rga = rgaResolverService.resolveRgaEmailByApplicationId(meteo.getIdApplication());
        String balf = rgaResolverService.resolveBalfMetierByApplicationId(meteo.getIdApplication());
        return new AbstractMap.SimpleEntry<>(new Destinataire(rga, balf), meteo);
    }

    private boolean isValidDestinataire(Destinataire destinataire) {
        return MeteoAlerteUtils.isValidEmail(destinataire.rgaEmail());
    }

    /**
     * Traite un couple (RGA, BALF) : classe en RAPPEL/RETARD et envoie les mails correspondants.
     */
    protected void processusEnvoie(
            Destinataire destinataire, List<Meteo> meteos, LocalDate today, boolean test) {

        if (meteos == null || meteos.isEmpty()) {
            log.warn("Aucune météo à traiter pour le destinataire: {}", destinataire.rgaEmail());
            return;
        }

        log.info(
                "Traitement de {} application(s) pour RGA: {}",
                meteos.size(),
                destinataire.rgaEmail());

        Map<MeteoAlerteUtils.AlerteType, List<Meteo>> appsParAlerteType =
                grouperAppsParAlerteType(meteos, today);

        List<Meteo> rappelApps =
                appsParAlerteType.getOrDefault(
                        MeteoAlerteUtils.AlerteType.RAPPEL, Collections.emptyList());
        List<Meteo> retardApps =
                appsParAlerteType.getOrDefault(
                        MeteoAlerteUtils.AlerteType.RETARD, Collections.emptyList());

        String responsableEmail = findResponsableEmailForApps(meteos);
        String responsableEmailAdj = findResponsableAdjEmailForApps(meteos);

        if (!rappelApps.isEmpty()) {
            log.info("Envoi mail RAPPEL pour {} application(s)", rappelApps.size());
            envoyerMailAlerte(
                    rappelApps,
                    MeteoAlerteUtils.AlerteType.RAPPEL,
                    destinataire,
                    responsableEmail,
                    responsableEmailAdj,
                    test);
        }

        if (!retardApps.isEmpty()) {
            log.info("Envoi mail RETARD pour {} application(s)", retardApps.size());
            envoyerMailAlerte(
                    retardApps,
                    MeteoAlerteUtils.AlerteType.RETARD,
                    destinataire,
                    responsableEmail,
                    responsableEmailAdj,
                    test);
        }
    }

    private void envoyerMailAlerte(
            List<Meteo> apps,
            MeteoAlerteUtils.AlerteType type,
            Destinataire destinataire,
            String responsableEmail,
            String responsableEmailAdj,
            boolean test) {

        MailAlerteMeteo mailAlerteMeteo =
                MailAlerteMeteo.builder()
                        .appsMeteo(apps)
                        .isTest(test)
                        .type(type)
                        .responsableEmail(Optional.ofNullable(responsableEmail))
                        .responsableAdjEmail(Optional.ofNullable(responsableEmailAdj))
                        .build();

        envoieMailPourGroupeRgaEtBalf(mailAlerteMeteo, destinataire);
    }

    private Map<MeteoAlerteUtils.AlerteType, List<Meteo>> grouperAppsParAlerteType(
            List<Meteo> meteos, LocalDate today) {
        return meteos.stream()
                .collect(
                        Collectors.groupingBy(
                                meteo -> MeteoAlerteUtils.classify(meteo.getDate(), today)));
    }

    /**
     * Envoie un mail (rappel ou retard) pour un sous-ensemble d'apps d'un même couple (RGA, BALF).
     */
    private void envoieMailPourGroupeRgaEtBalf(
            MailAlerteMeteo mailAlerteMeteo, Destinataire destinataire) {

        String mailSubject =
                templateAlerteMeteo.getSubjectTemplate(
                        mailAlerteMeteo.getAppsMeteo(), mailAlerteMeteo.getType());

        String mailBody = buildMailBody(mailAlerteMeteo, destinataire);

        MailRecipients recipients = buildMailRecipients(mailAlerteMeteo, destinataire);

        Mail mail = new Mail(mailSubject, mailBody, recipients.to(), recipients.cc());

        try {
            spocService.sendMail(mail);
            log.info(
                    "Mail {} envoyé avec succès à {} et en cc à {}",
                    mailAlerteMeteo.getType(),
                    recipients.to(),
                    recipients.cc());
        } catch (Exception e) {
            log.error(
                    "Erreur lors de l'envoi du mail {} à {}",
                    mailAlerteMeteo.getType(),
                    recipients.to(),
                    e);
        }
    }

    private String buildMailBody(MailAlerteMeteo mailAlerteMeteo, Destinataire destinataire) {
        String responsableMail =
                mailAlerteMeteo
                        .getResponsableEmail()
                        .filter(MeteoAlerteUtils::isValidEmail)
                        .orElse("");

        String responsableAdj =
                mailAlerteMeteo
                        .getResponsableAdjEmail()
                        .filter(MeteoAlerteUtils::isValidEmail)
                        .orElse("");

        return templateAlerteMeteo.getTemplateBody(
                destinataire.rgaEmail(),
                mailAlerteMeteo.getAppsMeteo(),
                mailAlerteMeteo.getIsTest(),
                responsableMail,
                responsableAdj,
                destinataire.balfMetier(),
                mailAlerteMeteo.getType());
    }

    private MailRecipients buildMailRecipients(
            MailAlerteMeteo mailAlerteMeteo, Destinataire destinataire) {

        if (Boolean.TRUE.equals(mailAlerteMeteo.getIsTest())) {
            return buildTestRecipients();
        }

        return buildProductionRecipients(mailAlerteMeteo, destinataire);
    }

    private MailRecipients buildTestRecipients() {
        log.info("Mode TEST activé - envoi aux destinataires de test");
        List<String> to = new ArrayList<>(spocService.getDefaultReceivers());
        List<String> cc = new ArrayList<>(spocService.getDefaultReceiverAdjMail());
        return new MailRecipients(to.stream().distinct().toList(), cc.stream().distinct().toList());
    }

    private MailRecipients buildProductionRecipients(
            MailAlerteMeteo mailAlerteMeteo, Destinataire destinataire) {

        List<String> to = new ArrayList<>();
        List<String> cc = new ArrayList<>();

        to.add(destinataire.rgaEmail());

        mailAlerteMeteo
                .getResponsableEmail()
                .filter(MeteoAlerteUtils::isValidEmail)
                .ifPresent(cc::add);

        mailAlerteMeteo
                .getResponsableAdjEmail()
                .filter(MeteoAlerteUtils::isValidEmail)
                .ifPresent(cc::add);

        if (MeteoAlerteUtils.isValidEmail(destinataire.balfMetier())) {
            cc.add(destinataire.balfMetier());
        }

        return new MailRecipients(to.stream().distinct().toList(), cc.stream().distinct().toList());
    }

    private Optional<String> getMailResponsable(String sndi) {
        if (sndi.isBlank()) {
            log.warn("SNDI vide pour recherche responsable");
            return Optional.empty();
        }

        String normalizedSearch = MeteoAlerteUtils.normalizeSndi(sndi);
        return receiverMailProperties.getResponsable().stream()
                .filter(r -> MeteoAlerteUtils.normalizeSndi(r.getSndi()).equals(normalizedSearch))
                .map(ReceiverMailProperties.Responsable::getMail)
                .findFirst();
    }

    private Optional<String> getMailResponsableAdj(String sndi) {
        if (sndi.isBlank()) {
            log.warn("SNDI vide pour recherche responsable adjoint");
            return Optional.empty();
        }

        String normalizedSearch = MeteoAlerteUtils.normalizeSndi(sndi);
        return receiverMailProperties.getResponsableAdj().stream()
                .filter(r -> MeteoAlerteUtils.normalizeSndi(r.getSndi()).equals(normalizedSearch))
                .map(ReceiverMailProperties.Responsable::getMail)
                .findFirst();
    }

    private String findResponsableEmailForApps(List<Meteo> apps) {
        if (apps == null || apps.isEmpty()) {
            log.warn("Liste d'applications vide pour recherche email responsable");
            return null;
        }

        Meteo firstMeteo = apps.getFirst();
        if (firstMeteo.getSndi() == null) {
            log.warn("SNDI responsable null pour l'application {}", firstMeteo.getIdApplication());
            return null;
        }

        return getMailResponsable(firstMeteo.getSndi()).orElse(null);
    }

    private String findResponsableAdjEmailForApps(List<Meteo> apps) {
        if (apps == null || apps.isEmpty()) {
            log.warn("Liste d'applications vide pour recherche email responsable adjoint");
            return null;
        }

        Meteo firstMeteo = apps.getFirst();
        if (firstMeteo.getSndi() == null) {
            log.warn("SNDI adjoint null pour l'application {}", firstMeteo.getIdApplication());
            return null;
        }

        return getMailResponsableAdj(firstMeteo.getSndi()).orElse(null);
    }
}
