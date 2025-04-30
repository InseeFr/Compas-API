package fr.insee.compas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.exception.CompasUploadException;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.repository.TableFaitsRepository;

@ExtendWith(MockitoExtension.class)
class FichierControlServiceTest {
    @InjectMocks private FichierControlService fichierControlService;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @Test
    void testIsFileDejaRecu_true() {
        final LocalDate date = LocalDate.of(2024, 3, 17);
        when(tableFaitsRepository.countGreenItValuesByDate(
                        date, IndicateurType.CONSO_ELEC.getValue()))
                .thenReturn(5);
        final boolean result = fichierControlService.isFileDejaRecu(date);
        assertTrue(result);
    }

    @Test
    void testIsFileDejaRecu_false() {
        final LocalDate date = LocalDate.of(2024, 3, 4);
        when(tableFaitsRepository.countGreenItValuesByDate(
                        date, IndicateurType.CONSO_ELEC.getValue()))
                .thenReturn(0);
        final boolean result = fichierControlService.isFileDejaRecu(date);
        assertFalse(result);
    }

    @Test
    void testValidFileName() {
        assertTrue(fichierControlService.isValidFileName("vm-metrique-20240327.csv"));
    }

    @Test
    void testNullFileName() {
        assertFalse(fichierControlService.isValidFileName(null));
    }

    @Test
    void testInvalidPrefix() {
        assertFalse(fichierControlService.isValidFileName("document-20240327.csv"));
    }

    @Test
    void testMissingDate() {
        assertFalse(fichierControlService.isValidFileName("vm-metrique-.csv"));
    }

    @Test
    void testShortDate() {
        assertFalse(fichierControlService.isValidFileName("vm-metrique-202403.csv"));
    }

    @Test
    void testWrongExtension() {
        assertFalse(fichierControlService.isValidFileName("vm-metrique-20240327.txt"));
    }

    @Test
    void testExtraCharacters() {
        assertFalse(fichierControlService.isValidFileName("vm-metrique-20240327-final.csv"));
    }

    @Test
    void testEmptyFileName() {
        assertFalse(fichierControlService.isValidFileName(""));
    }

    @Test
    void testExtractValidDate() {
        final LocalDate expectedDate = LocalDate.of(2024, 3, 27);
        final LocalDate result =
                fichierControlService.extractDateFromFileName("vm-metrique-20240327.csv");
        assertThat(result).isEqualTo(expectedDate);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                "doc-20240327.csv",
                "fichier-20240230.csv",
                "vm-metrique-20240327-extra.csv",
                "vm-metrique-202403.csv"
            })
    void testExtractDateReturnsNullForInvalidFilenames(String fileName) {
        assertThat(fichierControlService.extractDateFromFileName(fileName)).isNull();
    }

    @Test
    void testExtractDateInvalide() {
        final String invalidFileName = "vm-metrique-20240132.csv";
        final CompasUploadException exception =
                assertThrows(
                        CompasUploadException.class,
                        () -> {
                            fichierControlService.extractDateFromFileName(invalidFileName);
                        });
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(exception.getStatus()).isEqualTo(422);
        softAssertions.assertThat(exception.getErrorVM()).isNotNull();
        softAssertions.assertThat(exception.getErrorVM().getCle()).isEqualTo("date.formatInvalide");
        softAssertions.assertAll();
    }

    @Test
    void testControlFileName_Success() {
        final String validFileName = "vm-metrique-20240101.csv";
        assertThat(fichierControlService.controlFileName(validFileName))
                .isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    void testControlFileName_InvalidFileName() {
        final String invalidFileName = "invalid-name.csv";

        final CompasUploadException exception =
                assertThrows(
                        CompasUploadException.class,
                        () -> {
                            fichierControlService.controlFileName(invalidFileName);
                        });
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(exception.getStatus()).isEqualTo(422);
        softAssertions.assertThat(exception.getErrorVM()).isNotNull();
        softAssertions.assertThat(exception.getErrorVM().getCle()).isEqualTo("fichier.nomInvalide");
        softAssertions.assertAll();
    }

    @Test
    void testControlFileName_InvalidDate() {
        final String invalidDateFile = "vm-metrique-xxxxxx.csv";

        final CompasUploadException exception =
                assertThrows(
                        CompasUploadException.class,
                        () -> {
                            fichierControlService.controlFileName(invalidDateFile);
                        });
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(exception.getStatus()).isEqualTo(422);
        softAssertions.assertThat(exception.getErrorVM()).isNotNull();
        softAssertions.assertThat(exception.getErrorVM().getCle()).isEqualTo("fichier.nomInvalide");
        softAssertions.assertAll();
    }

    @Test
    void testControlFileName_FileAlreadyReceived() {
        final String validFileName = "vm-metrique-20240101.csv";
        when(tableFaitsRepository.countGreenItValuesByDate(
                        LocalDate.of(2024, 1, 1), IndicateurType.CONSO_ELEC.getValue()))
                .thenReturn(1);
        final CompasUploadException exception =
                assertThrows(
                        CompasUploadException.class,
                        () -> {
                            fichierControlService.controlFileName(validFileName);
                        });
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(exception.getStatus()).isEqualTo(400);
        softAssertions.assertThat(exception.getErrorVM()).isNotNull();
        softAssertions.assertThat(exception.getErrorVM().getCle()).isEqualTo("fichier.dejaReçu");
        softAssertions.assertAll();
    }
}
