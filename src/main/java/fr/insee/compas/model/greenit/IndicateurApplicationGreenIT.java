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
public class IndicateurApplicationGreenIT extends IndicateurGreenIT {
    private Integer applicationId;
    private String applicationName;

    @Override
    public GreenItScore toGreenItScore(
            BigDecimal score,
            String grade,
            BigDecimal conso,
            BigDecimal pression,
            BigDecimal gaspillage) {
        return new GreenItScore(applicationId, null, score, grade, conso, pression, gaspillage);
    }
}
