package fr.insee.compas.dto.green;

import java.math.BigDecimal;

import fr.insee.compas.model.greenit.GreenItScore;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class GreenItBaseDto {
    private Integer applicationId;
    private String applicationName;
    private String serviceDev;
    private String domaineDev;
    private String domaineFonc;

    public GreenItBaseDto() {
        super();
    }

    public GreenItScore toGreenItScore(
            BigDecimal score,
            String grade,
            BigDecimal conso,
            BigDecimal pression,
            BigDecimal gaspillage) {
        return new GreenItScore(applicationId, null, score, grade, conso, pression, gaspillage);
    }
}
