package fr.insee.compas.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import fr.insee.compas.client.view.ApplicationTechnique;
import fr.insee.compas.model.homologation.Homologation;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.homologation.HomologationMapper;

@ExtendWith(MockitoExtension.class)
public class HomologationMapperTest {
    Map<String, Integer> resultMap = new HashMap<>();

    @Mock private OscarService oscarService;

    @Mock private RestTemplate restTemplate;

    @InjectMocks private HomologationMapper homologationMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(homologationMapper, "urlGrist", "http://urltest.fr");
        ReflectionTestUtils.setField(homologationMapper, "gristToken", "tokenTest");
        resultMap.put("app1", 1);
        resultMap.put("app2", 2);
    }

    @Test
    void buildHomologation_non() {

        Homologation h = new Homologation();
        h.setStatutHomologation("non");
        h.setSensitivity("1");
        h.setHomologationRemarks("");

        Homologation result = homologationMapper.buildHomologation(h, "uneapp");

        assertEquals("uneapp", result.getApsOscar());
        assertEquals("non", result.getStatutHomologation());
        assertEquals("non", result.getHomologationSI());
        assertEquals("1", result.getSensitivity());
        assertEquals("", result.getHomologationRemarks());
        assertNull(result.getHomologationBeginDate());
        assertNull(result.getHomologationEndDate());
    }

    @Test
    void buildHomologation_complete() {

        Homologation h = new Homologation();
        h.setStatutHomologation("complète");
        h.setSensitivity("2");
        h.setHomologationBeginDate("24/03/2026");
        h.setHomologationEndDate("24/03/2027");
        h.setHomologationRemarks("");

        Homologation result = homologationMapper.buildHomologation(h, "uneapp");

        assertEquals("uneapp", result.getApsOscar());
        assertEquals("homologuée", result.getStatutHomologation());
        assertEquals("complète", result.getHomologationSI());
        assertEquals("2", result.getSensitivity());
        assertEquals("", result.getHomologationRemarks());
        assertEquals("24/03/2026", result.getHomologationBeginDate());
        assertEquals("24/03/2027", result.getHomologationEndDate());
    }

    @Test
    void buildHomologation_partielle_homologuee() {

        Homologation h = new Homologation();
        h.setStatutHomologation("partielle");
        h.setHomologationSI("app1, app2");
        h.setSensitivity("4");
        h.setHomologationBeginDate("24/03/2026");
        h.setHomologationEndDate("24/03/2027");
        h.setHomologationRemarks("");

        Homologation result = homologationMapper.buildHomologation(h, "app1");

        assertEquals("homologuée", result.getStatutHomologation());
        assertEquals("24/03/2026", result.getHomologationBeginDate());
        assertEquals("24/03/2027", result.getHomologationEndDate());
    }

    @Test
    void buildHomologation_partielle_nonhomologuee() {

        Homologation h = new Homologation();
        h.setStatutHomologation("partielle");
        h.setHomologationSI("app1, app2");
        h.setSensitivity("nd");
        h.setHomologationRemarks("");

        Homologation result = homologationMapper.buildHomologation(h, "app3");

        assertEquals("app3", result.getApsOscar());
        assertEquals("non", result.getStatutHomologation());
        assertEquals("partielle", result.getHomologationSI());
        assertNull(result.getHomologationBeginDate());
        assertNull(result.getHomologationEndDate());
        assertEquals("", result.getHomologationRemarks());
    }

    @Test
    void getApplicationMap_shouldMapCorrectly() {

        ApplicationTechnique app1 = new ApplicationTechnique();
        app1.setNom(" APP1 ");
        app1.setId(1);

        ApplicationTechnique app2 = new ApplicationTechnique();
        app2.setNom("app2");
        app2.setId(2);

        ApplicationTechnique app3 = new ApplicationTechnique();
        app3.setNom(null);

        when(oscarService.getApplicationsTechniques()).thenReturn(List.of(app1, app2, app3));

        Map<String, Integer> result = homologationMapper.getApplicationMap();

        assertEquals(2, result.size());
        assertEquals(1, result.get("app1"));
        assertEquals(2, result.get("app2"));
    }

    @Test
    void normalizeTest() {
        String result = homologationMapper.normalize("TEST");
        assertEquals("test", result);
    }

    @Test
    void getAllHomologationSep() {

        HomologationMapper spy = Mockito.spy(homologationMapper);

        Homologation h = new Homologation();
        h.setApsOscar("app1,app2");
        h.setStatutHomologation("non");

        Mockito.doReturn(List.of(h)).when(spy).getAllHomologationCsv();

        List<Homologation> result = spy.getAllHomologationSep();

        assertEquals(2, result.size());
        assertEquals("app1", result.get(0).getApsOscar());
        assertEquals("app2", result.get(1).getApsOscar());
    }

    @Test
    void getAllHomologationCsv() {
        String csv =
                "Applications_Oscar,Sensibilite_SI,Statut_homologation,Remarques\n"
                        + "app1,1,non,OK\n"
                        + "app2,2,complète,OK\n";

        when(restTemplate.exchange(
                        eq("http://urltest.fr"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(String.class)))
                .thenReturn(ResponseEntity.ok(csv));

        List<Homologation> result = homologationMapper.getAllHomologationCsv();

        assertEquals(2, result.size());
        assertEquals("app1", result.get(0).getApsOscar());
        assertEquals("app2", result.get(1).getApsOscar());
        assertEquals("1", result.get(0).getSensitivity());
        assertEquals("non", result.get(0).getStatutHomologation());
    }
}
