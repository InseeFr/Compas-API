package fr.insee.compas.util;

import java.util.List;

public class MaturiteConstantes {
    public static final String SANS_OBJET = "SO";
    public static final String NON_RENSEIGNE = "NR";
    public static final String ECART_CIBLE_INIT = "oui";
    public static final Integer ID_INDICATEUR_ENV_CIBLE = 613;
    public static final Integer ID_INDICATEUR_STRAT_CLOUD = 614;
    public static final Integer ID_INDICATEUR_MATURITE = 601;
    public static final List<Integer> MATURITEINDICATEURSLIST =
            List.of(ID_INDICATEUR_ENV_CIBLE, ID_INDICATEUR_STRAT_CLOUD);

    public record ModuleInfo(
            String nameMod,
            String appName,
            String envActuelProd,
            String zoneProduction,
            String domaineSndi,
            String domaineDev,
            String domaineFonctionnel) {}

    private MaturiteConstantes() {}
}
