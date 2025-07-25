package fr.insee.compas.model.greenit;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GreenItScore {
    private Integer idApplication;
    private Integer idModule;
    private BigDecimal score;
    private String grade;
    private BigDecimal conso;
    private BigDecimal impact;
    private BigDecimal gaspillage;
}
