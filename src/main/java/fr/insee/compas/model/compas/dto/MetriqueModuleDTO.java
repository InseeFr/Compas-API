package fr.insee.compas.model.compas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetriqueModuleDTO {
    private Integer idModule;
    private LocalDate date;
    private BigDecimal totalValeur;
}
