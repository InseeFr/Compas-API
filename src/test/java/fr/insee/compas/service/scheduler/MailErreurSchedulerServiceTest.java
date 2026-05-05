package fr.insee.compas.service.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.insee.compas.model.mail.Mail;
import fr.insee.compas.service.spoc.SpocService;
import fr.insee.compas.util.mail.templates.TemplateMailErreurScheduler;

class MailErreurSchedulerServiceTest {

    @TempDir Path tempDir;

    @Mock private SpocService spocService;

    @Mock private TemplateMailErreurScheduler templateMailErreurScheduler;

    @InjectMocks private MailErreurSchedulerService mailErreurSchedulerService;

    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        testFile = tempDir.resolve("test-error.log").toFile();
        Files.write(testFile.toPath(), "Test error message".getBytes());
    }

    @Test
    void testSendMailErreurScheduler_FileExists() {
        // Arrange
        String subject = "Test Subject";
        String body = "Test Body";
        List<String> receivers = List.of("receiver1@example.com");
        List<String> receiversAdj = List.of("receiver2@example.com");
        boolean isTest = false;

        when(templateMailErreurScheduler.getSubjectTemplateMail()).thenReturn(subject);
        when(templateMailErreurScheduler.getBodyTemplateMail(receivers, receiversAdj, isTest))
                .thenReturn(body);

        // Act
        mailErreurSchedulerService.sendMailErreurScheduler(testFile, isTest);

        // Assert
        verify(spocService, times(1)).sendMail(any(Mail.class));
        assertFalse(testFile.exists());
    }

    @Test
    void testSendMailErreurScheduler_FileDoesNotExist() {
        // Arrange
        File nonExistentFile = new File("non-existent.log");

        // Act
        mailErreurSchedulerService.sendMailErreurScheduler(nonExistentFile, false);

        // Assert
        verify(spocService, never()).sendMail(any());
        assertFalse(nonExistentFile.exists());
    }

    @Test
    void testSendMailErreurScheduler_EmptyFile() throws IOException {
        // Arrange
        File emptyFile = tempDir.resolve("empty.log").toFile();
        Files.write(emptyFile.toPath(), new byte[0]);

        // Act
        mailErreurSchedulerService.sendMailErreurScheduler(emptyFile, false);

        // Assert
        verify(spocService, never()).sendMail(any());
        assertFalse(emptyFile.exists());
    }

    @Test
    void testSendMailErreurScheduler_TestMode() {
        // Arrange
        String subject = "Test Subject";
        String body = "Test Body";
        boolean isTest = true;

        when(templateMailErreurScheduler.getSubjectTemplateMail()).thenReturn(subject);
        when(templateMailErreurScheduler.getBodyTemplateMail(List.of(), List.of(), isTest))
                .thenReturn(body);

        // Act
        mailErreurSchedulerService.sendMailErreurScheduler(testFile, isTest);

        // Assert
        verify(spocService, times(1)).sendMail(any(Mail.class));
        assertFalse(testFile.exists());
    }
}
