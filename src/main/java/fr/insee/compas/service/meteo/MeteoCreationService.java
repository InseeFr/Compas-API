package fr.insee.compas.service.meteo;

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
                        idApplication -> {
                            TableFaits tableFaits =
                                    TableFaits.builder()
                                            .idApplication(idApplication)
                                            .idIndicateur(ID_INDICATEUR_METEO)
                                            .valeur(demandeCreationMeteo.getValeurMeteo())
                                            .idSource(ID_SOURCE_SAISIE_MANUELLE)
                                            .commentaire(demandeCreationMeteo.getCommentaire())
                                            .date(demandeCreationMeteo.getDate())
                                            .build();
                            return tableFaitsRepository.save(tableFaits).getId();
                        })
                .toList();
    }
}
