package fr.insee.compas.logic;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import fr.insee.compas.model.greenit.GreenItScore;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.service.greenit.properties.GreenItScoreConfigProperties;
import fr.insee.compas.service.greenit.score.GreenItComputeScore;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GreenItScoreCalculatorTest {

    private GreenItComputeScore calculator;
    @Mock private GreenItScoreConfigProperties config;

    @Mock private GreenItScoreConfigProperties.ConfigurationApplicationModule application;

    @Mock private GreenItScoreConfigProperties.ConfigurationApplicationModule module;

    @BeforeEach
    void setUp() {
        Mockito.when(config.getApplication()).thenReturn(application);
        Mockito.when(config.getModule()).thenReturn(module);
        Mockito.when(config.getApplication().getConsoMax()).thenReturn(100.0);
        Mockito.when(config.getApplication().getPressionMaxRam()).thenReturn(2.0);
        Mockito.when(config.getApplication().getPressionMaxCpu()).thenReturn(1000.0);
        Mockito.when(config.getApplication().getPressionMaxDisk()).thenReturn(100.0);
        Mockito.when(config.getModule().getConsoMax()).thenReturn(10.0);
        Mockito.when(config.getModule().getPressionMaxRam()).thenReturn(2.0);
        Mockito.when(config.getModule().getPressionMaxCpu()).thenReturn(100.0);
        Mockito.when(config.getModule().getPressionMaxDisk()).thenReturn(10.0);
        calculator = new GreenItComputeScore(config);
    }

    @Test
    void testComputeApplicationScore() {
        final IndicateurApplicationGreenIT kpis = new IndicateurApplicationGreenIT();
        kpis.setApplicationId(1);
        kpis.setConso(50);
        kpis.setRamAllocated(1);
        kpis.setCpuAllocated(500);
        kpis.setDiskAllocated(50);
        kpis.setDiskUsed(40);

        final GreenItScore score = calculator.computeAppScore(kpis);

        assertNotNull(score);
        assertEquals(1, score.getIdApplication());
        assertNull(score.getIdModule());
        assertEquals(BigDecimal.valueOf(0.426), score.getScore().setScale(3, RoundingMode.UP));
        assertTrue(
                score.getScore().compareTo(BigDecimal.valueOf(0.0)) >= 0
                        && score.getScore().compareTo(BigDecimal.valueOf(1.0)) <= 0.0);
        assertEquals("C", score.getGrade());
    }

    @Test
    void testComputeModuleScore() {
        final IndicateurModuleGreenIT kpis = new IndicateurModuleGreenIT();
        kpis.setModuleId(1);
        kpis.setConso(8);
        kpis.setRamAllocated(2);
        kpis.setCpuAllocated(10);
        kpis.setDiskAllocated(10);
        kpis.setDiskUsed(5);

        final GreenItScore score = calculator.computeModuleScore(kpis);
        assertNotNull(score);
        assertNull(score.getIdApplication());
        assertEquals(1, score.getIdModule());
        assertTrue(
                score.getScore().compareTo(BigDecimal.valueOf(0.0)) >= 0
                        && score.getScore().compareTo(BigDecimal.valueOf(1.0)) <= 0.0);
    }

    @Test
    void testZeroMaximumsReturnScoreE() {

        Mockito.when(config.getApplication().getConsoMax()).thenReturn(0.0);
        final IndicateurApplicationGreenIT kpis = new IndicateurApplicationGreenIT();
        kpis.setApplicationId(1);
        kpis.setConso(10);
        kpis.setCpuAllocated(100);
        kpis.setRamAllocated(1);
        kpis.setDiskAllocated(50);
        kpis.setDiskUsed(80);

        final GreenItScore score = calculator.computeAppScore(kpis);

        assertEquals("E", score.getGrade());
        assertEquals(1.0, score.getScore().doubleValue());
    }

    @Test
    void testNullFieldsHandledSafely() {
        final IndicateurModuleGreenIT kpis = new IndicateurModuleGreenIT();
        kpis.setDiskUsed(null);
        final GreenItScore score = calculator.computeModuleScore(kpis);

        assertEquals(
                BigDecimal.valueOf(0.0),
                score.getGaspillage().setScale(1, RoundingMode.UP)); // car diskUsed = 0%
    }
}
