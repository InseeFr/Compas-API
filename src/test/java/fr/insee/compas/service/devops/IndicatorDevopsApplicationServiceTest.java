package fr.insee.compas.service.devops;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.view.IndicateurDevopsView;

class IndicatorDevopsApplicationServiceTest {

    private OscarService oscarService;
    private TableFaitsService tableFaitsService;
    private IndicatorDevopsApplicationService service;

    @BeforeEach
    void setup() {
        oscarService = mock(OscarService.class);
        tableFaitsService = mock(TableFaitsService.class);
        service = new IndicatorDevopsApplicationService(oscarService, tableFaitsService);
    }

    @Test
    void testGetIndicateurNiveauApplication_basicMapping() {
        // Préparation de 2 applications
        Application app1 = new Application();
        app1.setIdApplication(1);
        app1.setAppName("AppA");
        app1.setSndi("SNDI-A");
        app1.setDomaineSndi("DS-A");
        app1.setDomaineFonctionnel("DF-A");

        Application app2 = new Application();
        app2.setIdApplication(2);
        app2.setAppName("AppB");
        app2.setSndi("SNDI-B");
        app2.setDomaineSndi("DS-B");
        app2.setDomaineFonctionnel("DF-B");

        when(oscarService.getApplications()).thenReturn(List.of(app1, app2));

        // Préparation des indicateurs bruts
        IndicateurDevopsView view1 = new IndicateurDevopsView();
        view1.setDistanceCount("15"); // A
        view1.setNbDeploymentCount("0"); // E
        view1.setNbContributorCount("2"); // C

        IndicateurDevopsView view2 = new IndicateurDevopsView();
        view2.setDistanceCount("-1"); // SO
        view2.setNbDeploymentCount("-2"); // NR
        view2.setNbContributorCount("4"); // A

        Map<Integer, IndicateurDevopsView> mapQualite = new HashMap<>();
        mapQualite.put(1, view1);
        mapQualite.put(2, view2);
        when(tableFaitsService.getIndicateurApplicationDevops()).thenReturn(mapQualite);

        // Appel du service
        List<IndicateurDevopsView> resultat = service.getIndicateurNiveauApplication();

        // Vérifications sur app1
        Optional<IndicateurDevopsView> r1 =
                resultat.stream().filter(r -> "AppA".equals(r.getApplicationName())).findFirst();
        assertTrue(r1.isPresent());
        assertEquals("A", r1.get().getLettreDistanceCount()); // 15 → A
        assertEquals("E", r1.get().getLettreDeploymentCount()); // 0 → E
        assertEquals("C", r1.get().getLettreContributorCount()); // 2 → C
        assertEquals("AppA", r1.get().getApplicationName());
        assertEquals("SNDI-A", r1.get().getSndi());

        // Vérifications sur app2
        Optional<IndicateurDevopsView> r2 =
                resultat.stream().filter(r -> "AppB".equals(r.getApplicationName())).findFirst();
        assertTrue(r2.isPresent());
        assertEquals("SO", r2.get().getLettreDistanceCount()); // -1 → SO
        assertEquals("NR", r2.get().getLettreDeploymentCount()); // -2 → NR
        assertEquals("A", r2.get().getLettreContributorCount()); // 4 → A
        assertEquals("AppB", r2.get().getApplicationName());
        assertEquals("SNDI-B", r2.get().getSndi());
    }

    @Test
    void testGetIndicateurNiveauApplication_nullIndicatorView() {
        // Application sans indicateur dans mapQualite
        Application app1 = new Application();
        app1.setIdApplication(10);
        app1.setAppName("NewApp");
        app1.setSndi("SNDI-X");
        app1.setDomaineSndi("DS-X");
        app1.setDomaineFonctionnel("DF-X");

        when(oscarService.getApplications()).thenReturn(List.of(app1));
        when(tableFaitsService.getIndicateurApplicationDevops()).thenReturn(new HashMap<>());

        List<IndicateurDevopsView> resultat = service.getIndicateurNiveauApplication();

        assertEquals(1, resultat.size());
        IndicateurDevopsView view = resultat.getFirst();
        assertEquals("NewApp", view.getApplicationName());
        assertEquals("SNDI-X", view.getSndi());
        assertEquals("DS-X", view.getDomaineSndi());
        assertEquals("DF-X", view.getDomaineFonctionnel());
        // Comme tout est null → NR pour chaque lettre
        assertEquals(Notation.NR.getGrade(), view.getLettreDistanceCount());
        assertEquals(Notation.NR.getGrade(), view.getLettreDeploymentCount());
        assertEquals(Notation.NR.getGrade(), view.getLettreContributorCount());
    }
}
