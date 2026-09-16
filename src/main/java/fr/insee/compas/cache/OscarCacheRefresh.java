package fr.insee.compas.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.insee.compas.service.OscarService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class OscarCacheRefresh {

    private static final String APPLICATIONS_OSCAR = "applicationsOscarCache";
    private static final String MODULES_OSCAR = "modulesOscarCache";

    private final OscarService oscarService;
    private final CacheManager cacheManager;

    @Scheduled(cron = "0 0 6 * * *")
    public void refreshOscarCaches() {
        log.info("Cache oscar en cours de rafraîchissement");
        clearCache(APPLICATIONS_OSCAR);
        clearCache(MODULES_OSCAR);
        log.info("Cache oscar rafraîchi");
        log.info("Récupération des applications et modules depuis Oscar");
        oscarService.getApplications();
        oscarService.getModules();
        log.info("Récupération terminée des applications et modules depuis Oscar");
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);

        if (cache != null) {
            cache.clear();
        }
    }
}
