package fr.insee.compas.util.observer.listeners;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockitoAnnotations;

import fr.insee.compas.exception.FilerErrorListenerException;
import fr.insee.compas.util.observer.EventTypeObserver;
import fr.insee.compas.util.observer.listener.FileErrorListener;

class FileErrorListenerTest {

    @TempDir Path tempDir;

    private FileErrorListener fileErrorListener;
    private File logFile;
    private String logFileName;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logFileName = tempDir.resolve("test-error.log").toString();
        logFile = new File(logFileName);
        fileErrorListener = new FileErrorListener(logFileName);
    }

    @Test
    void testGetEventType() {
        EventTypeObserver expectedEventType = EventTypeObserver.EVENT_TYPE_ERROR;

        EventTypeObserver actualEventType = fileErrorListener.getEventType();

        assertEquals(expectedEventType, actualEventType);
    }

    @Test
    void testUpdate() throws IOException {
        String testData = "Test error message";

        fileErrorListener.update(testData);

        assertTrue(logFile.exists());
        String fileContent = Files.readString(logFile.toPath());
        assertTrue(fileContent.contains(testData));
    }

    @Test
    void testUpdateWithIOException() {
        String testData = "Test error message";
        FileErrorListener spyListener = spy(fileErrorListener);

        doThrow(new FilerErrorListenerException(new Throwable(), "Simulated IO error"))
                .when(spyListener)
                .update(testData);

        assertThrows(FilerErrorListenerException.class, () -> spyListener.update(testData));
    }

    @Test
    void testGetLogFile() {
        File expectedLogFile = new File(logFileName);

        File actualLogFile = fileErrorListener.getLogFile();

        assertEquals(expectedLogFile.getPath(), actualLogFile.getPath());
    }
}
