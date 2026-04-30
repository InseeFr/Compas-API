package fr.insee.compas.service.homologation;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.dto.HomologationDto;
import fr.insee.compas.model.homologation.Homologation;

@ExtendWith(MockitoExtension.class)
public class HomologationServiceTest {

    @Mock private HomologationMapper homologationMapper;

    @InjectMocks private HomologationService homologationService;

    private Homologation buildHomologation(String app, String statut) {
        Homologation h = new Homologation();
        h.setApsOscar(app);
        h.setStatutHomologation(statut);
        h.setSensitivity("1");
        h.setHomologationRemarks("");
        h.setHomologationBeginDate("24/03/2026");
        h.setHomologationEndDate("24/03/2027");
        return h;
    }

    @Test
    void getAllHomologationTest() {
        Homologation h = buildHomologation("une app", "homologuée");

        when(homologationMapper.getAllHomologationSep()).thenReturn(List.of(h));
        when(homologationMapper.getApplicationMap()).thenReturn(Map.of("une app", 1));

        List<HomologationDto> result = homologationService.getAllHomologation();
        HomologationDto dto = result.get(0);

        assertEquals(1, result.size());
        assertEquals(1, dto.applicationId());
        assertEquals("une app", dto.nomApp());
        assertEquals("homologuée", dto.statutHomologation());
        assertEquals("1", dto.sensitivity());
        assertEquals("", dto.homologationRemarks());
        assertEquals("24/03/2026", dto.homologationBeginDate());
        assertEquals("24/03/2027", dto.homologationEndDate());
    }

    @Test
    void testGetAppliAbsentesOscar() {
        // Arrange
        Map<String, Integer> applicationMap =
                Map.of(
                        "App1", 1,
                        "App2", 2,
                        "App3", 3);
        when(homologationMapper.getApplicationMap()).thenReturn(applicationMap);

        Homologation h1 = new Homologation();
        h1.setApsOscar("App1");

        Homologation h2 = new Homologation();
        h2.setApsOscar("AppAbsente1");

        Homologation h3 = new Homologation();
        h3.setApsOscar("App2");

        Homologation h4 = new Homologation();
        h4.setApsOscar("AppAbsente2");

        Homologation h5 = new Homologation();
        h5.setApsOscar("AppAbsente1"); // Doublon

        when(homologationMapper.getAllHomologationSep()).thenReturn(List.of(h1, h2, h3, h4, h5));

        // Act
        List<String> result = homologationService.getAppliAbsentesOscar();

        // Assert
        assertThat(result).hasSize(2).containsExactly("AppAbsente1", "AppAbsente2");
        verify(homologationMapper).getApplicationMap();
        verify(homologationMapper).getAllHomologationSep();
    }
}
