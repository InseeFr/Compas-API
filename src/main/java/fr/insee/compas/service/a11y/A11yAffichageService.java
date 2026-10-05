package fr.insee.compas.service.a11y;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.a11y.IndicateursModuleA11Y;
import fr.insee.compas.model.a11y.InfosSaisiesA11yEntity;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.service.conversion.ConversionService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class A11yAffichageService {

    private OscarService oscarService;
    private A11yMajService a11yMajService;
    private TableFaitsService tableFaitsService;
    private ConversionService convertService;

    public List<IndicateursModuleA11Y> listerModulesIHMA11y() {
        Map<Integer, InfosSaisiesA11yEntity> mapMetric = a11yMajService.getIndicateutA11y();
        Map<Integer, TableFaits> mapIssueSonar =
                tableFaitsService.getMapMetricByModule(
                        IndicateurType.ISSUE_ACCESSIBILITY.getValue());

        return oscarService.getModulesIhm().stream()
                .map(
                        module -> {
                            IndicateursModuleA11Y indicateursModuleA11Y =
                                    IndicateursModuleA11Y.builder()
                                            .idModule(module.getId())
                                            .idApplication(module.getIdApplication())
                                            .modName(module.getModName())
                                            .applicationName(module.getAppName())
                                            .domaineSndi(module.getDomaineSndi())
                                            .sndi(module.getSndi())
                                            .notation(Notation.NR)
                                            .build();
                            if (mapMetric.containsKey(module.getId())) {
                                InfosSaisiesA11yEntity infos = mapMetric.get(module.getId());
                                indicateursModuleA11Y.setDateMajInfosSaisie(
                                        infos.getDateMajInfosSaisies());
                                indicateursModuleA11Y.setIsDeclaration(infos.getIsDeclaration());
                                // 👇 NOUVEAU : on remonte la date de déclaration
                                indicateursModuleA11Y.setDateDeclaration(
                                        infos.getDateDeclaration());

                                indicateursModuleA11Y.setTypeAuditId(
                                        infos.getIdIndicateurTypeAudit());
                                indicateursModuleA11Y.setTypeAuditLibelle(
                                        getTypeAuditLibelle(infos.getIdIndicateurTypeAudit()));

                                indicateursModuleA11Y.setScoreAudit(infos.getScoreAudit());
                                indicateursModuleA11Y.setDateAudit(infos.getDateAudit());
                                indicateursModuleA11Y.setNotation(calculerNotation(infos, module.getZoneProduction()));                            }
                            if (mapIssueSonar.containsKey(module.getId())) {
                                String nbIssue =
                                        String.valueOf(
                                                mapIssueSonar.get(module.getId()).getValeur());
                                indicateursModuleA11Y.setNbIssueAccessibilite(nbIssue);
                                indicateursModuleA11Y.setLettreIssueAccessibilite(
                                        convertService.convertIssueAccessebilite(nbIssue));
                            }

                            return indicateursModuleA11Y;
                        })
                .toList();
    }

    public List<IndicateursModuleA11Y> listerModulesA11y() {
        List<IndicateursModuleA11Y> result = new ArrayList<>(listerModulesIHMA11y());
        List<Module> modules = oscarService.getModules();
        Set<Integer> idsExistants =
                result.stream().map(IndicateursModuleA11Y::getIdModule).collect(Collectors.toSet());

        for (Module module : modules) {
            if (!idsExistants.contains(module.getId())) {
                IndicateursModuleA11Y indicateur =
                        IndicateursModuleA11Y.builder()
                                .idModule(module.getId())
                                .idApplication(module.getIdApplication())
                                .modName(module.getModName())
                                .domaineSndi(module.getDomaineSndi())
                                .sndi(module.getSndi())
                                .notation(Notation.SO)
                                .build();
                result.add(indicateur);
            }
        }
        return result;
    }

    public List<IndicateursModuleA11Y> listerApplicationsA11y() {
        List<IndicateursModuleA11Y> retour = new ArrayList<>();
        Map<Application, Set<Integer>> mapApplicationsWithModules =
                oscarService.mapApplicationsWithModules();
        List<IndicateursModuleA11Y> listeDonneesA11y = listerModulesA11y();
        Map<Integer, IndicateursModuleA11Y> mapMetricA11iy =
                listeDonneesA11y.stream()
                        .collect(
                                Collectors.toMap(
                                        IndicateursModuleA11Y::getIdModule, Function.identity()));
        for (Map.Entry<Application, Set<Integer>> entry : mapApplicationsWithModules.entrySet()) {
            Application app = entry.getKey();
            IndicateursModuleA11Y indicateursModuleA11Y =
                    IndicateursModuleA11Y.builder()
                            .idModule(null)
                            .idApplication(app.getIdApplication())
                            .applicationName(app.getAppName())
                            .notation(Notation.SO)
                            .build();
            Set<Integer> moduleIds = entry.getValue();
            Optional<IndicateursModuleA11Y> meilleurIndicateur =
                    moduleIds.stream()
                            .map(mapMetricA11iy::get) // Obtenir l'indicateur à partir de l'idModule
                            .filter(Objects::nonNull)
                            .filter(
                                    ind -> {
                                        Notation n = ind.getNotation();
                                        return n != null && n != Notation.SO;
                                    })
                            .max(
                                    Comparator.comparing(
                                            ind ->
                                                    ind.getNotation()
                                                            .getGrade())); // Trouver la plus petite
            // (ordre alphabétique)

            meilleurIndicateur.ifPresentOrElse(
                    ind -> {
                        indicateursModuleA11Y.setNotation(ind.getNotation());
                        indicateursModuleA11Y.setScoreAudit(ind.getScoreAudit());
                    },
                    () -> log.debug("Aucune lettre trouvée pour l'application {}", entry.getKey()));
            retour.add(indicateursModuleA11Y);
        }

        return retour;
    }

    private String getTypeAuditLibelle(Integer typeAuditId) {
        return switch (typeAuditId) {
            case 510 -> "Aucun audit";
            case 511 -> "Audit partiel";
            case 512 -> "Audit complet";
            case 513 -> "Audit complet externe";
            default -> "Inconnu"; // Valeur par défaut si non trouvé
        };
    }

    /** Calcule la Notation en fonction du type d'audit, du score et de son exposition. */
    private Notation calculerNotation(InfosSaisiesA11yEntity infos, String zoneProduction) {
        boolean hasDeclaration = infos.getIsDeclaration();
        int typeAudit = infos.getIdIndicateurTypeAudit();
        float score = infos.getScoreAudit();

        LocalDate maintenant = LocalDate.now();

        // Pas de déclaration ou déclaration de plus de 3 ans
        if (!hasDeclaration
                || infos.getDateDeclaration() == null
                || infos.getDateDeclaration().isBefore(maintenant.minusYears(3))) {
            return Notation.F;
        }

        // Pas d'audit ou audit de plus de 3 ans
        if (typeAudit == 510
                || infos.getDateAudit() == null
                || infos.getDateAudit().isBefore(maintenant.minusYears(3))) {
            return Notation.E;
        }

        // Exposition externe : seuls les audits complets sont pris en compte
        if ("externe".equalsIgnoreCase(zoneProduction)
                && typeAudit == 511) {
            return Notation.E;
        }

        // Exposition interne : audit partiel ou complet
        if (score < 50) {
            return Notation.D;
        } else if (score < 75) {
            return Notation.C;
        } else if (score < 100) {
            return Notation.B;
        } else {
            return Notation.A;
        }
    }
}
