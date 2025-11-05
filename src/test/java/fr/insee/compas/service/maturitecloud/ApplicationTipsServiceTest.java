package fr.insee.compas.service.maturitecloud;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.compas.exception.CsvSeparatorDetectionException;
import fr.insee.compas.model.compas.ApplicationTip;
import fr.insee.compas.repository.ApplicationTipsRepository;

@ExtendWith(MockitoExtension.class)
class ApplicationTipsServiceTest {

    @Mock ApplicationTipsRepository repo;

    @InjectMocks ApplicationTipsService service;

    // Utilitaire : prépare un MultipartFile dont getInputStream() renvoie 2 flux successifs :
    //  - stream1 : utilisé pour detectSeparator (on ne lit que la 1re ligne)
    //  - stream2 : utilisé pour le parsing complet du CSV
    private MultipartFile mockFile(String firstLineOnly, String fullContent) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream())
                .thenReturn(
                        new ByteArrayInputStream(firstLineOnly.getBytes(StandardCharsets.UTF_8)))
                .thenReturn(new ByteArrayInputStream(fullContent.getBytes(StandardCharsets.UTF_8)));
        return file;
    }

    @Test
    void importCsv_shouldThrow_whenSourceIdInvalid() {
        MultipartFile file = mock(MultipartFile.class);
        assertThatThrownBy(() -> service.importCsv(file, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sourceId doit être 1 (technique) ou 2 (orga)");
        verifyNoInteractions(repo);
    }

    @Test
    void importCsv_shouldReturn0_whenHeaderMissingOrEmpty() throws Exception {
        // première ligne (pour détection) + contenu complet identiques : uniquement un saut de
        // ligne
        String first =
                "nom;conseil\n"; // detecte ; mais on mettra un contenu vide/ sans header réel
        String full = ""; // readNext() => null => 0
        MultipartFile file = mockFile(first, full);

        int count = service.importCsv(file, 1);

        assertThat(count).isZero();
        verifyNoInteractions(repo);
    }

    @Test
    void importCsv_semicolon_withHeaderNormalization_andValuesParsed() throws Exception {
        // En-têtes volontairement variés : accents/espaces/underscore, etc.
        String header =
                "Nom Oscar;Conseil;Priorité;variable;modalité;contrib;answer;points appli;delta\n";
        // 1) ligne valide  2) ligne vide  3) ligne sans conseil (ignorée)
        String row1 = "Appli-X;Faire X rapidement;Haute;VAR1;MOD1;12,34;oui;5.5;0,1\n";
        String row2 = ";;;;;;; ;\n"; // vide -> ignorée
        String row3 =
                "Appli-Y;;Moyenne;VAR2;MOD2;not-a-number;non;1;2\n"; // sans conseil -> ignorée

        String firstLine = header;
        String full = header + row1 + row2 + row3;
        MultipartFile file = mockFile(firstLine, full);

        ArgumentCaptor<List<ApplicationTip>> cap = ArgumentCaptor.forClass(List.class);

        int saved = service.importCsv(file, 2);

        assertThat(saved).isEqualTo(1);
        verify(repo).saveAll(cap.capture());
        List<ApplicationTip> tips = cap.getValue();
        assertThat(tips).hasSize(1);

        ApplicationTip t = tips.get(0);
        assertThat(t.getNomOscar()).isEqualTo("Appli-X");
        assertThat(t.getSourceId()).isEqualTo((short) 2);
        assertThat(t.getConseil()).isEqualTo("Faire X rapidement");
        assertThat(t.getPriorite()).isEqualTo("Haute");
        assertThat(t.getVariable()).isEqualTo("VAR1");
        assertThat(t.getModalite()).isEqualTo("MOD1");
        assertThat(t.getContrib())
                .isEqualByComparingTo(new BigDecimal("12.34")); // virgule -> point
        assertThat(t.getAnswer()).isEqualTo("oui");
        assertThat(t.getPointsAppli()).isEqualByComparingTo(new BigDecimal("5.5"));
        assertThat(t.getDelta()).isEqualByComparingTo(new BigDecimal("0.1"));
        assertThat(t.getDate()).isNotNull(); // date du jour Europe/Paris
        assertThat(t.getCreatedAt()).isNotNull(); // OffsetDateTime Europe/Paris
    }

    @Test
    void importCsv_comma_separator_and_alternativeKeys_tipColumn() throws Exception {
        // utilise "tip" au lieu de "conseil", "nom_oscar" avec underscore, "points_appli" aussi
        String header = "nom_oscar,tip,points_appli,contrib,delta\n";
        String row1 = "APPLI-1,Astuce utile,10,0.5,1\n";

        MultipartFile file = mockFile(header, header + row1);

        ArgumentCaptor<List<ApplicationTip>> cap = ArgumentCaptor.forClass(List.class);

        int saved = service.importCsv(file, 1);

        assertThat(saved).isEqualTo(1);
        verify(repo).saveAll(cap.capture());
        ApplicationTip t = cap.getValue().get(0);
        assertThat(t.getNomOscar()).isEqualTo("APPLI-1");
        assertThat(t.getConseil()).isEqualTo("Astuce utile");
        assertThat(t.getPointsAppli()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(t.getContrib()).isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(t.getDelta()).isEqualByComparingTo(new BigDecimal("1"));
    }

    @Test
    void importCsv_tab_separator_and_missingRequiredFields_areSkipped() throws Exception {
        String header = "application\tconseil\tpriorite\n";
        String ok = "AppA\tFaire Y\tbasse\n";
        String bad1 = "\t\t\n"; // vide
        String bad2 = "AppB\t\tmoyenne\n"; // pas de conseil -> ignorée

        MultipartFile file = mockFile(header, header + ok + bad1 + bad2);

        int saved = service.importCsv(file, 1);

        assertThat(saved).isEqualTo(1);
        verify(repo).saveAll(any());
    }

    @Test
    void detectSeparator_shouldThrow_whenNoKnownSeparator() throws Exception {
        String firstLine = "Header sans separateur connu";
        MultipartFile file = mockFile(firstLine, firstLine + "\n"); // peu importe pour full ici

        assertThatThrownBy(() -> service.importCsv(file, 1))
                .isInstanceOf(CsvSeparatorDetectionException.class)
                .hasMessageContaining("Impossible de détecter le séparateur");
        verifyNoInteractions(repo);
    }

    @Test
    void importCsv_shouldNotSave_whenNoValidRows() throws Exception {
        String header = "nomoscar;conseil\n";
        String onlyInvalid = "    ;    \n"; // vide
        MultipartFile file = mockFile(header, header + onlyInvalid);

        int saved = service.importCsv(file, 1);

        assertThat(saved).isZero();
        verifyNoInteractions(repo);
    }
}
