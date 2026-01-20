package fr.insee.compas.service.meteo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.meteo.DemandeCreationMeteo;
import fr.insee.compas.repository.TableFaitsRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MeteoCreationService {

    // discuter du mapping Indicateur TableFaits : ManyToOne, type id Long
    public static final int ID_INDICATEUR_METEO = 401;

    // discuter du mapping Source TableFaits
    public static final int ID_SOURCE_SAISIE_MANUELLE = 2;

    private TableFaitsRepository tableFaitsRepository;

    /**
     * Créer le {@link fr.insee.compas.model.compas.TableFaits} pour la demande
     *
     * @param demandeCreationMeteo : params saisie
     * @return les ids de {@link fr.insee.compas.model.compas.TableFaits} créés
     */
    public List<Long> creerMeteo(DemandeCreationMeteo demandeCreationMeteo) {
        return demandeCreationMeteo.getIdsApplication().stream()
                .map(
                        idApp -> {
                            List<TableFaits> existingMeteos =
                                    tableFaitsRepository
                                            .findByIdApplicationAndDateAndIdIndicateur(
                                                    idApp,
                                                    demandeCreationMeteo.getDate(),
                                                    ID_INDICATEUR_METEO)
                                            .orElse(Collections.emptyList());
                            if (existingMeteos.isEmpty()) {
                                return saveNewMeteo(
                                        idApp,
                                        demandeCreationMeteo.getValeurMeteo(),
                                        demandeCreationMeteo.getCommentaire(),
                                        demandeCreationMeteo.getDate());
                            }
                            TableFaits latestMeteo =
                                    existingMeteos.stream()
                                            .max(Comparator.comparingLong(TableFaits::getId))
                                            .orElseThrow();
                            return overwriteMeteo(
                                    latestMeteo,
                                    demandeCreationMeteo.getValeurMeteo(),
                                    demandeCreationMeteo.getCommentaire());
                        })
                .toList();
    }

    private Long overwriteMeteo(TableFaits tableFaits, BigDecimal valeur, String commentaire) {
        tableFaits.setValeur(valeur);
        tableFaits.setCommentaire(commentaire);
        return tableFaitsRepository.save(tableFaits).getId();
    }

    private Long saveNewMeteo(
            Integer idApp, BigDecimal valeurMeteo, String commentaire, LocalDate date) {
        TableFaits tableFaits =
                TableFaits.builder()
                        .idApplication(idApp)
                        .idIndicateur(ID_INDICATEUR_METEO)
                        .valeur(valeurMeteo)
                        .idSource(ID_SOURCE_SAISIE_MANUELLE)
                        .commentaire(commentaire)
                        .date(date)
                        .build();
        return tableFaitsRepository.save(tableFaits).getId();
    }
}
