package fr.insee.compas.util;

import java.util.List;

public class GitLabConstantes {
    private GitLabConstantes() {}

    public static final List<String> INDICATEURS_MD =
            List.of(
                    "indicateurs-generaux",
                    "indicateurs-securite",
                    "indicateurs-qualite",
                    "indicateurs-devops",
                    "indicateurs-greenIt",
                    "indicateurs-meteo",
                    "indicateurs-a11y",
                    "indicateurs-maturite",
                    "NR-et-SO");

    public static final Integer ID_PROJET_COMPAS_API = 15164;
    public static final Integer ID_PROJET_COMPAS_UI = 21127;
}
