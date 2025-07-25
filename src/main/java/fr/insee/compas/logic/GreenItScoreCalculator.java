package fr.insee.compas.logic;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.greenit.GreenItScore;
import fr.insee.compas.model.greenit.IndicateurGreenIT;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.model.greenit.util.ScoreGreenUtils;
import fr.insee.compas.util.ScoreUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GreenItScoreCalculator {

    private static final BigDecimal POIDS_CONSO = new BigDecimal("0.4");
    private static final BigDecimal POIDS_PRESSION = new BigDecimal("0.4");
    private static final BigDecimal POIDS_GASPILLAGE = new BigDecimal("0.2");

    private static final BigDecimal FACTEUR_RAM = new BigDecimal("4");
    private static final BigDecimal FACTEUR_CPU = new BigDecimal("0.0125");
    private static final BigDecimal FACTEUR_DISK = new BigDecimal("0.01");
    private static final BigDecimal FACTEUR_GASPILLAGE_ALLOUE = new BigDecimal("0.7");
    private static final BigDecimal FACTEUR_GASPILLAGE_INUTILISE = new BigDecimal("0.3");

    private final GreenItScoreConfigProperties config;

    public GreenItScoreCalculator(GreenItScoreConfigProperties config) {
        super();
        this.config = config;
    }

    public GreenItScore compute(IndicateurGreenIT kpis) {
        return computeScore(
                kpis,
                config.getApplication().getConsoMax(),
                config.getApplication().getPressionMaxRam(),
                config.getApplication().getPressionMaxCpu(),
                config.getApplication().getPressionMaxDisk());
    }

    public GreenItScore compute(IndicateurModuleGreenIT kpis) {
        return computeScore(
                kpis,
                config.getModule().getConsoMax(),
                config.getModule().getPressionMaxRam(),
                config.getModule().getPressionMaxCpu(),
                config.getModule().getPressionMaxDisk());
    }

    public GreenItScore computeScore(
            IndicateurGreenIT kpis, double consoMax, double maxRam, double maxCpu, double maxDisk) {

        if (isOneDenomCloseToZero(consoMax, maxRam, maxCpu, maxDisk)) {
            log.warn("Un ou plusieurs maximums sont à 0, évitant division par zéro.");
            return kpis.toGreenItScore(
                    BigDecimal.valueOf(1.0),
                    "E",
                    BigDecimal.valueOf(1),
                    BigDecimal.valueOf(1),
                    BigDecimal.valueOf(1));
        }

        final BigDecimal scoreConso = BigDecimal.valueOf(safeDouble(kpis.getConso()) / consoMax);
        final BigDecimal pression =
                calculPression(
                        safeDouble(kpis.getRamAllocated()),
                        safeDouble(kpis.getCpuAllocated()),
                        safeDouble(kpis.getDiskAllocated()));
        final BigDecimal pressionMax = calculPression(maxRam, maxCpu, maxDisk);

        final BigDecimal scorePression = pression.divide(pressionMax, RoundingMode.UP);
        final BigDecimal scoreGaspillage =
                scoreGaspillage(
                        safeDouble(kpis.getDiskUsed()),
                        safeDouble(kpis.getDiskAllocated()),
                        maxDisk);

        final BigDecimal scoreFinal =
                (POIDS_CONSO.multiply(scoreConso))
                        .add(POIDS_PRESSION.multiply(scorePression))
                        .add(POIDS_GASPILLAGE.multiply(scoreGaspillage));
        final String grade = ScoreGreenUtils.gradeFromScore(scoreFinal);

        log.debug(
                "Scores calculés - Conso: {}, Pression: {}, Gaspillage: {}, Total: {}, Grade: {}",
                scoreConso,
                scorePression,
                scoreGaspillage,
                scoreFinal,
                grade);
        return kpis.toGreenItScore(scoreFinal, grade, scoreConso, scorePression, scoreGaspillage);
    }

    private BigDecimal scoreGaspillage(
            double diskUsed, double diskAllocated, double maxDiskAllocated) {
        return (FACTEUR_GASPILLAGE_ALLOUE.multiply(
                        BigDecimal.valueOf(
                                diskAllocated * (1 - diskUsed / 100) / maxDiskAllocated)))
                .add(FACTEUR_GASPILLAGE_INUTILISE.multiply(BigDecimal.valueOf(1 - diskUsed / 100)));
    }

    private BigDecimal calculPression(double ram, double cpu, double disk) {
        final BigDecimal bigRam = BigDecimal.valueOf(ram);
        final BigDecimal bigCpu = BigDecimal.valueOf(cpu);
        final BigDecimal bigDisk = BigDecimal.valueOf(disk);

        return (bigRam.multiply(FACTEUR_RAM))
                .add(bigCpu.multiply(FACTEUR_CPU))
                .add(bigDisk.multiply(FACTEUR_DISK));
    }

    private double safeDouble(Number number) {
        return number != null ? number.doubleValue() : 0.0;
    }

    private boolean isOneDenomCloseToZero(
            double consoMax, double maxRam, double maxCpu, double maxDisk) {
        return (ScoreUtils.isCloseToZero(consoMax)
                || ScoreUtils.isCloseToZero(maxRam)
                || ScoreUtils.isCloseToZero(maxCpu)
                || ScoreUtils.isCloseToZero(maxDisk));
    }
}
