package fr.insee.compas.service.homologation;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.client.view.ApplicationTechnique;
import fr.insee.compas.dto.HomologationDoublonsGristDto;
import fr.insee.compas.dto.HomologationDto;
import fr.insee.compas.model.homologation.Homologation;
import fr.insee.compas.service.OscarService;

@ExtendWith(MockitoExtension.class)
public class HomologationServiceTest {

    @Mock private HomologationMapper homologationMapper;

    @InjectMocks private HomologationService homologationService;

    @Mock private OscarService oscarService;

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

        ApplicationTechnique app = new ApplicationTechnique();
        app.setId(1);
        app.setNom("une app");

        when(homologationMapper.getAllHomologationSep()).thenReturn(List.of(h));
        when(oscarService.findApplicationByName("une app")).thenReturn(app);

        List<HomologationDto> result = homologationService.getAllHomologation();

        assertEquals(1, result.size());

        HomologationDto dto = result.get(0);
        assertEquals(1, dto.applicationId());
        assertEquals("une app", dto.nomApp());
        assertEquals("homologuée", dto.statutHomologation());
        assertEquals("1", dto.sensitivity());
        assertEquals("", dto.homologationRemarks());
        assertEquals("24/03/2026", dto.homologationBeginDate());
        assertEquals("24/03/2027", dto.homologationEndDate());

        verify(oscarService, times(1)).findApplicationByName("une app");
    }

    @Test
    void testGetAppliAbsentesOscar() {

        Homologation h1 = new Homologation();
        h1.setApsOscar("app1");

        Homologation h2 = new Homologation();
        h2.setApsOscar("appabsente1");

        Homologation h3 = new Homologation();
        h3.setApsOscar("app2");

        Homologation h4 = new Homologation();
        h4.setApsOscar("appabsente2");

        Homologation h5 = new Homologation();
        h5.setApsOscar("appabsente1");

        when(homologationMapper.getAllHomologationSep()).thenReturn(List.of(h1, h2, h3, h4, h5));

        ApplicationTechnique app1 = new ApplicationTechnique();
        app1.setId(1);
        app1.setNom("app1");

        ApplicationTechnique app2 = new ApplicationTechnique();
        app2.setId(2);
        app2.setNom("app2");

        when(oscarService.findApplicationByName("app1")).thenReturn(app1);
        when(oscarService.findApplicationByName("app2")).thenReturn(app2);
        when(oscarService.findApplicationByName("appabsente1")).thenReturn(null);
        when(oscarService.findApplicationByName("appabsente2")).thenReturn(null);

        List<String> result = homologationService.getAppliAbsentesOscar();

        assertThat(result).hasSize(2).containsExactlyInAnyOrder("appabsente1", "appabsente2");

        verify(oscarService).findApplicationByName("app1");
        verify(oscarService).findApplicationByName("app2");
        verify(oscarService).findApplicationByName("appabsente1");
        verify(oscarService).findApplicationByName("appabsente2");
    }

    @Test
    void getApplicationGrist_retourneNomsUniquesTries() {

        Homologation h1 = new Homologation();
        h1.setApsOscar("app2");

        Homologation h2 = new Homologation();
        h2.setApsOscar("app1");

        Homologation h3 = new Homologation();
        h3.setApsOscar("app2"); // Doublon

        Homologation h4 = new Homologation();
        h4.setApsOscar("app3");

        when(homologationMapper.getAllHomologationSep()).thenReturn(List.of(h1, h2, h3, h4));

        List<String> result = homologationService.getApplicationGrist();

        assertEquals(3, result.size());
        assertEquals(List.of("app1", "app2", "app3"), result);
        verify(homologationMapper).getAllHomologationSep();
    }

    @Test
    void getDoublonsGrist_retourneApplicationsDansPlusieursSI() {
        Homologation h1 = new Homologation();
        h1.setApsOscar("arc");
        h1.setNomSI("SI Collecte");

        Homologation h2 = new Homologation();
        h2.setApsOscar("arc");
        h2.setNomSI("SI Diffusion");

        Homologation h3 = new Homologation();
        h3.setApsOscar("app-unique");
        h3.setNomSI("SI A");

        when(homologationMapper.getAllHomologationSep()).thenReturn(List.of(h1, h2, h3));

        List<HomologationDoublonsGristDto> result = homologationService.getDoublonsGrist();

        assertEquals(1, result.size());

        HomologationDoublonsGristDto doublon = result.get(0);
        assertEquals("arc", doublon.getNomApplication());
        assertEquals(2, doublon.getNombreOccurrences());
        assertTrue(doublon.getListeSI().contains("SI Collecte"));
        assertTrue(doublon.getListeSI().contains("SI Diffusion"));
    }
}
