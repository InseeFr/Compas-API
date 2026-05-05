package fr.insee.compas.util.observer.listener;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.insee.compas.exception.FilerErrorListenerException;
import fr.insee.compas.util.observer.EventTypeObserver;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FileErrorListener implements IEventListenerObserver {

    private final File logFile;
    private final String logFileName;

    public FileErrorListener(@Value("${compas.error-log-file}") String logFileName) {
        this.logFileName = logFileName;
        this.logFile = new File(logFileName);
    }

    @Override
    public EventTypeObserver getEventType() {
        return EventTypeObserver.EVENT_TYPE_ERROR;
    }

    @Override
    public synchronized void update(String data) {
        try (BufferedWriter writer =
                Files.newBufferedWriter(
                        logFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) {
            log.error("Erreur lors de l'écriture dans le fichier {}", logFileName);
            throw new FilerErrorListenerException(e, "Erreur lors de l'écriture dans le fichier");
        }
    }

    public File getLogFile() {
        return new File(logFile.getPath());
    }
}
