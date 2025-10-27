package fr.insee.compas.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import fr.insee.compas.logic.GreenItScoreCalculator;
import fr.insee.compas.model.greenit.GreenItScore;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.view.IndicateurModuleGreenITView;

class IndicateurModuleGreenITViewTest {

    private IndicateurModuleGreenITViewMapper mapper;
    private GreenItScoreCalculator greenItScoreCalculator;

    @BeforeEach
    void setUp() {
        greenItScoreCalculator = Mockito.mock(GreenItScoreCalculator.class);
        mapper = new IndicateurModuleGreenITViewMapper(greenItScoreCalculator);
    }

    @Test
    void toView_shouldMapCorrectly_whenIndicateurIsValid() {
        // Given
        final IndicateurModuleGreenIT indicateur = new IndicateurModuleGreenIT();
        indicateur.setModuleId(42);
        indicateur.setModuleName("TestModule");
        indicateur.setRamAllocated(4);
        indicateur.setRamMaxi(new BigDecimal("80"));
        indicateur.setDiskAllocated(100);
        indicateur.setDiskUsed(new BigDecimal(60));
        indicateur.setCpuAllocated(2000);
        indicateur.setCpuMaxi(new BigDecimal(90));
        indicateur.setConso(150);
        indicateur.setNbVm(10);

        final GreenItScore score = new GreenItScore();
        score.setConso(BigDecimal.valueOf(12.3456));
        score.setImpact(BigDecimal.valueOf(20.1111));
        score.setGaspillage(BigDecimal.valueOf(2.0));
        score.setGrade("B");

        Mockito.when(greenItScoreCalculator.compute(indicateur)).thenReturn(score);

        final Optional<IndicateurModuleGreenITView> optView = mapper.toView(indicateur);
        final IndicateurModuleGreenITView view = optView.get();

        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(optView).isPresent();
        softAssertions.assertThat(view.getModuleName()).isEqualTo("TestModule");
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
        softAssertions.assertThat(view.getLettreGreen()).isEqualTo("B");
        softAssertions.assertAll();
    }

    @Test
    void toView_shouldReturnEmpty_whenInputIsNull() {
        final Optional<IndicateurModuleGreenITView> result = mapper.toView(null);
        assertTrue(result.isEmpty());
    }
}
