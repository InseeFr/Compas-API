package fr.insee.compas.service.spoc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.configuration.oauth.ApiRhAuthentification;
import fr.insee.compas.client.view.ApplicationOscarView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Résout l'email du RGA et la BALF métier d'une application : Oscar -> IDEP du RGA -> API RH (map
 * idep->email) -> email. Également, récupère la BALF métier directement depuis Oscar.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RgaResolverService {

    private final OscarClient oscarClient;
    private final ApiRhAuthentification apiRhAuthentification;

    // petit cache mémoire (IDEP -> email) pour éviter de redemander à RH plusieurs fois
    private final Map<String, String> cacheIdepToEmail = new ConcurrentHashMap<>();
    // cache pour les BALF (idApplication -> email)
    private final Map<Integer, String> cacheBalf = new ConcurrentHashMap<>();

    /**
     * @param idApplication id de l'application (Oscar)
     * @return email du RGA, ou null si introuvable
     */
    public String resolveRgaEmailByApplicationId(Integer idApplication) {
        ApplicationOscarView app = oscarClient.getApplicationOscar(idApplication).getBody();
        if (app == null) {
            log.warn("Application Oscar {} introuvable", idApplication);
            return null;
        }

        String idepRga = app.getRga();
        if (idepRga == null || idepRga.isBlank()) {
            log.info("Aucun IDEP RGA pour l'application {}", idApplication);
            return null;
        }

        // Vérifie le cache
        String cached = cacheIdepToEmail.get(idepRga);
        if (cached != null) return cached;

        // Appelle l’API RH pour récupérer la map IDEP -> email
        Map<String, String> idepMap = apiRhAuthentification.recupererIdepEtEmails();
        String email = idepMap.get(idepRga);

        if (email == null || email.isBlank()) {
            log.info("Pas d'email pour IDEP RGA {} (app {}) dans l'API RH", idepRga, idApplication);
            return null;
        }

        cacheIdepToEmail.put(idepRga, email);
        return email;
    }

    /**
     * @param idApplication id de l'application (Oscar)
     * @return adresse BALF métier (souvent une adresse fonctionnelle), ou null si absente
     */
    public String resolveBalfMetierByApplicationId(Integer idApplication) {
        // Vérifie le cache d’abord
        if (cacheBalf.containsKey(idApplication)) {
            return cacheBalf.get(idApplication);
        }

        try {
            var response = oscarClient.getApplicationOscar(idApplication);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String balf = response.getBody().getBalfMetier();

                if (balf != null && !balf.isBlank()) {
                    cacheBalf.put(idApplication, balf);
                    log.debug("BALF métier trouvée pour app {} : {}", idApplication, balf);
                    return balf;
                } else {
                    log.info("Aucune BALF métier trouvée pour l'application {}", idApplication);
                    return null;
                }
            } else {
                log.warn(
                        "Oscar /applications/{} -> HTTP {}",
                        idApplication,
                        response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error(
                    "Erreur lors de la récupération de la BALF métier pour l'application {}",
                    idApplication,
                    e);
            return null;
        }
    }
}
