package fr.insee.compas.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import fr.insee.compas.dto.GreenItAppDto;
import fr.insee.compas.mapper.green.GreenItMapper;
import fr.insee.compas.model.greenit.GreenItScore;
import fr.insee.compas.service.greenit.score.GreenItComputeScore;
import fr.insee.compas.view.IndicateurApplicationGreenITView;

class IndicateurApplicationGreenITViewTest {

    private GreenItMapper mapper;
    private GreenItComputeScore greenItComputeScore;

    @BeforeEach
    void setUp() {
        greenItComputeScore = Mockito.mock(GreenItComputeScore.class);
        mapper = new GreenItMapper(greenItComputeScore);
    }

    @Test
    void toView_shouldMapCorrectly_whenIndicateurIsValid() {
        // Given
        final GreenItAppDto indicateur =
                GreenItAppDto.builder()
                        .applicationId(42)
                        .applicationName("TestApp")
                        .ramAllocated(BigDecimal.valueOf(4))
                        .ramMaxi(new BigDecimal("80"))
                        .diskAllocated(BigDecimal.valueOf(100))
                        .diskUsed(BigDecimal.valueOf(60))
                        .cpuAllocated(BigDecimal.valueOf(2000))
                        .cpuMaxi(new BigDecimal(90))
                        .conso(BigDecimal.valueOf(150))
                        .nbVm(BigDecimal.valueOf(10))
                        .ramAllocatedProd(BigDecimal.valueOf(2))
                        .ramMaxiProd(new BigDecimal(2))
                        .diskAllocatedProd(BigDecimal.valueOf(80))
                        .diskUsedProd(BigDecimal.valueOf(20))
                        .cpuAllocatedProd(BigDecimal.valueOf(800))
                        .cpuMaxiProd(new BigDecimal(60))
                        .consoProd(BigDecimal.valueOf(100))
                        .nbVmProd(BigDecimal.valueOf(5))
                        .dateMaj(LocalDate.of(2025, 11, 24))
                        .build();

        final GreenItScore score = new GreenItScore();
        score.setScore(BigDecimal.valueOf(12.3456));
        score.setImpact(BigDecimal.valueOf(20.1111));
        score.setGaspillage(BigDecimal.valueOf(2.0));
        score.setGrade("B");

        Mockito.when(greenItComputeScore.computeAppScore(indicateur)).thenReturn(score);

        final IndicateurApplicationGreenITView view = mapper.mapToView(indicateur);

        final SoftAssertions softAssertions = new SoftAssertions();
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
        softAssertions.assertThat(view.getImpactScore()).isEqualTo("20.112");
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
}
