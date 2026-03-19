package fr.insee.compas.model.compas;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class TableFaits {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer idModule;
    private Integer idApplication;
    private Integer idIndicateur;
    private LocalDate date;
    private BigDecimal valeur;
    private Integer idSource;
    private String commentaire;
}
