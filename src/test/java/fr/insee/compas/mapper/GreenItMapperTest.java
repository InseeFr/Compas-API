package fr.insee.compas.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.dto.GreenItAppDto;
import fr.insee.compas.mapper.green.GreenItMapper;
import fr.insee.compas.model.greenit.GreenItScore;
import fr.insee.compas.service.greenit.score.GreenItComputeScore;
import fr.insee.compas.view.IndicateurApplicationGreenITView;

@ExtendWith(MockitoExtension.class)
class GreenItMapperTest {

    @Mock private GreenItComputeScore greenItComputeScore;

    @InjectMocks private GreenItMapper greenItMapper;

    @Test
    void should_map_dto_to_view() {

        // GIVEN
        GreenItAppDto dto =
                GreenItAppDto.builder()
                        .applicationId(1)
                        .applicationName("App1")
                        .ramAllocated(BigDecimal.TEN)
                        .cpuAllocated(BigDecimal.ONE)
                        .dateMaj(LocalDate.of(2024, 1, 1))
                        .build();

        GreenItScore score =
                new GreenItScore(
                        1,
                        null,
                        BigDecimal.valueOf(0.8),
                        "A",
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE);

        when(greenItComputeScore.computeAppScore(dto)).thenReturn(score);

        // WHEN
        IndicateurApplicationGreenITView result = greenItMapper.mapToView(dto);

        // THEN
        assertEquals(1, result.getApplicationId());
        assertEquals("App1", result.getApplicationName());
        assertEquals("A", result.getLettreGreen());
    }
}
