package fr.insee.compas.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import fr.insee.compas.exception.CompasUploadException;
import fr.insee.compas.exception.ErrorVM;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.repository.TableFaitsRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FichierControlService {

    private final TableFaitsRepository tableFaitsRepository;
    private static final String EXCEPTION_FORMAT_DATE_INVALIDE =
            "Format de date invalide dans le nom du fichier";

    public FichierControlService(TableFaitsRepository tableFaitsRepository) {
        super();
        this.tableFaitsRepository = tableFaitsRepository;
    }

    private static final String FILE_NAME_PATTERN = "^vm-metrique-(\\d{8})\\.csv$";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public boolean isValidFileName(String fileName) {
        return fileName != null && fileName.matches(FILE_NAME_PATTERN);
    }

    public LocalDate extractDateFromFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        final Pattern pattern = Pattern.compile(FILE_NAME_PATTERN);
        final Matcher matcher = pattern.matcher(fileName);

        if (matcher.matches()) {
            final String dateStr = matcher.group(1);
            try {
                return LocalDate.parse(dateStr, DATE_FORMATTER);
            } catch (final DateTimeParseException e) {
                final ErrorVM errorVM = new ErrorVM();
                log.info(EXCEPTION_FORMAT_DATE_INVALIDE);
                errorVM.setCle("date.formatInvalide");
                errorVM.setMessage(EXCEPTION_FORMAT_DATE_INVALIDE);
                throw new CompasUploadException(422, errorVM);
            }
        }
        return null;
    }

    /*
     * pour vérifier qu'on l'a déjà reçu, en attendant une tables pour tracer les fichiers, on vérifie qu'il y a des enregistrements d'indicateurs de conso
     * électrique pour ce même jour dans la table de fait
     */
    public boolean isFileDejaRecu(LocalDate fileDate) {
        return tableFaitsRepository.countGreenItValuesByDate(
                        fileDate, IndicateurType.CONSO_ELEC.getValue())
                != 0;
    }

    public LocalDate controlFileName(String fileName) {
        if (!isValidFileName(fileName)) {
            final ErrorVM errorVM = new ErrorVM();
            errorVM.setCle("fichier.nomInvalide");
            errorVM.setMessage(
                    "Nom de fichier invalide. Format attendu : vm-metrique-yyyymmdd.csv");
            throw new CompasUploadException(422, errorVM);
        }
        final LocalDate fileDate = extractDateFromFileName(fileName);
        if (fileDate == null) {
            final ErrorVM errorVM = new ErrorVM();
            log.info(EXCEPTION_FORMAT_DATE_INVALIDE);
            errorVM.setCle("ficher.dateInvalide");
            errorVM.setMessage(EXCEPTION_FORMAT_DATE_INVALIDE);
            throw new CompasUploadException(422, errorVM);
        }
        if (isFileDejaRecu(fileDate)) {
            final ErrorVM errorVM = new ErrorVM();
            log.info("Fichier déjà chargé");
            errorVM.setCle("fichier.dejaReçu");
            errorVM.setMessage("Fichier déjà chargé");
            throw new CompasUploadException(400, errorVM);
        }
        return fileDate;
    }
}
