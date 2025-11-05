package fr.insee.compas.service.maturitecloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaturiteCloudCsvService {

    private final JdbcTemplate jdbc;

    private static final int SOURCE_ID = 2;
    private static final int BATCH_SIZE = 1000;

    // SQL d'insert
    private static final String INSERT_SQL =
            """
            INSERT INTO table_faits
                (id_indicateur, "date", valeur, id_source, id_application, id_module, commentaire)
            VALUES (?, ?, ?, ?, ?, NULL, ?)
            """;

    // Mapping en-tête → id_indicateur
    private static final Map<String, Integer> HEADER_TO_INDICATEUR =
            Map.ofEntries(
                    Map.entry("maturite", 601),
                    Map.entry("robustesse", 602),
                    Map.entry("score_benefice", 603),
                    Map.entry("scoreorga", 604),
                    Map.entry("score_orga", 604),
                    Map.entry("score orga", 604),
                    Map.entry("scorecomplexite", 605),
                    Map.entry("score_complexite", 605),
                    Map.entry("score complexite", 605),
                    Map.entry("scoretechnique", 606),
                    Map.entry("score_technique", 606),
                    Map.entry("score technique", 606),
                    Map.entry("progressiondeploy", 607),
                    Map.entry("progression_deploy", 607),
                    Map.entry("progression deploy", 607),
                    Map.entry("progressiontechnos", 608),
                    Map.entry("progression_technos", 608),
                    Map.entry("progression technos", 608),
                    Map.entry("progressionarchi", 609),
                    Map.entry("progression_archi", 609),
                    Map.entry("progression archi", 609),
                    Map.entry("progressionmateqip", 610),
                    Map.entry("progression_mateqip", 610),
                    Map.entry("progression mateqip", 610),
                    Map.entry("progressiondevops", 611),
                    Map.entry("progression_devops", 611),
                    Map.entry("progression devops", 611),
                    Map.entry("progressioncloud", 612),
                    Map.entry("progression_cloud", 612),
                    Map.entry("progression cloud", 612));

    // en haut de la classe
    private static final Map<String, Integer> NORM_HEADER_TO_INDICATEUR;

    static {
        Map<String, Integer> tmp = new HashMap<>();
        for (Map.Entry<String, Integer> e : HEADER_TO_INDICATEUR.entrySet()) {
            tmp.put(normalize(e.getKey()), e.getValue());
        }
        NORM_HEADER_TO_INDICATEUR = Collections.unmodifiableMap(tmp);
    }

    /** Les flags maturite/robustesse sont ignorés : on insère toutes les colonnes présentes. */
    public int importCsv(MultipartFile file) throws Exception {
        try (BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            HeaderInfo header = readHeader(br);
            validateMandatoryColumns(header.headerIndex());

            List<Object[]> batch = new ArrayList<>(1024);
            LocalDate today = LocalDate.now();
            int inserted = 0;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                addRowToBatch(line, header, today, batch);
                inserted += flushIfNeeded(batch);
            }
            inserted += flushIfRemaining(batch);

            log.info("Import CSV maturité cloud : {} lignes insérées dans table_faits.", inserted);
            return inserted;
        }
    }

    /* =================== Refactored helpers =================== */

    private record HeaderInfo(
            char delimiter, List<String> headers, Map<String, Integer> headerIndex) {}

    private HeaderInfo readHeader(BufferedReader br) throws EmptyCsvException, IOException {
        String headerLine = readFirstNonBlankLine(br);
        if (headerLine == null) {
            throw new EmptyCsvException("CSV vide.");
        }

        char delimiter = detectDelimiter(headerLine);
        List<String> headers = parseCsvLine(headerLine, delimiter);
        Map<String, Integer> headerIndex = buildHeaderIndex(headers);
        return new HeaderInfo(delimiter, headers, headerIndex);
    }

    private String readFirstNonBlankLine(BufferedReader br) throws IOException {
        String line;
        do {
            line = br.readLine();
        } while (line != null && line.isBlank());
        return line;
    }

    private void validateMandatoryColumns(Map<String, Integer> headerIndex) {
        if (!headerIndex.containsKey(normalize("id"))) {
            throw new IllegalArgumentException(
                    "Colonne obligatoire manquante : 'id' (id_application).");
        }
    }

    private void addRowToBatch(
            String line, HeaderInfo header, LocalDate today, List<Object[]> batch) {
        List<String> cols = parseCsvLine(line, header.delimiter());
        padToHeaderSize(cols, header.headers().size());

        Integer idApp = parseIdApplication(get(cols, header.headerIndex()));
        if (idApp == null) return;

        LocalDate date =
                Optional.ofNullable(parseDate(getOpt(cols, header.headerIndex(), "date")))
                        .orElse(today);
        String commentaire = getOpt(cols, header.headerIndex(), "commentaire");
        addIndicatorsToBatch(cols, header.headerIndex(), date, idApp, commentaire, batch);
    }

    private void padToHeaderSize(List<String> cols, int headersSize) {
        for (int i = cols.size(); i < headersSize; i++) cols.add("");
    }

    private void addIndicatorsToBatch(
            List<String> cols,
            Map<String, Integer> headerIndex,
            LocalDate date,
            Integer idApp,
            String commentaire,
            List<Object[]> batch) {

        for (String presentNormHeader : headerIndex.keySet()) {
            Integer indicId = NORM_HEADER_TO_INDICATEUR.get(presentNormHeader);
            String rawVal = (indicId != null) ? getOpt(cols, headerIndex, presentNormHeader) : null;
            BigDecimal val =
                    (rawVal != null && !rawVal.isBlank())
                            ? parseToDecimal(presentNormHeader, rawVal)
                            : null;

            if (indicId == null || rawVal == null || rawVal.isBlank() || val == null) {
                continue;
            }

            batch.add(
                    new Object[] {
                        indicId,
                        java.sql.Date.valueOf(date),
                        val.setScale(2, RoundingMode.HALF_UP),
                        SOURCE_ID,
                        idApp,
                        (commentaire == null || commentaire.isBlank()) ? null : commentaire
                    });
        }
    }

    private int flushIfNeeded(List<Object[]> batch) {
        if (batch.size() < BATCH_SIZE) return 0;
        return flushBatch(batch);
    }

    private int flushIfRemaining(List<Object[]> batch) {
        return batch.isEmpty() ? 0 : flushBatch(batch);
    }

    /* =================== Existing helpers (unchanged) =================== */

    private int flushBatch(List<Object[]> batch) {
        int[] res = jdbc.batchUpdate(INSERT_SQL, batch);
        batch.clear();
        int sum = 0;
        for (int r : res) sum += r;
        return sum;
    }

    private static Map<String, Integer> buildHeaderIndex(List<String> headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            map.put(normalize(headers.get(i)), i);
        }
        return map;
    }

    private static String get(List<String> row, Map<String, Integer> h) {
        Integer idx = h.get(normalize("id"));
        if (idx == null || idx >= row.size()) {
            throw new IllegalArgumentException("Colonne obligatoire manquante : " + "id");
        }
        return row.get(idx);
    }

    private static String getOpt(List<String> row, Map<String, Integer> h, String logical) {
        Integer idx = h.get(normalize(logical));
        if (idx == null || idx >= row.size()) return null;
        return row.get(idx);
    }

    private static Integer parseIdApplication(String v) {
        if (v == null) return null;
        String s = v.trim().replace(",", "").replace(" ", "");
        if (s.isEmpty()) return null;
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate parseDate(String v) {
        if (v == null || v.isBlank()) return null;
        String s = v.trim();
        try {
            return LocalDate.parse(s); // yyyy-MM-dd
        } catch (DateTimeParseException ignored) {
            // dd/MM/yyyy
            try {
                String[] p = s.split("/");
                if (p.length == 3) {
                    int d = Integer.parseInt(p[0]);
                    int m = Integer.parseInt(p[1]);
                    int y = Integer.parseInt(p[2]);
                    return LocalDate.of(y, m, d);
                }
            } catch (Exception e) {
                return null; // parsing échoué
            }
            return null;
        }
    }

    /**
     * Convertit une valeur CSV en BigDecimal. - Supporte virgule ou point comme séparateur décimal,
     * et retire %. - Pour "maturité" textuelle (A..E), mapping A→5, B→4, C→3, D→2, E→1.
     */
    private static BigDecimal parseToDecimal(String headerKey, String raw) {
        if (raw == null) return null;
        String s = raw.trim();

        if (isMaturiteHeader(headerKey)) {
            BigDecimal mapped = mapMaturiteAlpha(s);
            if (mapped != null) return mapped;
        }

        s = s.replace("\u00A0", ""); // espace insécable
        s = s.replace(" ", "");
        s = s.replace("%", "");
        s = s.replace(",", "."); // virgule -> point
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isMaturiteHeader(String headerKey) {
        String k = normalize(headerKey);
        return k.equals("maturite") || k.equals("maturitecloud") || k.equals("maturite_cloud");
    }

    private static BigDecimal mapMaturiteAlpha(String v) {
        String s = v.trim().toUpperCase(Locale.ROOT);
        return switch (s) {
            case "A" -> BigDecimal.valueOf(5);
            case "B" -> BigDecimal.valueOf(4);
            case "C" -> BigDecimal.valueOf(3);
            case "D" -> BigDecimal.valueOf(2);
            case "E" -> BigDecimal.valueOf(1);
            default -> null;
        };
    }

    private static char detectDelimiter(String headerLine) {
        long semi = headerLine.chars().filter(c -> c == ';').count();
        long comma = headerLine.chars().filter(c -> c == ',').count();
        return (semi > comma) ? ';' : ',';
    }

    /**
     * Parse une ligne CSV en respectant les guillemets et le séparateur. Supporte les guillemets
     * échappés par doublement (""). Hypothèse : pas de retours à la ligne à l'intérieur d'un champ.
     */
    private static List<String> parseCsvLine(String line, char delimiter) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        int i = 0;
        while (i < line.length()) {
            char ch = line.charAt(i);

            if (ch == '"') {
                // Si on est dans des guillemets et que le prochain caractère est aussi un guillemet
                // → guillemet échappé
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i += 2; // on saute les deux guillemets
                    continue;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == delimiter && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
            i++;
        }

        out.add(cur.toString());
        return out;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase(Locale.ROOT);
        t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return t.replaceAll("[\\s_\\-./\\\\'’\"()\\[\\]]", "");
    }

    public class EmptyCsvException extends RuntimeException {
        public EmptyCsvException(String message) {
            super(message);
        }

        public EmptyCsvException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
