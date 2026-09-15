package fr.insee.compas.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.insee.compas.service.OscarService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class OscarCacheRefresh {

    private static final String APPLICATIONS_OSCAR = "applicationsOscarCache";
    private static final String MODULES_OSCAR = "modulesOscarCache";

    private final OscarService oscarService;
    private final CacheManager cacheManager;

    @Scheduled(cron = "0 0 6 * * *")
    public void refreshOscarCaches() {
        clearCache(APPLICATIONS_OSCAR);
        clearCache(MODULES_OSCAR);

        oscarService.getApplications();
        oscarService.getModules();
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);

        if (cache != null) {
            cache.clear();
        }
    }
}
