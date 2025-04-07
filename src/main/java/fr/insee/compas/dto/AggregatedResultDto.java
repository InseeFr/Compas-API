package fr.insee.compas.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AggregatedResultDto {
    private BigDecimal sumValeur;
    private Integer idApplication;
}
