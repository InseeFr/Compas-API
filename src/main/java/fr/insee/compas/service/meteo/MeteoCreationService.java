package fr.insee.compas.service.meteo;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.meteo.DemandeCreationModificationMeteo;
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
     * @return le id du {@link fr.insee.compas.model.compas.TableFaits} créé
     */
    public Long creerMeteo(DemandeCreationModificationMeteo demandeCreationMeteo) {
        TableFaits tableFaits =
                TableFaits.builder()
                        .idApplication(demandeCreationMeteo.getIdApplication())
                        .idIndicateur(ID_INDICATEUR_METEO)
                        .valeur(demandeCreationMeteo.getValeurMeteo())
                        .idSource(ID_SOURCE_SAISIE_MANUELLE)
                        .commentaire(demandeCreationMeteo.getCommentaire())
                        .date(demandeCreationMeteo.getDate())
                        .build();
        return tableFaitsRepository.save(tableFaits).getId();
    }
}
