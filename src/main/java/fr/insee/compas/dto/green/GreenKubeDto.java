package fr.insee.compas.dto.green;

import static fr.insee.compas.util.greenit.GreenITutils.subtractExactSafe;
import static fr.insee.compas.util.greenit.GreenITutils.subtractSafe;

import java.math.BigDecimal;
import java.time.LocalDate;

import fr.insee.compas.model.greenit.GreenItScore;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class GreenKubeDto extends GreenItBaseDto {

    private final BigDecimal nbPodMaxi;
    private final BigDecimal nbPodMaxiHist;
    private final BigDecimal nbPodMaxiProd;
    private final BigDecimal nbPodMaxiHistProd;
    private final Long pvcUsed;
    private final Long pvcUsedHist;
    private final Long pvcUsedProd;
    private final Long pvcUsedHistProd;
    private final Long ramUsed;
    private final Long ramUsedHist;
    private final Long ramUsedProd;
    private final Long ramUsedHistProd;
    private final Long s3Used;
    private final Long s3UsedHist;
    private final Long s3UsedProd;
    private final Long s3UsedHistProd;
    private final BigDecimal cpuUsed;
    private final BigDecimal cpuUsedHist;
    private final BigDecimal cpuUsedProd;
    private final BigDecimal cpuUsedHistProd;
    private final LocalDate dateMaj;

    public Long pvcUsedHp() {
        return subtractExactSafe(pvcUsed, pvcUsedProd);
    }

    public Long pvcUsedHistHp() {
        return subtractExactSafe(pvcUsedHist, pvcUsedHistProd);
    }

    public Long s3UsedHp() {
        return subtractExactSafe(s3Used, s3UsedProd);
    }

    public Long s3UsedHistHp() {
        return subtractExactSafe(s3UsedHist, s3UsedHistProd);
    }

    public Long ramUsedHp() {
        return subtractExactSafe(ramUsed, ramUsedProd);
    }

    public Long ramUsedHistHp() {
        return subtractExactSafe(ramUsedHist, ramUsedHistProd);
    }

    public BigDecimal cpuUsedHp() {
        return subtractSafe(cpuUsed, cpuUsedProd);
    }

    public BigDecimal cpuUsedHistHp() {
        return subtractSafe(cpuUsedHist, cpuUsedHistProd);
    }

    public BigDecimal nbPodMaxiHp() {
        return subtractSafe(nbPodMaxi, nbPodMaxiProd);
    }

    public BigDecimal nbPodMaxiHistHp() {
        return subtractSafe(nbPodMaxiHist, nbPodMaxiHistProd);
    }

    public GreenItScore toGreenItScoreKube(
            BigDecimal score,
            String grade,
            BigDecimal conso,
            BigDecimal pression,
            BigDecimal gaspillage) {
        return toGreenItScore(score, grade, conso, pression, gaspillage);
    }
}
