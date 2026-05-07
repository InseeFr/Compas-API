package fr.insee.compas.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import fr.insee.compas.model.greenit.GreenItScore;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.service.greenit.score.GreenItComputeScore;
import fr.insee.compas.view.IndicateurApplicationGreenITView;

class IndicateurApplicationGreenITViewTest {

    private IndicateurApplicationGreenITViewMapper mapper;
    private GreenItComputeScore greenItComputeScore;

    @BeforeEach
    void setUp() {
        greenItComputeScore = Mockito.mock(GreenItComputeScore.class);
        mapper = new IndicateurApplicationGreenITViewMapper(greenItComputeScore);
    }

    @Test
    void toView_shouldMapCorrectly_whenIndicateurIsValid() {
        // Given
        final IndicateurApplicationGreenIT indicateur =
                IndicateurApplicationGreenIT.builder()
                        .applicationId(42)
                        .applicationName("TestApp")
                        .ramAllocated(4)
                        .ramMaxi(new BigDecimal("80"))
                        .diskAllocated(100)
                        .diskUsed(new BigDecimal(60))
                        .cpuAllocated(2000)
                        .cpuMaxi(new BigDecimal(90))
                        .conso(150)
                        .nbVm(10)
                        .ramAllocatedProd(2)
                        .ramMaxiProd(new BigDecimal(2))
                        .diskAllocatedProd(80)
                        .diskUsedProd(new BigDecimal(20))
                        .cpuAllocatedProd(800)
                        .cpuMaxiProd(new BigDecimal(60))
                        .consoProd(100)
                        .nbVmProd(5)
                        .dateMaj(LocalDate.of(2025, 11, 24))
                        .build();

        final GreenItScore score = new GreenItScore();
        score.setConso(BigDecimal.valueOf(12.3456));
        score.setImpact(BigDecimal.valueOf(20.1111));
        score.setGaspillage(BigDecimal.valueOf(2.0));
        score.setGrade("B");

        Mockito.when(greenItComputeScore.computeAppScore(indicateur)).thenReturn(score);

        final Optional<IndicateurApplicationGreenITView> optView = mapper.toView(indicateur);
        final IndicateurApplicationGreenITView view = optView.get();

        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(optView).isPresent();
        softAssertions.assertThat(view.getApplicationName()).isEqualTo("TestApp");
        softAssertions.assertThat(view.getRamAllocated()).isEqualTo("4");
        softAssertions.assertThat(view.getRamMaxi()).isEqualTo("80");
        softAssertions.assertThat(view.getDiskAllocated()).isEqualTo("100");
        softAssertions.assertThat(view.getDiskUsed()).isEqualTo("60");
        softAssertions.assertThat(view.getCpuAllocated()).isEqualTo("2000");
        softAssertions.assertThat(view.getCpuMaxi()).isEqualTo("90");
        softAssertions.assertThat(view.getConso()).isEqualTo("150");
        softAssertions.assertThat(view.getNbVm()).isEqualTo("10");
        softAssertions.assertThat(view.getConsoScore()).isEqualTo("12.346");
        softAssertions.assertThat(view.getImpactScore()).isEqualTo("20.111");
        softAssertions.assertThat(view.getGaspillageScore()).isEqualTo("2.000");
        softAssertions.assertThat(view.getRamAllocatedProd()).isEqualTo("2");
        softAssertions.assertThat(view.getRamMaxiProd()).isEqualTo("2");
        softAssertions.assertThat(view.getDiskAllocatedProd()).isEqualTo("80");
        softAssertions.assertThat(view.getDiskUsedProd()).isEqualTo("20");
        softAssertions.assertThat(view.getCpuAllocatedProd()).isEqualTo("800");
        softAssertions.assertThat(view.getCpuMaxiProd()).isEqualTo("60");
        softAssertions.assertThat(view.getConsoProd()).isEqualTo("100");
        softAssertions.assertThat(view.getNbVmProd()).isEqualTo("5");
        softAssertions.assertThat(view.getDateMaj()).isEqualTo("2025-11-24");
        softAssertions.assertThat(view.getLettreGreen()).isEqualTo("B");

        softAssertions.assertAll();
    }

    @Test
    void toView_shouldReturnEmpty_whenInputIsNull() {
        final Optional<IndicateurApplicationGreenITView> result = mapper.toView(null);
        assertTrue(result.isEmpty());
    }
}
