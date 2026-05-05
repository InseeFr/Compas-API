package fr.insee.compas.util.template.mail;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.insee.compas.util.mail.templates.TemplateMailErreurScheduler;

class TemplateMailErreurSchedulerTest {

    private final TemplateMailErreurScheduler templateMailErreurScheduler =
            new TemplateMailErreurScheduler();

    @Test
    void testGetSubjectTemplateMail() {
        String subject = templateMailErreurScheduler.getSubjectTemplateMail();
        assertEquals("[Compas] Erreur lors de l'exécution du scheduler", subject);
    }

    @Test
    void testGetBodyTemplateMailWithTestMode() {
        List<String> authors = Arrays.asList("author1@example.com", "author2@example.com");
        List<String> cc = Arrays.asList("cc1@example.com", "cc2@example.com");
        boolean test = true;

        String body = templateMailErreurScheduler.getBodyTemplateMail(authors, cc, test);

        assertTrue(
                body.contains(
                        "⚠️ MODE TEST — Ce mail est destiné à"
                                + " author1@example.com,author2@example.com"));
        assertTrue(body.contains("Avec en cc: cc1@example.com,cc2@example.com"));
        assertTrue(
                body.contains(
                        "Une ou plusieurs erreurs ont été retrouvées durant le job du scheduler."));
        assertTrue(
                body.contains("Vous trouverez en pièce jointe le fichier contenant les erreurs."));
        assertTrue(body.contains("Cordialement,"));
        assertTrue(body.contains("L'équipe Compas"));
    }

    @Test
    void testGetBodyTemplateMailWithoutTestMode() {
        List<String> authors = List.of("author1@example.com");
        List<String> cc = List.of("cc1@example.com");
        boolean test = false;

        String body = templateMailErreurScheduler.getBodyTemplateMail(authors, cc, test);

        assertFalse(body.contains("⚠️ MODE TEST"));
        assertTrue(
                body.contains(
                        "Une ou plusieurs erreurs ont été retrouvées durant le job du scheduler."));
        assertTrue(
                body.contains("Vous trouverez en pièce jointe le fichier contenant les erreurs."));
        assertTrue(body.contains("Cordialement,"));
        assertTrue(body.contains("L'équipe Compas"));
    }
}
