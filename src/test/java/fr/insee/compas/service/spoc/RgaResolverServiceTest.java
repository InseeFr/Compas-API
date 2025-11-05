package fr.insee.compas.service.spoc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.configuration.oauth.ApiRhAuthentification;
import fr.insee.compas.client.view.ApplicationOscarView;

@ExtendWith(MockitoExtension.class)
class RgaResolverServiceTest {

    @Mock OscarClient oscarClient;
    @Mock ApiRhAuthentification apiRhAuthentification;

    @InjectMocks RgaResolverService service;

    // --- 1) Cas OK + cache : même IDEP sur 2 applis -> API RH appelée 1 seule fois
    @Test
    void resolveRgaEmail_ok_and_cached_per_idep() {
        ApplicationOscarView app10 = appWithRga("IDEP123");
        ApplicationOscarView app11 = appWithRga("IDEP123");

        when(oscarClient.getApplicationOscar(10)).thenReturn(ResponseEntity.ok(app10));
        when(oscarClient.getApplicationOscar(11)).thenReturn(ResponseEntity.ok(app11));
        when(apiRhAuthentification.recupererIdepEtEmails())
                .thenReturn(Map.of("IDEP123", "rga@insee.fr"));

        String e1 = service.resolveRgaEmailByApplicationId(10);
        String e2 = service.resolveRgaEmailByApplicationId(11);

        assertThat(e1).isEqualTo("rga@insee.fr");
        assertThat(e2).isEqualTo("rga@insee.fr");

        // Une seule récupération côté API RH grâce au cache interne
        verify(apiRhAuthentification, times(1)).recupererIdepEtEmails();
        verify(oscarClient, times(1)).getApplicationOscar(10);
        verify(oscarClient, times(1)).getApplicationOscar(11);
    }

    // --- 2) Application introuvable -> null et pas d’appel API RH
    @Test
    void resolveRgaEmail_returnsNull_whenApplicationBodyIsNull() {
        when(oscarClient.getApplicationOscar(42)).thenReturn(ResponseEntity.ok(null));

        String email = service.resolveRgaEmailByApplicationId(42);

        assertThat(email).isNull();
        verify(apiRhAuthentification, never()).recupererIdepEtEmails();
    }

    // --- 3) IDEP manquant/blanc -> null et pas d’appel API RH
    @Test
    void resolveRgaEmail_returnsNull_whenIdepMissingOrBlank() {
        ApplicationOscarView app = appWithRga("   "); // blanc
        when(oscarClient.getApplicationOscar(7)).thenReturn(ResponseEntity.ok(app));

        String email = service.resolveRgaEmailByApplicationId(7);

        assertThat(email).isNull();
        verify(apiRhAuthentification, never()).recupererIdepEtEmails();
    }

    // --- 4) IDEP non présent dans API RH ou email vide -> null
    @Test
    void resolveRgaEmail_returnsNull_whenApiRhHasNoEmailForIdep() {
        ApplicationOscarView app = appWithRga("IDEP999");
        when(oscarClient.getApplicationOscar(99)).thenReturn(ResponseEntity.ok(app));

        // Variante A : pas de clé
        when(apiRhAuthentification.recupererIdepEtEmails()).thenReturn(Map.of());
        String emailA = service.resolveRgaEmailByApplicationId(99);
        assertThat(emailA).isNull();

        // Variante B : clé présente mais email vide
        when(apiRhAuthentification.recupererIdepEtEmails()).thenReturn(Map.of("IDEP999", ""));
        String emailB = service.resolveRgaEmailByApplicationId(99);
        assertThat(emailB).isNull();

        verify(apiRhAuthentification, times(2)).recupererIdepEtEmails();
    }

    // --- 5) Deux IDEP différents -> 2 appels API RH (un par nouvel IDEP), chacun ensuite en cache
    @Test
    void resolveRgaEmail_twoDifferentIdep_callsApiRhTwice_thenCached() {
        ApplicationOscarView appA1 = appWithRga("IDEP_A");
        ApplicationOscarView appA2 = appWithRga("IDEP_A");
        ApplicationOscarView appB1 = appWithRga("IDEP_B");

        when(oscarClient.getApplicationOscar(1)).thenReturn(ResponseEntity.ok(appA1));
        when(oscarClient.getApplicationOscar(2)).thenReturn(ResponseEntity.ok(appA2));
        when(oscarClient.getApplicationOscar(3)).thenReturn(ResponseEntity.ok(appB1));

        // 1er appel : pour IDEP_A
        when(apiRhAuthentification.recupererIdepEtEmails())
                .thenReturn(Map.of("IDEP_A", "a@insee.fr"));
        assertThat(service.resolveRgaEmailByApplicationId(1)).isEqualTo("a@insee.fr");

        // 2e appel avec IDEP_A (cache) -> pas de nouvel appel API RH
        assertThat(service.resolveRgaEmailByApplicationId(2)).isEqualTo("a@insee.fr");

        // 3e appel : IDEP_B (nouveau) -> nouvelle récupération API RH
        when(apiRhAuthentification.recupererIdepEtEmails())
                .thenReturn(Map.of("IDEP_B", "b@insee.fr"));
        assertThat(service.resolveRgaEmailByApplicationId(3)).isEqualTo("b@insee.fr");

        verify(apiRhAuthentification, times(2)).recupererIdepEtEmails();
    }

    // ----- helpers -----
    private static ApplicationOscarView appWithRga(String idep) {
        ApplicationOscarView app = mock(ApplicationOscarView.class);
        when(app.getRga()).thenReturn(idep);
        return app;
    }
}
