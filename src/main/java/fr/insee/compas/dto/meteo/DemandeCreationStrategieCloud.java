package fr.insee.compas.dto.meteo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO pour la saisie de la stratégie cloud */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeCreationStrategieCloud {

    /** Liste des IDs des modules sélectionnées changez les tests apres */
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Integer> idsModule;

    /** Avancement de la stratégie cloud 1 => A instruire 2 => En cours 3 => Validée */
    private BigDecimal avancement;

    /** Commentaire facultatif */
    private String commentaire;

    private BigDecimal envCibleProd;
    private LocalDate date;
}
