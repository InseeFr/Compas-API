package fr.insee.compas.service.maturitecloud.indicateur;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.insee.compas.dto.DemandeCreationStrategieCloud;
import fr.insee.compas.exception.StrategieCloudException;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class CloudCreationService implements ICloudCreation {

    private static final int ID_INDICATEUR_STRATEGIE_CLOUD = 614;
    private static final int ID_INDICATEUR_ENV_CIBLE_PROD = 613;
    private static final int ID_SOURCE_SAISIE_MANUELLE = 2;

    private final TableFaitsRepository tableFaitsRepository;
    private final OscarService oscarService;

    @Transactional
    public List<Long> creerStrategieCloud(DemandeCreationStrategieCloud demande) {
        List<Long> ids = new ArrayList<>();
        try {
            log.info(
                    "Début création stratégie cloud pour {} module(s)",
                    demande.getIdsModule().size());
            List<Module> modules = oscarService.getModules();
            Map<Integer, Integer> mappedIdModuleToIdApplication =
                    mappedIdModuleToIdApplication(modules);
            demande.getIdsModule()
                    .forEach(
                            idModule -> {
                                Long idAvancement =
                                        saveOrUpdate(
                                                mappedIdModuleToIdApplication,
                                                idModule,
                                                ID_INDICATEUR_STRATEGIE_CLOUD,
                                                demande.getDate(),
                                                demande.getAvancement(),
                                                demande.getCommentaire());
                                Long idEnvCible =
                                        saveOrUpdate(
                                                mappedIdModuleToIdApplication,
                                                idModule,
                                                ID_INDICATEUR_ENV_CIBLE_PROD,
                                                demande.getDate(),
                                                demande.getEnvCibleProd(),
                                                null);
                                ids.addAll(List.of(idAvancement, idEnvCible));
                            });
            log.info("Création terminée pour les modules {}", demande.getIdsModule());
            return ids;
        } catch (Exception e) {

            log.error(
                    "Erreur lors de la création de la stratégie cloud pour la demande : {}",
                    demande,
                    e);

            throw new StrategieCloudException(
                    "Erreur lors de la création de la stratégie cloud", e);
        }
    }

    private Map<Integer, Integer> mappedIdModuleToIdApplication(List<Module> modules) {
        log.debug("Mapping id module to id application");
        return modules.stream().collect(Collectors.toMap(Module::getId, Module::getIdApplication));
    }

    private Long saveOrUpdate(
            Map<Integer, Integer> mappedIdModuleToIdApplication,
            Integer idModule,
            Integer idIndicateur,
            LocalDate date,
            BigDecimal valeur,
            String commentaire) {

        List<TableFaits> existing =
                tableFaitsRepository.findByIdIndicateurAndIdModule(idIndicateur, idModule);

        if (!existing.isEmpty()) {
            log.info(
                    "La stratégie Cloud est présente pour l'id {}, on va écraser les données",
                    idModule);
            return updateLatest(existing, valeur, commentaire);
        }
        log.info(
                "La stratégie Cloud n'est pas présente pour l'id {}, on va créer les données",
                idModule);
        Integer idApp = mappedIdModuleToIdApplication.get(idModule);
        if (idApp == null) {
            log.warn("Aucune application trouvée pour le module {}", idModule);
            throw new StrategieCloudException(
                    "Aucune application dans oscar trouvée pour le module " + idModule);
        }
        return createNew(idModule, idIndicateur, idApp, date, valeur, commentaire);
    }

    private Long updateLatest(List<TableFaits> existing, BigDecimal valeur, String commentaire) {
        TableFaits tableFaits =
                existing.stream()
                        .max(Comparator.comparingLong(TableFaits::getIdApplication))
                        .orElseThrow();
        tableFaits.setValeur(valeur);
        tableFaits.setCommentaire(commentaire);
        return tableFaitsRepository.save(tableFaits).getId();
    }

    private Long createNew(
            Integer idModule,
            Integer idIndicateur,
            Integer idApp,
            LocalDate date,
            BigDecimal valeur,
            String commentaire) {

        TableFaits tableFaits =
                TableFaits.builder()
                        .idModule(idModule)
                        .idIndicateur(idIndicateur)
                        .idApplication(idApp)
                        .date(date)
                        .valeur(valeur)
                        .idSource(ID_SOURCE_SAISIE_MANUELLE)
                        .commentaire(commentaire)
                        .build();

        return tableFaitsRepository.save(tableFaits).getId();
    }
}
