package fr.insee.compas.service.meteo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.mail.Mail;
import fr.insee.compas.model.mail.ReceiverMailProperties;
import fr.insee.compas.model.meteo.Meteo;
import fr.insee.compas.service.meteo.alerte.MeteoAlerteService;
import fr.insee.compas.service.meteo.alerte.TemplateAlerteMeteo;
import fr.insee.compas.service.spoc.RgaResolverService;
import fr.insee.compas.service.spoc.SpocService;
import fr.insee.compas.util.MeteoAlerteUtils;

@ExtendWith(MockitoExtension.class)
class MeteoAlerteServiceTest {

    private static final ZoneId TZ_PARIS = ZoneId.of("Europe/Paris");

    @Mock ReceiverMailProperties receiverMailProperties;
    @Mock TemplateAlerteMeteo templateAlerteMeteo;
    @Mock MeteoAffichageService meteoAffichageService;
    @Mock RgaResolverService rgaResolverService;
    @Mock SpocService spocService;

    @InjectMocks MeteoAlerteService service;

    @BeforeEach
    void setup() {
        service =
                new MeteoAlerteService(
                        meteoAffichageService,
                        receiverMailProperties,
                        rgaResolverService,
                        templateAlerteMeteo,
                        spocService);
    }

    // --- 1) Mode TEST : groupage, tri corps, redirection vers default receivers
    @Test
    void envoyerAlertesRga_testMode_groupByRga_and_sortedBody_and_defaultReceivers() {
        LocalDate today = LocalDate.of(2025, 3, 1); // calculé AVANT le mock static

        Meteo m10 = meteo(10, "App A", LocalDate.of(2025, 1, 1)); // RGA1
        Meteo m12 = meteo(12, "App C", LocalDate.of(2025, 1, 20)); // RGA1
        Meteo m11 = meteo(11, "App B", LocalDate.of(2025, 2, 10)); // bad email -> ignorée
        Meteo m13 = meteo(13, "App D", LocalDate.of(2024, 12, 31)); // RGA2

        when(meteoAffichageService.listerApplicationsMeteoAvecAgeMin(30))
                .thenReturn(List.of(m10, m12, m11, m13));

        when(rgaResolverService.resolveRgaEmailByApplicationId(10)).thenReturn("rga1@insee.fr");
        when(rgaResolverService.resolveRgaEmailByApplicationId(12)).thenReturn("rga1@insee.fr");
        when(rgaResolverService.resolveRgaEmailByApplicationId(11)).thenReturn("pas-un-mail");
        when(rgaResolverService.resolveRgaEmailByApplicationId(13)).thenReturn("rga2@insee.fr");

        List<String> defaults = List.of("debug@compas.local");
        List<String> defaultsAdj = List.of("debugAdj@compas.local");
        when(spocService.getDefaultReceivers()).thenReturn(defaults);
        when(spocService.getDefaultReceiverAdjMail()).thenReturn(defaultsAdj);

        when(templateAlerteMeteo.getTemplateBody(
                        anyString(),
                        anyList(),
                        anyBoolean(),
                        anyString(),
                        anyString(),
                        nullable(String.class),
                        any(MeteoAlerteUtils.AlerteType.class),
                        anyInt()))
                .thenAnswer(
                        invocation -> {
                            String emailRga = invocation.getArgument(0);
                            List<Meteo> apps = invocation.getArgument(1);

                            if (apps.isEmpty()) {
                                return "Mail pour " + emailRga + " avec aucune application";
                            }

                            return "Mail pour "
                                    + emailRga
                                    + " avec "
                                    + apps.size()
                                    + " applis: "
                                    + apps.stream()
                                            .map(Meteo::getAppName)
                                            .collect(Collectors.joining(", "));
                        });

        when(templateAlerteMeteo.getSubjectTemplate(
                        anyList(), any(MeteoAlerteUtils.AlerteType.class)))
                .thenAnswer(
                        invocation -> {
                            List<Meteo> apps = invocation.getArgument(0);
                            return "[COMPAS] Météo en retard — "
                                    + apps.size()
                                    + (apps.size() > 1 ? " applications" : " application");
                        });

        try (MockedStatic<LocalDate> mocked =
                Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> LocalDate.now(TZ_PARIS)).thenReturn(today);
            service.sendAlerteMeteo(30, true);
        }

        ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(spocService, times(2)).sendMail(mailCaptor.capture());

        List<Mail> mails =
                mailCaptor.getAllValues().stream()
                        .filter(m -> !m.getObject().contains("— 0 application"))
                        .toList();

        assertThat(mails).hasSize(2);

        Mail mailRga1 =
                mails.stream()
                        .filter(m -> m.getMessage().contains("rga1@insee.fr"))
                        .findFirst()
                        .orElseThrow();
        Mail mailRga2 =
                mails.stream()
                        .filter(m -> m.getMessage().contains("rga2@insee.fr"))
                        .findFirst()
                        .orElseThrow();

        // RGA1 : 2 applis -> pluriel
        assertThat(mailRga1.getObject()).isEqualTo("[COMPAS] Météo en retard — 2 applications");
        assertThat(mailRga1.getTo()).isEqualTo(defaults);
        assertThat(mailRga1.getCc()).isEqualTo(defaultsAdj);
        String b1 = mailRga1.getMessage();
        assertThat(b1).contains("App A").contains("App C");
        assertThat(b1.indexOf("App A")).isLessThan(b1.indexOf("App C")); // plus ancienne d'abord

        // RGA2 : 1 appli -> singulier
        assertThat(mailRga2.getObject()).isEqualTo("[COMPAS] Météo en retard — 1 application");
        assertThat(mailRga2.getTo()).isEqualTo(defaults);
        assertThat(mailRga2.getCc()).isEqualTo(defaultsAdj);
        assertThat(mailRga2.getMessage())
                .contains("App D")
                .doesNotContain("App A")
                .doesNotContain("App C");
    }

    // --- 2) Mode PROD : envoi direct au RGA
    @Test
    void envoyerAlertesRga_prodMode_sendsToRgaDirectly() {
        LocalDate today = LocalDate.of(2025, 3, 1);
        when(receiverMailProperties.getResponsable())
                .thenReturn(List.of(responsable("SNDI1", "resp1@insee.fr")));

        when(receiverMailProperties.getResponsableAdj())
                .thenReturn(List.of(responsable("SNDI1", "adj1@insee.fr")));
        Meteo m10 = meteo(10, "App A", LocalDate.of(2025, 1, 1));
        m10.setSndi("SNDI1");
        when(meteoAffichageService.listerApplicationsMeteoAvecAgeMin(45)).thenReturn(List.of(m10));
        when(rgaResolverService.resolveRgaEmailByApplicationId(10)).thenReturn("rga1@insee.fr");
        when(templateAlerteMeteo.getTemplateBody(
                        anyString(),
                        anyList(),
                        anyBoolean(),
                        anyString(),
                        anyString(),
                        nullable(String.class),
                        any(MeteoAlerteUtils.AlerteType.class),
                        anyInt()))
                .thenAnswer(
                        invocation -> {
                            String emailRga = invocation.getArgument(0);
                            List<Meteo> apps = invocation.getArgument(1);
                            if (apps.isEmpty()) return "Mail vide";
                            return "Mail pour " + emailRga + " - " + apps.getFirst().getAppName();
                        });

        when(templateAlerteMeteo.getSubjectTemplate(
                        anyList(), any(MeteoAlerteUtils.AlerteType.class)))
                .thenAnswer(
                        invocation -> {
                            List<Meteo> apps = invocation.getArgument(0);
                            return "[COMPAS] Météo en retard — "
                                    + apps.size()
                                    + (apps.size() > 1 ? " applications" : " application");
                        });

        try (MockedStatic<LocalDate> mocked =
                Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> LocalDate.now(TZ_PARIS)).thenReturn(today);
            service.sendAlerteMeteo(45, false);
        }

        ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(spocService, atLeastOnce()).sendMail(mailCaptor.capture());
        verify(spocService, never()).getDefaultReceivers();
        verify(spocService, never()).getDefaultReceiverAdjMail();

        Mail sent = mailCaptor.getValue();
        assertThat(sent.getTo()).containsExactly("rga1@insee.fr");
        assertThat(sent.getCc()).containsExactlyInAnyOrder("resp1@insee.fr", "adj1@insee.fr");
        assertThat(sent.getObject()).isEqualTo("[COMPAS] Météo en retard — 1 application");
        assertThat(sent.getMessage()).doesNotContain("MODE TEST");
        assertThat(sent.getMessage()).contains("App A");
    }

    // --- 3) Aucun envoi si aucune appli / emails invalides seulement
    @Test
    void envoyerAlertesRga_noAnciennes_noSend() {
        when(meteoAffichageService.listerApplicationsMeteoAvecAgeMin(30)).thenReturn(List.of());

        service.sendAlerteMeteo(30, true);

        verify(spocService, never()).sendMail(any());
        verify(spocService, never()).getDefaultReceivers();
    }

    @Test
    void envoyerAlertesRga_allInvalidEmails_noSend() {
        LocalDate today = LocalDate.of(2025, 3, 1);
        Meteo m10 = meteo(10, "App A", LocalDate.of(2025, 1, 1));
        Meteo m11 = meteo(11, "App B", LocalDate.of(2025, 1, 15));

        when(meteoAffichageService.listerApplicationsMeteoAvecAgeMin(30))
                .thenReturn(List.of(m10, m11));
        when(rgaResolverService.resolveRgaEmailByApplicationId(10)).thenReturn("not-an-email");
        when(rgaResolverService.resolveRgaEmailByApplicationId(11))
                .thenReturn("also@bad"); // pas de TLD -> invalide

        try (MockedStatic<LocalDate> mocked =
                Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> LocalDate.now(TZ_PARIS)).thenReturn(today);
            service.sendAlerteMeteo(30, true);
        }

        verify(spocService, never()).sendMail(any());
        verify(spocService, never()).getDefaultReceivers();
    }

    // -------- helpers --------
    private static Meteo meteo(int idApp, String name, LocalDate date) {
        return Meteo.builder().idApplication(idApp).appName(name).date(date).build();
    }

    private ReceiverMailProperties.Responsable responsable(String sndi, String mail) {
        ReceiverMailProperties.Responsable r = new ReceiverMailProperties.Responsable();
        r.setSndi(sndi);
        r.setMail(mail);
        return r;
    }
}
