package fr.insee.compas.service.maturitecloud;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import fr.insee.compas.model.compas.ApplicationTip;
import fr.insee.compas.repository.ApplicationTipsRepository;

@Service
public class ApplicationTipsService {

    private final ApplicationTipsRepository repo;

    public ApplicationTipsService(ApplicationTipsRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public int importCsv(MultipartFile file, int sourceId) throws Exception {
        if (sourceId != 1 && sourceId != 2) {
            throw new IllegalArgumentException("sourceId doit être 1 (technique) ou 2 (orga)");
        }

        final LocalDate today = LocalDate.now(ZoneId.of("Europe/Paris"));
        final List<ApplicationTip> toSave = new ArrayList<>();

        // 1) Détection du séparateur
        char sep = detectSeparator(file);

        // 2) Parser OpenCSV configuré
        CSVParser parser =
                new CSVParserBuilder().withSeparator(sep).withIgnoreQuotations(false).build();

        try (Reader in = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVReader reader = new CSVReaderBuilder(in).withCSVParser(parser).build()) {

            String[] header = reader.readNext();
            if (header == null) return 0;

            // 3) Index des colonnes, normalisé (sans accents, minuscule, sans espaces/underscores)
            Map<String, Integer> idx = indexHeader(header);

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (isRowEmpty(row)) continue;

                String nomOscar =
                        firstNonBlank(row, idx, "nom_oscar", "nomoscar", "application", "appli");
                String conseil = firstNonBlank(row, idx, "conseil", "tip");

                if (isBlank(nomOscar) || isBlank(conseil)) {
                    // ligne incomplète → on ignore
                    continue;
                }

                ApplicationTip tip = new ApplicationTip();
                tip.setNomOscar(nomOscar.trim());
                tip.setDate(today); // 👈 date du jour (Europe/Paris)
                tip.setSourceId((short) sourceId);
                tip.setConseil(conseil.trim());

                tip.setPriorite(
                        blankToNull(firstNonBlank(row, idx, "priorite", "priorité", "prio")));
                tip.setVariable(blankToNull(firstNonBlank(row, idx, "variable")));
                tip.setModalite(blankToNull(firstNonBlank(row, idx, "modalite", "modalité")));
                tip.setContrib(parseBigDecimal(firstNonBlank(row, idx, "contrib")));
                tip.setAnswer(blankToNull(firstNonBlank(row, idx, "answer", "reponse", "réponse")));
                tip.setPointsAppli(
                        parseBigDecimal(
                                firstNonBlank(
                                        row, idx, "points_appli", "pointsappli", "points appli")));
                tip.setDelta(parseBigDecimal(firstNonBlank(row, idx, "delta")));
                tip.setCreatedAt(OffsetDateTime.now(ZoneId.of("Europe/Paris")));

                toSave.add(tip);
            }
        }

        if (!toSave.isEmpty()) {
            repo.saveAll(toSave);
        }
        return toSave.size();
    }

    // ---------- Helpers robustes ----------

    private static boolean isRowEmpty(String[] row) {
        for (String s : row) {
            if (s != null && !s.trim().isEmpty()) return false;
        }
        return true;
    }

    private static String key(String s) {
        if (s == null) return "";
        String k =
                Normalizer.normalize(s, Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", ""); // enlève les accents
        k = k.toLowerCase(Locale.ROOT).replaceAll("[\\s_]+", ""); // supprime espaces/underscore
        return k;
    }

    private static Map<String, Integer> indexHeader(String[] header) {
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            idx.put(key(header[i]), i);
        }
        return idx;
    }

    private static String firstNonBlank(String[] row, Map<String, Integer> idx, String... keys) {
        for (String k : keys) {
            Integer i = idx.get(k);
            if (i != null && i >= 0 && i < row.length) {
                String v = row[i];
                if (v != null && !v.trim().isEmpty()) return v.trim();
            }
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String blankToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }

    private static BigDecimal parseBigDecimal(String s) {
        if (isBlank(s)) return null;
        try {
            // accepte virgule décimale
            return new BigDecimal(s.trim().replace(',', '.'));
        } catch (Exception e) {
            return null;
        }
    }

    private static char detectSeparator(MultipartFile file) throws Exception {
        try (BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String first = br.readLine();
            if (first == null) return ',';
            // priorité au ; s'il est présent et plus fréquent
            long sc = first.chars().filter(c -> c == ';').count();
            long cc = first.chars().filter(c -> c == ',').count();
            long tc = first.chars().filter(c -> c == '\t').count();
            if (tc > sc && tc > cc) return '\t';
            if (sc > cc) return ';';
            return ',';
        }
    }
}
