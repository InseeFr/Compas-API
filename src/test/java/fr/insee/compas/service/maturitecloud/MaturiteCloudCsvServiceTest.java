package fr.insee.compas.service.maturitecloud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MaturiteCloudCsvServiceTest {

    @Mock private JdbcTemplate jdbcTemplate;

    private MaturiteCloudCsvService service;

    @BeforeEach
    void setUp() {
        service = new MaturiteCloudCsvService(jdbcTemplate);
    }

    @DisplayName("importCsv — cas de base paramétrés (compte des insertions)")
    @ParameterizedTest(name = "{index} – {0}: attendu={2}")
    @MethodSource("csvCases")
    void importCsv_parametrized_countInserted(String label, String csv, int expectedInserted)
            throws Exception {
        when(jdbcTemplate.batchUpdate(anyString(), anyList()))
                .thenAnswer(
                        inv -> {
                            @SuppressWarnings("unchecked")
                            var batch = (java.util.List<Object[]>) inv.getArgument(1);
                            int[] res = new int[batch.size()];
                            java.util.Arrays.fill(res, 1);
                            return res;
                        });

        MultipartFile file =
                new MockMultipartFile(
                        "file", "test.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        int result = service.importCsv(file);
        assertThat(result).isEqualTo(expectedInserted);
    }

    private static Stream<Arguments> csvCases() {
        return Stream.of(
                // 2 lignes × 2 indicateurs (maturite + robustesse) = 4 insertions
                Arguments.of(
                        "basic semicolon",
                        """
                        id;maturite;robustesse;date
                        100;A;4.5;2025-01-15
                        200;B;3.2;2025-01-16
                        """,
                        4),

                // lignes blanches ignorées ; 2 lignes × 1 indicateur (maturite) = 2 insertions
                Arguments.of(
                        "blank lines skipped",
                        """
                        id;maturite

                        100;5

                        200;4
                        """,
                        2),

                // 1 ligne × 2 indicateurs = 2 insertions
                Arguments.of(
                        "semicolon delimiter",
                        """
                        id;maturite;robustesse
                        100;5;3.5
                        """,
                        2),

                // 1 ligne × 2 indicateurs = 2 insertions
                Arguments.of(
                        "comma delimiter",
                        """
                        id,maturite,robustesse
                        100,5,3.5
                        """,
                        2),

                // première ligne sans id ignorée ; 1 ligne valide × 1 indicateur = 1 insertion
                Arguments.of(
                        "missing id -> skip row",
                        """
                        id;maturite
                        ;5
                        100;4
                        """,
                        1),

                // première ligne id invalide ignorée ; 1 ligne valide × 1 indicateur = 1 insertion
                Arguments.of(
                        "invalid id -> skip row",
                        """
                        id;maturite
                        invalid;5
                        100;4
                        """,
                        1));
    }
}
