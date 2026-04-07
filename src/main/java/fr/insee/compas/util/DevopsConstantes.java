package fr.insee.compas.util;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import fr.insee.compas.model.oscar.ModuleHistorique;

import lombok.Getter;

@Getter
public class DevopsConstantes {

    public static final String SANS_OBJET = "Sans objet";
    public static final String EMPTY = "";
    public static final String SAISIE_MANUELLE = "Saisie manuelle";
    public static final String EN_DEVELOPPEMENT = "en développement";
    public static final String MODIFICATION = "MODIFICATION";
    public static final String SERVICE = "service";
    public static final String FIELD_DATA = "data";
    public static final String FIELD_REPOSITORY = "repository";
    public static final String FIELD_REFS = "refs";
    public static final String FIELD_NODES = "nodes";
    public static final String FIELD_TARGET = "target";
    public static final String FIELD_HISTORY = "history";
    public static final String FIELD_AUTHOR = "author";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_X_TOTAL_PAGE = "X-Total-Page";
    public static final int DUPLICATE_OFFSET = 1000;

    public static final int MAX_BRANCHES = 100;
    public static final int MAX_COMMITS_PER_BRANCH = 100;
    public static final int MAX_COMMITS_PER_PAGE = 100;
    public static final String GITHUB_GRAPHQL_API_URL = "https://api.github.com/graphql";
    public static final String GITLAB_API_URL = "https://gitlab.insee.fr/api/v4";
    // ajout de nouveaux keywords
    public static final List<String> INVALID_EMAIL_KEYWORDS =
            List.of("bot", "noreply", "maintenance", "intra", "runner@", "github-actions@");

    public static final List<String> INVALID_NAME_KEYWORDS = List.of("bot", "ci", "automation");

    public static boolean isValidDeployment(
            ModuleHistorique h, LocalDateTime start, LocalDateTime end) {
        boolean isServiceAccount =
                h.getAuteurOperation() != null
                        && h.getAuteurOperation().endsWith(DevopsConstantes.SERVICE);
        return isServiceAccount
                && !h.getDateOperation().isBefore(start)
                && !h.getDateOperation().isAfter(end)
                && DevopsConstantes.MODIFICATION.equals(h.getOperation());
    }

    public static final LocalDateTime[] normalizeDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return new LocalDateTime[] {
                LocalDateTime.now().minusMonths(1), LocalDateTime.now().with(LocalTime.MAX)
            };
        }
        if (start.isAfter(end)) throw new IllegalArgumentException("startDate > endDate");
        return new LocalDateTime[] {start, end};
    }

    public static final int calculateRoundedAverage(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return IndicatorSpecialValue.SO.getCode();
        }

        boolean allNR = values.stream().allMatch(v -> v == IndicatorSpecialValue.NR.getCode());
        boolean containsSO = values.stream().anyMatch(v -> v == IndicatorSpecialValue.SO.getCode());
        boolean allNRorSO =
                values.stream()
                        .allMatch(
                                v ->
                                        v == IndicatorSpecialValue.NR.getCode()
                                                || v == IndicatorSpecialValue.SO.getCode());

        if (allNR) {
            return IndicatorSpecialValue.NR.getCode();
        }

        if (allNRorSO && containsSO) {
            return IndicatorSpecialValue.SO.getCode();
        }

        List<Integer> filtered = values.stream().filter(v -> v >= 0).toList();

        if (filtered.isEmpty()) {
            return IndicatorSpecialValue.SO.getCode();
        }

        return (int) Math.round(filtered.stream().mapToInt(Integer::intValue).average().orElse(0));
    }

    private DevopsConstantes() {}
}
