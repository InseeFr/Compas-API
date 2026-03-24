package fr.insee.compas.service.devops;

import static fr.insee.compas.util.DevopsConstantes.DUPLICATE_OFFSET;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.UtilsService;
import fr.insee.compas.service.gitservice.GithubService;
import fr.insee.compas.service.gitservice.IGitlabService;
import fr.insee.compas.util.DevopsConstantes;
import fr.insee.compas.util.IndicatorSpecialValue;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsable de la mise à jour en base des indicateurs DevOps (jours depuis dernière MEP,
 * nombre de déploiements, nombre de contributions).
 *
 * <p>Les indicateurs sont calculés à deux niveaux :
 *
 * <ul>
 *   <li>par module applicatif
 *   <li>par application (moyenne arrondie des modules)
 * </ul>
 *
 * <p>Les résultats sont persistés en base via {@link TableFaitsRepository}.
 */
@Service
@Slf4j
public class UpdateIndicatorDevopsService {

    private final OscarService oscarService;
    private final TableFaitsRepository tableFaitsRepository;
    private final IGitlabService gitlabService;
    private final GithubService githubService;
    private final UtilsService utilsService;

    @Autowired
    public UpdateIndicatorDevopsService(
            TableFaitsRepository tableFaitsRepository,
            OscarService oscarService,
            IGitlabService gitlabService,
            GithubService githubService,
            UtilsService utilsService) {
        this.tableFaitsRepository = tableFaitsRepository;
        this.oscarService = oscarService;
        this.gitlabService = gitlabService;
        this.githubService = githubService;
        this.utilsService = utilsService;
    }

    /**
     * Lance la mise à jour des trois indicateurs principaux :
     *
     * <ul>
     *   <li>{@link IndicateurType#NBR_JOUR_MEP}
     *   <li>{@link IndicateurType#DEPLOYMENT_COUNT}
     *   <li>{@link IndicateurType#NBR_CONTRIBUTIONS_PROJET}
     * </ul>
     *
     * @param startDate borne de début pour les indicateurs temporels
     * @param endDate borne de fin pour les indicateurs temporels
     */
    public void miseAJourIndicateursDevopsEnBaseDeDonnes(
            LocalDateTime startDate, LocalDateTime endDate) {

        log.info("****** Début mise à jour NBR_JOUR_MEP ********");
        updateNbrJourMep();
        log.info("****** Fin mise à jour NBR_JOUR_MEP ********");

        log.info("****** Début mise à jour DEPLOYMENT_COUNT ********");
        updateDeploymentCount(startDate, endDate);
        log.info("****** Fin mise à jour DEPLOYMENT_COUNT ********");

        log.info("****** Début mise à jour NBR_CONTRIBUTIONS_PROJET ********");
        updateContributorCount(startDate, endDate);
        log.info("****** Fin mise à jour NBR_CONTRIBUTIONS_PROJET ********");
    }

    /**
     * Met à jour l’indicateur {@link IndicateurType#NBR_JOUR_MEP}, représentant le nombre de jours
     * écoulés depuis la dernière mise en production d’un module.
     *
     * <p>Si aucune mise en production n’a été effectuée ou si le module n’est pas concerné, une
     * valeur spéciale de {@link IndicatorSpecialValue} est utilisée :
     *
     * <ul>
     *   <li>{@link IndicatorSpecialValue#NR} — si la source de création est manuelle ou si aucune
     *       date de mise en production n’est disponible.
     *   <li>{@link IndicatorSpecialValue#SO} — si le module est encore en développement.
     * </ul>
     */
    private void updateNbrJourMep() {
        updateIndicatorsForModulesAndOptionallyApplications(
                IndicateurType.NBR_JOUR_MEP,
                (module, unused) -> {
                    if (DevopsConstantes.SAISIE_MANUELLE.equals(module.getSourceCreation())) {
                        return IndicatorSpecialValue.NR.getCode();
                    }

                    if (DevopsConstantes.EN_DEVELOPPEMENT.equals(module.getStatut())) {
                        return IndicatorSpecialValue.SO.getCode();
                    }

                    LocalDate dateLivraison = module.getDateDerniereLivraisonEnProduction();
                    return (dateLivraison == null)
                            ? IndicatorSpecialValue.NR.getCode()
                            : (int) ChronoUnit.DAYS.between(dateLivraison, LocalDate.now());
                },
                true);
    }

    /**
     * Met à jour l’indicateur {@link IndicateurType#DEPLOYMENT_COUNT} correspondant au nombre de
     * déploiements effectués pour chaque module entre deux dates.
     *
     * <p>Règles spécifiques :
     *
     * <ul>
     *   <li>Si le module a été créé manuellement ({@link DevopsConstantes#SAISIE_MANUELLE}),
     *       l’indicateur retourne {@link IndicatorSpecialValue#NR}.
     *   <li>Si le module est en développement ({@link DevopsConstantes#EN_DEVELOPPEMENT}),
     *       l’indicateur retourne {@link IndicatorSpecialValue#SO}.
     *   <li>Si l’historique des déploiements est absent pour le module, retourne {@link
     *       IndicatorSpecialValue#NR}.
     *   <li>Sinon, retourne le nombre de déploiements valides dans la période spécifiée.
     * </ul>
     *
     * @param startDate borne de début pour le calcul des déploiements
     * @param endDate borne de fin pour le calcul des déploiements
     */
    private void updateDeploymentCount(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime[] dates = normalizeDates(startDate, endDate);
        Map<String, List<ModuleHistorique>> allHistoriqueMap = oscarService.getModulesHistorique();

        updateIndicatorsForModulesAndOptionallyApplications(
                IndicateurType.DEPLOYMENT_COUNT,
                (module, unused) -> {
                    List<ModuleHistorique> historique =
                            allHistoriqueMap.get(String.valueOf(module.getId()));

                    if (DevopsConstantes.SAISIE_MANUELLE.equals(module.getSourceCreation())) {
                        return IndicatorSpecialValue.NR.getCode();
                    }

                    if (DevopsConstantes.EN_DEVELOPPEMENT.equals(module.getStatut())) {
                        return IndicatorSpecialValue.SO.getCode();
                    }

                    return (historique == null)
                            ? IndicatorSpecialValue.NR.getCode()
                            : (int)
                                    historique.stream()
                                            .filter(h -> isValidDeployment(h, dates[0], dates[1]))
                                            .count();
                },
                true);
    }

    /**
     * Met à jour l’indicateur {@link IndicateurType#NBR_CONTRIBUTIONS_PROJET}, représentant le
     * nombre de contributeurs uniques ayant effectué des commits sur la période spécifiée.
     *
     * <p>Cette méthode calcule, pour chaque module, le nombre de contributeurs uniques de son
     * dépôt. Elle applique ensuite une valeur spéciale ou un décalage (offset) dans les cas
     * particuliers :
     *
     * <ul>
     *   <li>Si le module n’a pas d’URL de dépôt, la valeur {@link IndicatorSpecialValue#NR} est
     *       utilisée.
     *   <li>Si l’URL est marquée comme "sans objet", la valeur {@link IndicatorSpecialValue#SO} est
     *       utilisée.
     *   <li>Si plusieurs modules partagent le même dépôt, un offset est ajouté pour signaler un
     *       doublon.
     * </ul>
     *
     * <p>Après la mise à jour des indicateurs pour chaque module, la méthode appelle {@link
     * #updateContributorAverageByApplication(Map)} pour calculer et enregistrer la valeur moyenne
     * des contributeurs uniques par application.
     *
     * @param startDate borne de début pour le calcul des contributions
     * @param endDate borne de fin pour le calcul des contributions
     */
    private void updateContributorCount(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime[] dates = normalizeDates(startDate, endDate);
        List<Module> modules = oscarService.getModules();
        if (modules == null || modules.isEmpty()) return;

        // Comptage des occurrences des URLs par application
        Map<Integer, Map<String, Long>> urlCountParApplication =
                modules.stream()
                        .filter(m -> m.getUrlCodeSource() != null)
                        .collect(
                                Collectors.groupingBy(
                                        Module::getIdApplication,
                                        Collectors.groupingBy(
                                                Module::getUrlCodeSource, Collectors.counting())));

        Map<String, Integer> cacheAuthors = new HashMap<>();
        Map<Integer, Set<Integer>> appUniqueRepoValues = new HashMap<>();

        // Fonction de calcul par module
        BiFunction<Module, Void, Integer> valueProvider =
                (module, unused) -> {
                    String url = module.getUrlCodeSource();
                    if (url == null) return IndicatorSpecialValue.NR.getCode();
                    if (DevopsConstantes.EMPTY.equals(url))
                        return IndicatorSpecialValue.NR.getCode();
                    if (DevopsConstantes.SANS_OBJET.equals(url))
                        return IndicatorSpecialValue.SO.getCode();

                    long occurrences =
                            urlCountParApplication
                                    .getOrDefault(module.getIdApplication(), Map.of())
                                    .getOrDefault(url, 0L);

                    String cacheKey = module.getIdApplication() + "::" + url;
                    int uniqueAuthors =
                            cacheAuthors.computeIfAbsent(
                                    cacheKey, k -> getUniqueAuthorCount(url, dates[0], dates[1]));

                    appUniqueRepoValues
                            .computeIfAbsent(module.getIdApplication(), k -> new HashSet<>())
                            .add(uniqueAuthors);

                    if (occurrences >= 2 && uniqueAuthors >= 0) {
                        return uniqueAuthors + DUPLICATE_OFFSET;
                    } else {
                        return uniqueAuthors;
                    }
                };

        // Mise à jour des modules
        updateIndicatorsForModulesAndOptionallyApplications(
                IndicateurType.NBR_CONTRIBUTIONS_PROJET, valueProvider, false);

        // Calcul et sauvegarde de la moyenne par application
        updateContributorAverageByApplication(appUniqueRepoValues);
    }

    /**
     * Calcule et enregistre la valeur moyenne du nombre de contributeurs uniques pour chaque
     * application, à partir des valeurs uniques des modules précédemment calculées.
     *
     * <p>Pour chaque application :
     *
     * <ul>
     *   <li>Les valeurs des modules sont nettoyées pour retirer les offsets liés aux dépôts
     *       partagés,
     *   <li>La moyenne est calculée selon des règles de gestion spécifiques (NR, SO ou moyenne
     *       arrondie),
     *   <li>Le résultat est ensuite enregistré via {@link #saveIndicator(Integer, Integer,
     *       IndicateurType, BigDecimal)}.
     * </ul>
     *
     * @param appUniqueRepoValues mapping associant l’identifiant d’une application à l’ensemble de
     *     ses valeurs uniques (avec ou sans offset)
     */
    private void updateContributorAverageByApplication(
            Map<Integer, Set<Integer>> appUniqueRepoValues) {
        for (Application app : oscarService.getApplications()) {
            Set<Integer> uniqueValues = appUniqueRepoValues.get(app.getIdApplication());
            int avg = computeAverage(uniqueValues);
            saveIndicator(
                    null,
                    app.getIdApplication(),
                    IndicateurType.NBR_CONTRIBUTIONS_PROJET,
                    BigDecimal.valueOf(avg));
        }
    }

    /**
     * Calcule la valeur moyenne du nombre de contributeurs uniques pour une application donnée.
     *
     * <p>Règles de calcul :
     *
     * <ul>
     *   <li>Si la liste est vide ou nulle → renvoie {@link IndicatorSpecialValue#SO},
     *   <li>Si toutes les valeurs sont {@link IndicatorSpecialValue#NR} → renvoie NR,
     *   <li>Si toutes les valeurs sont NR ou SO, et qu’au moins une est SO → renvoie SO,
     *   <li>Sinon, calcule la moyenne arrondie via {@link #calculateRoundedAverage(List)}.
     * </ul>
     *
     * <p>Avant le calcul, les offsets liés aux doublons sont retirés.
     *
     * @param uniqueValues ensemble des valeurs uniques (avec ou sans offset)
     * @return la valeur moyenne ou un code spécial (NR ou SO)
     */
    private int computeAverage(Set<Integer> uniqueValues) {
        if (uniqueValues == null || uniqueValues.isEmpty()) {
            return IndicatorSpecialValue.SO.getCode();
        }

        // Retire les offsets pour les doublons
        List<Integer> values =
                uniqueValues.stream()
                        .map(v -> (v >= DUPLICATE_OFFSET ? v - DUPLICATE_OFFSET : v))
                        .toList();

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

        return calculateRoundedAverage(values);
    }

    /**
     * Met à jour un indicateur DevOps pour chaque module, et éventuellement pour les applications.
     *
     * <p>Cette méthode parcourt tous les modules et calcule la valeur de l’indicateur via la
     * fonction {@code valueProvider}. Elle sauvegarde ensuite cette valeur pour chaque module. Si
     * {@code includeApplication} est vrai, elle calcule ensuite la moyenne arrondie des modules par
     * application et sauvegarde un seul indicateur pour l’application.
     *
     * <p>Cette approche permet d’éviter de sauvegarder des indicateurs d’application incorrects si
     * certains modules partagent un même dépôt (comme c’est le cas pour le nombre de contributeurs
     * uniques).
     *
     * @param type type d’indicateur à calculer (ex: {@link IndicateurType#NBR_JOUR_MEP})
     * @param valueProvider fonction de calcul de la valeur par module, de type {@code
     *     BiFunction<Module, Void, Integer>}. Elle prend en paramètre un module et retourne la
     *     valeur de l’indicateur pour ce module.
     * @param includeApplication si vrai, calcule et sauvegarde également l’indicateur moyen au
     *     niveau application ; sinon, ne sauvegarde que les modules.
     */
    private void updateIndicatorsForModulesAndOptionallyApplications(
            IndicateurType type,
            BiFunction<Module, Void, Integer> valueProvider,
            boolean includeApplication) {

        List<Module> modules = oscarService.getModules();
        if (modules == null) return;

        Map<Integer, List<Integer>> valuesByApp = new HashMap<>();

        for (Module module : modules) {
            int value = valueProvider.apply(module, null);
            saveIndicator(
                    module.getId(), module.getIdApplication(), type, BigDecimal.valueOf(value));

            if (includeApplication) {
                valuesByApp
                        .computeIfAbsent(module.getIdApplication(), k -> new ArrayList<>())
                        .add(value);
            }
        }

        if (includeApplication) {
            List<Application> applications = oscarService.getApplications();
            if (applications == null) return;

            for (Application application : applications) {
                int avg = calculateRoundedAverage(valuesByApp.get(application.getIdApplication()));
                saveIndicator(null, application.getIdApplication(), type, BigDecimal.valueOf(avg));
            }
        }
    }

    /**
     * Vérifie si un enregistrement d’historique correspond à un déploiement valide (auteur attendu,
     * date dans l’intervalle, opération de type modification).
     *
     * @param h historique du module
     * @param start borne de début
     * @param end borne de fin
     * @return vrai si le déploiement est valide
     */
    private boolean isValidDeployment(ModuleHistorique h, LocalDateTime start, LocalDateTime end) {
        boolean isServiceAccount =
                h.getAuteurOperation() != null
                        && h.getAuteurOperation().endsWith(DevopsConstantes.SERVICE);
        return isServiceAccount
                && !h.getDateOperation().isBefore(start)
                && !h.getDateOperation().isAfter(end)
                && DevopsConstantes.MODIFICATION.equals(h.getOperation());
    }

    /**
     * Retourne le nombre d’auteurs uniques ayant contribué à un dépôt GitLab/GitHub.
     *
     * @param sourceUrl URL du dépôt
     * @param startDate borne de début
     * @param endDate borne de fin
     * @return nombre d’auteurs, ou valeur spéciale si erreur / dépôt inconnu
     */
    private int getUniqueAuthorCount(
            String sourceUrl, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            if (sourceUrl.contains("gitlab.insee.fr")) {
                String repoPath = utilsService.extractRepoPath(sourceUrl);
                String encodedPath = URLEncoder.encode(repoPath, StandardCharsets.UTF_8);
                return gitlabService
                        .getGitlabAuthorsForProject(encodedPath, startDate, endDate)
                        .size();
            } else if (sourceUrl.contains("github.com")) {
                String[] parts = sourceUrl.split("github.com/")[1].split("/");
                if (parts.length < 2) {
                    return IndicatorSpecialValue.NR.getCode();
                }

                return githubService
                        .getGithubAuthorsForRepo(parts[0], parts[1], startDate, endDate)
                        .size();
            } else {
                return IndicatorSpecialValue.SO.getCode();
            }

        } catch (IllegalArgumentException e) {
            log.warn("Scan ignoré (hors périmètre) pour l'URL {} : {}", sourceUrl, e.getMessage());
            return IndicatorSpecialValue.SO.getCode();

        } catch (IOException e) {
            log.error("Erreur réseau lors récupération auteurs: {}", sourceUrl, e);
            return IndicatorSpecialValue.NR.getCode();
        }
    }

    /**
     * Sauvegarde un indicateur en base via {@link TableFaitsRepository}.
     *
     * @param idModule identifiant du module (ou null si indicateur au niveau application)
     * @param idApplication identifiant de l’application
     * @param type type d’indicateur
     * @param valeur valeur calculée
     */
    private void saveIndicator(
            Integer idModule, Integer idApplication, IndicateurType type, BigDecimal valeur) {
        TableFaits fait =
                TableFaits.builder()
                        .idModule(idModule)
                        .idApplication(idApplication)
                        .idIndicateur(type.getValue())
                        .valeur(valeur)
                        .idSource(SourceType.OSCAR.getValue())
                        .date(LocalDate.now())
                        .build();
        tableFaitsRepository.save(fait);
    }

    /**
     * Calcule la moyenne arrondie d'une liste de valeurs.
     *
     * <p>Règles spécifiques :
     *
     * <ul>
     *   <li>Si la liste est {@code null} ou vide, retourne {@link IndicatorSpecialValue#SO}.
     *   <li>Si toutes les valeurs sont {@link IndicatorSpecialValue#NR}, retourne {@link
     *       IndicatorSpecialValue#SO}.
     *   <li>Si toutes les valeurs sont {@link IndicatorSpecialValue#NR} ou {@link
     *       IndicatorSpecialValue#SO} et qu'il y a au moins un {@link IndicatorSpecialValue#SO},
     *       retourne {@link IndicatorSpecialValue#NR}.
     *   <li>Sinon, calcule la moyenne des valeurs >= 0 et retourne le résultat arrondi à l'entier
     *       le plus proche.
     * </ul>
     *
     * @param values la liste de valeurs à moyenner, peut contenir des codes spéciaux NR/SO
     * @return moyenne arrondie des valeurs valides, ou un code spécial SO/NR selon les règles
     */
    private int calculateRoundedAverage(List<Integer> values) {
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

    /**
     * Normalise les dates de début et de fin :
     *
     * <ul>
     *   <li>Si null → période par défaut : [il y a 1 mois ; aujourd’hui]
     *   <li>Si start > end → exception
     * </ul>
     *
     * @param start borne de début souhaitée (peut être null)
     * @param end borne de fin souhaitée (peut être null)
     * @return tableau [startNormalisée, endNormalisée]
     * @throws IllegalArgumentException si start > end
     */
    private LocalDateTime[] normalizeDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return new LocalDateTime[] {
                LocalDateTime.now().minusMonths(1), LocalDateTime.now().with(LocalTime.MAX)
            };
        }
        if (start.isAfter(end)) throw new IllegalArgumentException("startDate > endDate");
        return new LocalDateTime[] {start, end};
    }
}
