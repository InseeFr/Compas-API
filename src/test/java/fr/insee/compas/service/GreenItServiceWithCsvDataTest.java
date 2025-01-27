package fr.insee.compas.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.compas.mapper.MetriqueVmMapper;
import fr.insee.compas.model.greenit.MetriqueVmCsvRead;
import fr.insee.compas.repository.TableFaitsRepository;

@ExtendWith(MockitoExtension.class)
class GreenItServiceWithCsvDataTest {

    @InjectMocks private GreenItService greenItService;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @Mock private MetriqueVmMapper metriqueVmMapper;

    @Test
    void testSaveCsvData_FichierValide() throws IOException {
        final Path csvPath = Path.of("src/test/resources/metrique-vm.csv");
        final MultipartFile file = new MockMultipartFile("file", Files.readAllBytes(csvPath));

        final List<MetriqueVmCsvRead> metricsFromCsv = greenItService.loadCSVData(file);

        assertNotNull(metricsFromCsv);
        assertEquals(5, metricsFromCsv.size());
    }

    @Test
    void testSaveCsvData_FichierInvalide() throws IOException {
        final Path csvPath = Path.of("src/test/resources/metrique-failed.csv");
        final MultipartFile file = new MockMultipartFile("file", Files.readAllBytes(csvPath));

        assertThatThrownBy(() -> greenItService.loadCSVData(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erreur lors de la lecture");
    }

    @Test
    void testMiseAJourIndicateursGreenItFromFile_FichierInvalide() throws IOException {
        final Path csvPath = Path.of("src/test/resources/metrique-failed.csv");
        final MultipartFile file = new MockMultipartFile("file", Files.readAllBytes(csvPath));
        assertThatThrownBy(() -> greenItService.miseAJourIndicateursGreenItFromFile(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erreur lors de la lecture");
    }
}
