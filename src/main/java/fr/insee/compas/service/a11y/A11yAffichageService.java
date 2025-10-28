package fr.insee.compas.service.a11y;

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
import fr.insee.compas.service.ConvertirValeurEnLettreService;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class A11yAffichageService {

    private OscarService oscarService;
    private A11yMajService a11yMajService;
    private TableFaitsService tableFaitsService;
    private ConvertirValeurEnLettreService convertService;

    public List<IndicateursModuleA11Y> listerModulesA11y() {
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
                                            .domaineSndi(module.getDomaineSndi())
                                            .sndi(module.getSndi())
                                            .notation(Notation.NR)
                                            .build();
                            if (mapMetric.containsKey(module.getId())) {
                                InfosSaisiesA11yEntity infos = mapMetric.get(module.getId());
                                indicateursModuleA11Y.setDateMajInfosSaisie(
                                        infos.getDateMajInfosSaisies());
                                indicateursModuleA11Y.setDeclaration(infos.isDeclaration());
                                indicateursModuleA11Y.setTypeAuditId(
                                        infos.getIdIndicateurTypeAudit());
                                indicateursModuleA11Y.setTypeAuditLibelle(
                                        getTypeAuditLibelle(
                                                infos.getIdIndicateurTypeAudit())); // todo

                                indicateursModuleA11Y.setScoreAudit(infos.getScoreAudit());
                                indicateursModuleA11Y.setDateAudit(infos.getDateAudit());
                                indicateursModuleA11Y.setNotation(calculerNotation(infos));
                            }
                            if (mapIssueSonar.containsKey(module.getId())) {
                                String nbIssue =
                                        String.valueOf(
                                                mapIssueSonar.get(module.getId()).getValeur());
                                indicateursModuleA11Y.setNbIssueAccessibilite(nbIssue);
                                indicateursModuleA11Y.setLettreIssueAccessibilite(
                                        convertService.getLettreIssueAccessebilite(nbIssue));
                            }

                            return indicateursModuleA11Y;
                        })
                .toList();
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
                            .nameApplication(app.getAppName())
                            .notation(Notation.NR)
                            .build();
            Set<Integer> moduleIds = entry.getValue();
            Optional<IndicateursModuleA11Y> meilleurIndicateur =
                    moduleIds.stream()
                            .map(mapMetricA11iy::get) // Obtenir l'indicateur à partir de l'idModule
                            .filter(Objects::nonNull)
                            .filter(
                                    ind -> {
                                        Notation n = ind.getNotation();
                                        return n != null && n != Notation.NR && n != Notation.SO;
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

    /** Calcule le Notation en fonction du type d'audit et du score. */

    /* todo gerer les codes indicateurs via une enum ou table */
    private Notation calculerNotation(InfosSaisiesA11yEntity infos) {
        if (infos == null) {
            return Notation.NR; // Aucune info.Indicateur Sonar todo si aucune remontée sonar
        }

        boolean hasDeclaration = infos.isDeclaration();
        int typeAudit = infos.getIdIndicateurTypeAudit();
        float score = infos.getScoreAudit();

        if (!hasDeclaration) {
            /*  if (infos.IndicateurSonar == null) {
                return Notation.NR; // Aucune info.IndicateurSonar todo si aucune remontée sonar
            }*/

            return Notation.H; // Pas de déclaration mais IndicateurSonar
        }

        if (typeAudit == 510) {
            return Notation.G; // Déclaration mais aucun audit
        }
        // Cas par défaut pour audit partiel
        if (typeAudit == 511) {
            return (score >= 50) ? Notation.E : Notation.F; // Conforme ou non
        }
        if (typeAudit == 512 || typeAudit == 513) { // Audit complet interne ou externe
            if (score < 50) {
                return Notation.D; // Audit complet mais < 50%
            } else if (score < 75) {
                return Notation.C; // Audit complet mais < 75%
            } else if (score < 100) {
                return Notation.B; // Audit complet mais < 100%
            } else if (score == 100) {
                return Notation.A; // Audit conforme à 100%
            }
        }

        return Notation.SO; //  valeur par défaut
    }
}
