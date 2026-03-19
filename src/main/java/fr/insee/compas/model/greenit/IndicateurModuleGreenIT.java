package fr.insee.compas.model.greenit;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IndicateurModuleGreenIT extends IndicateurGreenIT {
    private Integer moduleId;
    private String moduleName;

    @Override
    public GreenItScore toGreenItScore(
            BigDecimal score,
            String grade,
            BigDecimal conso,
            BigDecimal pression,
            BigDecimal gaspillage) {
        return new GreenItScore(null, moduleId, score, grade, conso, pression, gaspillage);
    }
}
