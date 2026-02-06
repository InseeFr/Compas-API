package fr.insee.compas.service.meteo.alerte;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.mail.ReceiverMailProperties;
import fr.insee.compas.model.meteo.Meteo;
import fr.insee.compas.service.meteo.MeteoAffichageService;
import fr.insee.compas.service.spoc.RgaResolverService;
import fr.insee.compas.service.spoc.SpocService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MeteoAlerteService extends MeteoExtendAlerteService implements ISendAlerteMeteo {
    private static final ZoneId TZ_PARIS = ZoneId.of("Europe/Paris");
    private final MeteoAffichageService meteoAffichageService;

    public MeteoAlerteService(
            MeteoAffichageService meteoAffichageService,
            ReceiverMailProperties receiverMailProperties,
            RgaResolverService rgaResolverService,
            TemplateAlerteMeteo templateAlerteMeteo,
            SpocService spocService) {
        super(receiverMailProperties, rgaResolverService, templateAlerteMeteo, spocService);
        this.meteoAffichageService = meteoAffichageService;
    }

    /**
     * Envoie des mails par couple (RGA, BALF métier) : - RAPPEL : dernière météo >= 23 jours et < 1
     * mois - RETARD : dernière météo >= 1 mois (ou absente) Le paramètre ageMinJours sert à
     * préfiltrer (ex. 23).
     */
    @Override
    public void sendAlerteMeteo(int ageMinJours, boolean test) {
        log.info("Envoi alertes météo - ageMin: {} jours, mode test: {}", ageMinJours, test);
        List<Meteo> meteos = meteoAffichageService.listerApplicationsMeteoAvecAgeMin(ageMinJours);
        Map<Destinataire, List<Meteo>> destinataireListMap = this.grouperAppsParRGAEtBalf(meteos);
        if (destinataireListMap.isEmpty()) {
            log.info("Aucune alerte à envoyer (0 couple RGA/BALF concerné).");
            return;
        }
        log.info("{} couple(s) RGA/BALF à traiter", destinataireListMap.size());
        LocalDate today = LocalDate.now(TZ_PARIS);
        destinataireListMap.forEach(
                (destinataire, meteos1) ->
                        this.processusEnvoie(destinataire, meteos1, today, test));
    }
}
