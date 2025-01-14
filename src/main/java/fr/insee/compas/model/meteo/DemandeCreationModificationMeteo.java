package fr.insee.compas.model.meteo;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DemandeCreationModificationMeteo {

    private Integer idApplication;
    private int idIndicateur;
    private BigDecimal valeurMeteo;
    private LocalDate date;
    private String commentaire;
}
