package fr.insee.compas.service.a11y;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.a11y.IndicateursModuleA11Y;
import fr.insee.compas.model.a11y.InfosSaisiesA11yEntity;
import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.repository.InfosSaisiesA11yRepository;
import fr.insee.compas.service.OscarService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class A11yAffichageService {

    private OscarService oscarService;
    private InfosSaisiesA11yRepository infosSaisiesA11yRepository;

    public List<IndicateursModuleA11Y> listerModulesA11y() {
        List<InfosSaisiesA11yEntity> metric =
                infosSaisiesA11yRepository.findLatestinfosSaisiesA11yForAllModules();
        Map<Integer, InfosSaisiesA11yEntity> mapMetric =
                metric.stream()
                        .collect(
                                Collectors.toMap(
                                        InfosSaisiesA11yEntity::getIdModule, // Clé : idModule
                                        infosSaisiesA11yEntity ->
                                                infosSaisiesA11yEntity // Valeur : l'objet
                                        // TableFaits
                                        ));
        return oscarService.getModulesIhm().stream()
                .map(
                        module -> {
                            IndicateursModuleA11Y indicateursModuleA11Y =
                                    IndicateursModuleA11Y.builder()
                                            .idModule(module.getId())
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
                                // mettre les
                                // libelles
                                // dans
                                // tableindicateur
                                indicateursModuleA11Y.setScoreAudit(infos.getScoreAudit());
                                indicateursModuleA11Y.setDateAudit(infos.getDateAudit());
                                indicateursModuleA11Y.setNotation(calculerNotation(infos));
                            }

                            return indicateursModuleA11Y;
                        })
                .toList();
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
            } else if (score < 90) {
                return Notation.B; // Audit complet mais < 99%
            } else if (score == 100) {
                return Notation.A; // Audit conforme à 100%
            }
        }

        return Notation.SO; //  valeur par défaut
    }
}
