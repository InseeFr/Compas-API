package fr.insee.compas.cache;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import fr.insee.compas.service.OscarService;

@ExtendWith(MockitoExtension.class)
class OscarCacheRefreshTest {

    private static final String APPLICATIONS_OSCAR = "applicationsOscarCache";
    private static final String MODULES_OSCAR = "modulesOscarCache";

    @Mock private OscarService oscarService;

    @Mock private CacheManager cacheManager;

    @Mock private Cache applicationsCache;

    @Mock private Cache modulesCache;

    @Test
    void shouldRefreshOscarCaches() {
        when(cacheManager.getCache(APPLICATIONS_OSCAR)).thenReturn(applicationsCache);

        when(cacheManager.getCache(MODULES_OSCAR)).thenReturn(modulesCache);

        OscarCacheRefresh cacheRefresh = new OscarCacheRefresh(oscarService, cacheManager);

        cacheRefresh.refreshOscarCaches();

        verify(cacheManager).getCache(APPLICATIONS_OSCAR);
        verify(cacheManager).getCache(MODULES_OSCAR);

        verify(applicationsCache).clear();
        verify(modulesCache).clear();

        verify(oscarService).getApplications();
        verify(oscarService).getModules();
    }

    @Test
    void shouldClearCachesBeforeRefreshing() {
        when(cacheManager.getCache(APPLICATIONS_OSCAR)).thenReturn(applicationsCache);

        when(cacheManager.getCache(MODULES_OSCAR)).thenReturn(modulesCache);

        OscarCacheRefresh cacheRefresh = new OscarCacheRefresh(oscarService, cacheManager);

        cacheRefresh.refreshOscarCaches();

        InOrder inOrder = inOrder(applicationsCache, modulesCache, oscarService);

        inOrder.verify(applicationsCache).clear();
        inOrder.verify(modulesCache).clear();
        inOrder.verify(oscarService).getApplications();
        inOrder.verify(oscarService).getModules();
    }

    @Test
    void shouldRefreshEvenWhenCacheDoesNotExist() {
        when(cacheManager.getCache(APPLICATIONS_OSCAR)).thenReturn(null);

        when(cacheManager.getCache(MODULES_OSCAR)).thenReturn(null);

        OscarCacheRefresh cacheRefresh = new OscarCacheRefresh(oscarService, cacheManager);

        cacheRefresh.refreshOscarCaches();

        verify(cacheManager).getCache(APPLICATIONS_OSCAR);
        verify(cacheManager).getCache(MODULES_OSCAR);

        verify(oscarService).getApplications();
        verify(oscarService).getModules();
    }
}
