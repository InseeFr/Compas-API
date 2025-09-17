package fr.insee.compas.service.maturitecloud;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
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
                    Map.entry("maturitecloud", 601),
                    Map.entry("maturite_cloud", 601),
                    Map.entry("robustesse", 602),
                    Map.entry("scorebenefice", 603),
                    Map.entry("score_benefice", 603),
                    Map.entry("score benefice", 603),
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

    /** Les flags maturite/robustesse sont ignorés : on insère toutes les colonnes présentes. */
    public int importCsv(MultipartFile file, boolean maturiteFlag, boolean robustesseFlag)
            throws Exception {
        int inserted = 0;

        try (BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // 1) Lire l'en-tête et détecter le séparateur
            String headerLine;
            do {
                headerLine = br.readLine();
            } while (headerLine != null && headerLine.isBlank());

            if (headerLine == null) {
                throw new IllegalArgumentException("CSV vide.");
            }

            char delimiter = detectDelimiter(headerLine);
            List<String> headers = parseCsvLine(headerLine, delimiter);
            Map<String, Integer> headerIndex = buildHeaderIndex(headers);

            if (!headerIndex.containsKey("id")) {
                throw new IllegalArgumentException(
                        "Colonne obligatoire manquante : 'id' (id_application).");
            }

            // 2) Parcourir les lignes de données
            List<Object[]> batch = new ArrayList<>(1024);
            String line;
            LocalDate today = LocalDate.now();

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                List<String> cols = parseCsvLine(line, delimiter);
                // Normaliser (taille de la ligne) en cas de colonnes manquantes
                if (cols.size() < headers.size()) {
                    // compléter avec vides
                    for (int i = cols.size(); i < headers.size(); i++) cols.add("");
                }

                Integer idApp = parseIdApplication(get(cols, headerIndex, "id"));
                if (idApp == null) {
                    // ligne ignorée si pas d'id application
                    continue;
                }

                LocalDate date = parseDate(getOpt(cols, headerIndex, "date"));
                if (date == null) date = today;

                String commentaire = getOpt(cols, headerIndex, "commentaire");

                // Pour chaque indicateur connu, si la colonne correspondante est présente on insère
                for (Map.Entry<String, Integer> e : HEADER_TO_INDICATEUR.entrySet()) {
                    String logicalHeader = e.getKey();
                    Integer indicateurId = e.getValue();

                    String rawVal = getOpt(cols, headerIndex, logicalHeader);
                    if (rawVal == null || rawVal.isBlank()) continue;

                    BigDecimal val = parseToDecimal(logicalHeader, rawVal);
                    if (val == null) continue;

                    batch.add(
                            new Object[] {
                                indicateurId,
                                Date.valueOf(date),
                                val.setScale(2, RoundingMode.HALF_UP),
                                SOURCE_ID,
                                idApp,
                                (commentaire == null || commentaire.isBlank()) ? null : commentaire
                            });
                }

                // Flush périodique
                if (batch.size() >= 1000) {
                    inserted += flushBatch(batch);
                }
            }

            // Flush final
            if (!batch.isEmpty()) {
                inserted += flushBatch(batch);
            }
        }

        log.info("Import CSV maturité cloud : {} lignes insérées dans table_faits.", inserted);
        return inserted;
    }

    /* =================== Helpers =================== */

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

    private static String get(List<String> row, Map<String, Integer> h, String logical) {
        Integer idx = h.get(normalize(logical));
        if (idx == null || idx >= row.size()) {
            throw new IllegalArgumentException("Colonne obligatoire manquante : " + logical);
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
            } catch (Exception ignored2) {
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
        switch (s) {
            case "A":
                return BigDecimal.valueOf(5);
            case "B":
                return BigDecimal.valueOf(4);
            case "C":
                return BigDecimal.valueOf(3);
            case "D":
                return BigDecimal.valueOf(2);
            case "E":
                return BigDecimal.valueOf(1);
            default:
                return null;
        }
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

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // guillemet échappé
                    cur.append('"');
                    i++; // skip second quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == delimiter && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
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
}
