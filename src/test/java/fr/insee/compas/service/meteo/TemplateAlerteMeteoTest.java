package fr.insee.compas.service.meteo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insee.compas.model.meteo.Meteo;
import fr.insee.compas.service.meteo.alerte.TemplateAlerteMeteo;
import fr.insee.compas.util.MeteoAlerteUtils.AlerteType;

class TemplateAlerteMeteoTest {

    private TemplateAlerteMeteo template;

    @BeforeEach
    void setUp() {
        template = new TemplateAlerteMeteo();
    }

    @Test
    void testGetSubjectTemplate() {
        Meteo m1 =
                Meteo.builder()
                        .sndi("SND2")
                        .valeurMeteo(BigDecimal.ONE)
                        .date(LocalDate.of(2026, 1, 13))
                        .build();
        m1.setAppName("App1");
        Meteo m2 =
                Meteo.builder()
                        .sndi("SND2")
                        .valeurMeteo(BigDecimal.ONE)
                        .date(LocalDate.of(2026, 1, 13))
                        .build();
        m2.setAppName("App2");

        // Test RETARD
        String subject = template.getSubjectTemplate(List.of(m1, m2), AlerteType.RETARD);
        assertEquals("[COMPAS] Météo en retard — 2 applications", subject);

        // Test RAPPEL
        subject = template.getSubjectTemplate(List.of(m1), AlerteType.RAPPEL);
        assertEquals("[COMPAS] Rappel météo — 1 application", subject);
    }

    @Test
    void testGetTemplateBodyWithTestMode() {
        Meteo m1 =
                Meteo.builder()
                        .sndi("SND1")
                        .valeurMeteo(BigDecimal.ONE)
                        .date(LocalDate.of(2026, 1, 12))
                        .build();
        m1.setAppName("App1");
        m1.setDate(LocalDate.now().minusDays(10));

        String rgaEmail = "test@example.com";
        String emailResp = "resp@example.com";
        String emailAdj = "adj@example.com";
        String balf = "balf@example.com";

        String body =
                template.getTemplateBody(
                        rgaEmail, List.of(m1), true, emailResp, emailAdj, balf, AlerteType.RETARD, 26);

        assertTrue(body.contains("⚠️ MODE TEST"));
        assertTrue(body.contains(rgaEmail));
        assertTrue(body.contains(emailResp));
        assertTrue(body.contains(emailAdj));
        assertTrue(body.contains(balf));
        assertTrue(body.contains("App1"));
        assertTrue(body.contains("dernière météo"));
        assertTrue(body.contains("jours"));
        assertTrue(body.contains("Merci de mettre à jour la météo"));
    }

    @Test
    void testGetTemplateBodyWithoutTestMode() {
        Meteo m1 =
                Meteo.builder()
                        .sndi("SND3")
                        .valeurMeteo(BigDecimal.ONE)
                        .date(LocalDate.of(2026, 1, 12))
                        .build();
        m1.setAppName("App1");
        m1.setDate(LocalDate.now().minusDays(5));
        String rgaEmail = "user@example.com";
        String emailResp = "resp@example.com";
        String emailAdj = "adj@example.com";
        String balf = "balf@example.com";

        String body =
                template.getTemplateBody(
                        rgaEmail, List.of(m1), false, emailResp, emailAdj, balf, AlerteType.RAPPEL, 26);

        assertFalse(body.contains("MODE TEST"));
        assertTrue(
                body.contains("Vos applications ci-dessous ont une météo à bientôt mettre à jour"));
        assertTrue(body.contains("App1"));
    }
}
