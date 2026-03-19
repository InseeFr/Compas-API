package fr.insee.compas.logic.update.greenit;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

public interface GreenItMetricsUpdater {

    void miseAJourIndicateursGreenItFromFile(MultipartFile file, LocalDate fileDate);
}
