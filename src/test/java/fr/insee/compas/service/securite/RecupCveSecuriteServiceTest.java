package fr.insee.compas.service.securite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import fr.insee.compas.client.configuration.oauth.AnalyzerAuthentification;
import fr.insee.compas.model.analyzer.ApplicationAnalyzer;
import fr.insee.compas.model.analyzer.CveActivesAnalyzer;
import fr.insee.compas.model.analyzer.ModuleAnalyzer;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.TableFaitsRepository;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class RecupCveSecuriteServiceTest {

    @Mock TableFaitsRepository tableFaitsRepository;
    @Mock UtilsCveService utilService;
    @Mock RestTemplate restTemplate;
    @Mock AnalyzerAuthentification analyzerAuthentification;

    @InjectMocks RecupCveSecuriteService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "apiBaseUrl", "https://analyzer.example/api");
    }

    @Test
    @DisplayName(
            "recupereCve: pas d'applications (body null) -> aucun save et header Bearer présent")
    void recupereCve_noApps_nullBody() {
        given(analyzerAuthentification.execute()).willReturn("tok");
        // Body null => le service doit traiter comme une liste vide
        given(
                        restTemplate.exchange(
                                anyString(),
                                eq(HttpMethod.GET),
                                any(HttpEntity.class),
                                eq(ApplicationAnalyzer[].class)))
                .willReturn(ResponseEntity.ok(null));

        assertDoesNotThrow(() -> service.recupereCve());

        verify(tableFaitsRepository, never()).save(any());
        verify(restTemplate)
                .exchange(
                        eq("https://analyzer.example/api/cve-par-application"),
                        eq(HttpMethod.GET),
                        argThat(
                                (HttpEntity<?> e) ->
                                        "Bearer tok"
                                                .equals(
                                                        e.getHeaders()
                                                                .getFirst(
                                                                        HttpHeaders
                                                                                .AUTHORIZATION))),
                        eq(ApplicationAnalyzer[].class));
    }

    @Test
    @DisplayName("recupereCve: erreurs API -> capturées et aucun save")
    void recupereCve_apiErrorCaught() {
        given(analyzerAuthentification.execute()).willReturn("tok");
        given(
                        restTemplate.exchange(
                                anyString(),
                                eq(HttpMethod.GET),
                                any(HttpEntity.class),
                                eq(ApplicationAnalyzer[].class)))
                .willThrow(new RestClientException("boom"));

        // recupereCve() attrape toute Exception et ne relance pas
        assertDoesNotThrow(() -> service.recupereCve());
        verify(tableFaitsRepository, never()).save(any());
    }

    @Test
    @DisplayName("recupereCve: sauvegarde app + modules ayant une date de scan")
    void recupereCve_savesAppAndScannedModules() {
        // Stubs nécessaires uniquement pour ce test
        given(utilService.getIndicateurApplication("CRITICAL")).willReturn(11);
        given(utilService.getIndicateurApplication("HIGH")).willReturn(12);
        given(utilService.getIndicateurApplication("MEDIUM")).willReturn(13);
        given(utilService.getIndicateurApplication("LOW")).willReturn(14);
        given(utilService.getIndicateurModule("CRITICAL")).willReturn(21);
        given(utilService.getIndicateurModule("HIGH")).willReturn(22);
        given(utilService.getIndicateurModule("MEDIUM")).willReturn(23);
        given(utilService.getIndicateurModule("LOW")).willReturn(24);

        given(analyzerAuthentification.execute()).willReturn("tok");

        // --- Application 101 avec 2 modules: m1 (scanné), m2 (non scanné)
        ApplicationAnalyzer app1 = mock(ApplicationAnalyzer.class);
        when(app1.getId()).thenReturn(101);
        when(app1.getNom()).thenReturn("App101");
        when(app1.getCveActives())
                .thenReturn(
                        CveActivesAnalyzer.builder()
                                .nombreCveCritique(5)
                                .nombreCveMajeur(4)
                                .nombreCveMoyenne(3)
                                .nombreCveFaible(2)
                                .build());

        ModuleAnalyzer m1 = mock(ModuleAnalyzer.class);
        when(m1.getId()).thenReturn(1001);
        when(m1.getDateMajAnalyseTrivyCodeSource()).thenReturn(new java.util.Date()); // scanné
        when(m1.getCveActives())
                .thenReturn(
                        CveActivesAnalyzer.builder()
                                .nombreCveCritique(9)
                                .nombreCveMajeur(8)
                                .nombreCveMoyenne(7)
                                .nombreCveFaible(6)
                                .build());

        ModuleAnalyzer m2 = mock(ModuleAnalyzer.class);
        when(m2.getId()).thenReturn(1002);
        when(m2.getDateMajAnalyseTrivyCodeSource()).thenReturn(null); // ignoré

        when(app1.getModules()).thenReturn(List.of(m1, m2));

        // --- Application 202 sans modules -> ignorée
        ApplicationAnalyzer app2 = mock(ApplicationAnalyzer.class);
        when(app2.getId()).thenReturn(202);
        when(app2.getNom()).thenReturn("App202");
        when(app2.getModules()).thenReturn(null);

        // --- Application 303 avec 1 module scanné m3
        ApplicationAnalyzer app3 = mock(ApplicationAnalyzer.class);
        when(app3.getId()).thenReturn(303);
        when(app3.getNom()).thenReturn("App303");
        when(app3.getCveActives())
                .thenReturn(
                        CveActivesAnalyzer.builder()
                                .nombreCveCritique(1)
                                .nombreCveMajeur(1)
                                .nombreCveMoyenne(1)
                                .nombreCveFaible(1)
                                .build());

        ModuleAnalyzer m3 = mock(ModuleAnalyzer.class);
        when(m3.getId()).thenReturn(3001);
        when(m3.getDateMajAnalyseTrivyCodeSource()).thenReturn(new java.util.Date());
        when(m3.getCveActives())
                .thenReturn(
                        CveActivesAnalyzer.builder()
                                .nombreCveCritique(3)
                                .nombreCveMajeur(2)
                                .nombreCveMoyenne(1)
                                .nombreCveFaible(0)
                                .build());

        when(app3.getModules()).thenReturn(List.of(m3));

        ApplicationAnalyzer[] payload = new ApplicationAnalyzer[] {app1, app2, app3};
        given(
                        restTemplate.exchange(
                                anyString(),
                                eq(HttpMethod.GET),
                                any(HttpEntity.class),
                                eq(ApplicationAnalyzer[].class)))
                .willReturn(ResponseEntity.ok(payload));

        service.recupereCve();

        // 4 enregistrements par application retenue (app1 et app3)
        // + 4 par module scanné (m1, m3) => 16
        ArgumentCaptor<TableFaits> cap = ArgumentCaptor.forClass(TableFaits.class);
        verify(tableFaitsRepository, times(16)).save(cap.capture());
        List<TableFaits> saved = cap.getAllValues();

        long app1Count =
                saved.stream()
                        .filter(tf -> tf.getIdApplication().equals(101) && tf.getIdModule() == null)
                        .count();
        long app3Count =
                saved.stream()
                        .filter(tf -> tf.getIdApplication().equals(303) && tf.getIdModule() == null)
                        .count();
        long modM1Count =
                saved.stream()
                        .filter(
                                tf ->
                                        tf.getIdApplication().equals(101)
                                                && Integer.valueOf(1001).equals(tf.getIdModule()))
                        .count();
        long modM3Count =
                saved.stream()
                        .filter(
                                tf ->
                                        tf.getIdApplication().equals(303)
                                                && Integer.valueOf(3001).equals(tf.getIdModule()))
                        .count();

        assertThat(app1Count).isEqualTo(4);
        assertThat(app3Count).isEqualTo(4);
        assertThat(modM1Count).isEqualTo(4);
        assertThat(modM3Count).isEqualTo(4);

        assertThat(saved)
                .extracting(
                        TableFaits::getIdApplication,
                        TableFaits::getIdModule,
                        TableFaits::getIdIndicateur,
                        TableFaits::getValeur)
                .contains(
                        tuple(101, null, 11, BigDecimal.valueOf(5)),
                        tuple(101, 1001, 24, BigDecimal.valueOf(6)),
                        tuple(303, null, 14, BigDecimal.valueOf(1)),
                        tuple(303, 3001, 21, BigDecimal.valueOf(3)));
    }
}
