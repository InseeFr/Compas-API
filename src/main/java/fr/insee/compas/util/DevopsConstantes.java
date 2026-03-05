package fr.insee.compas.util;

import java.util.List;

import lombok.Getter;

@Getter
public class DevopsConstantes {

    public static final String SANS_OBJET = "Sans objet";
    public static final String EMPTY = "";
    public static final String SAISIE_MANUELLE = "Saisie manuelle";
    public static final String EN_DEVELOPPEMENT = "en développement";
    public static final String EN_PRODUCTION = "en production";
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
    public static final String FIELD_AUTHOR_EMAIL = "author_email";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_AUTHOR_NAME = "author_name";
    public static final String FIELD_X_NEXT_PAGE = "X-Next-Page";
    public static final int DUPLICATE_OFFSET = 1000;

    public static final int MAX_BRANCHES = 100;
    public static final int MAX_COMMITS_PER_BRANCH = 100;
    public static final int MAX_COMMITS_PER_PAGE = 100;
    public static final String GITHUB_GRAPHQL_API_URL = "https://api.github.com/graphql";
    public static final String GITLAB_API_URL = "https://gitlab.insee.fr/api/v4";

    public static final List<String> INVALID_EMAIL_KEYWORDS =
            List.of("bot", "noreply", "maintenance", "intra");

    public static final List<String> INVALID_NAME_KEYWORDS = List.of("bot", "ci", "automation");

    private DevopsConstantes() {}
}
