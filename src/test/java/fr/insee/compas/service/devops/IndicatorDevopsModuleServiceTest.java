package fr.insee.compas.service.devops;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.view.IndicateurDevopsView;

class IndicatorDevopsModuleServiceTest {

    private OscarService oscarService;
    private TableFaitsService tableFaitsService;
    private IndicatorDevopsModuleService service;

    @BeforeEach
    void setup() {
        oscarService = mock(OscarService.class);
        tableFaitsService = mock(TableFaitsService.class);
        service = new IndicatorDevopsModuleService(oscarService, tableFaitsService);
    }

    @Test
    void testGetIndicateurNiveauModule_basicMapping() {
        // Prépare 2 modules
        Module module1 = new Module();
        module1.setId(1);
        module1.setIdApplication(101);
        module1.setAppName("AppM1");
        module1.setModName("Module1");
        module1.setSndi("SNDI-M1");
        module1.setDomaineSndi("DS-M1");
        module1.setDomaineFonctionnel("DF-M1");

        Module module2 = new Module();
        module2.setId(2);
        module2.setIdApplication(102);
        module2.setAppName("AppM2");
        module2.setModName("Module2");
        module2.setSndi("SNDI-M2");
        module2.setDomaineSndi("DS-M2");
        module2.setDomaineFonctionnel("DF-M2");

        when(oscarService.getModules()).thenReturn(List.of(module1, module2));

        // Prépare les indicateurs bruts
        IndicateurDevopsView view1 = new IndicateurDevopsView();
        view1.setDistanceCount("60"); // B
        view1.setNbDeploymentCount("7"); // A
        view1.setNbContributorCount("1"); // D

        IndicateurDevopsView view2 = new IndicateurDevopsView();
        view2.setDistanceCount("-1"); // SO
        view2.setNbDeploymentCount("0"); // E
        view2.setNbContributorCount("4"); // A

        Map<Integer, IndicateurDevopsView> mapDevops = new HashMap<>();
        mapDevops.put(1, view1);
        mapDevops.put(2, view2);

        when(tableFaitsService.getIndicateurModuleDevops()).thenReturn(mapDevops);

        // Appel du service
        List<IndicateurDevopsView> resultat = service.getIndicateurNiveauModule();

        // Vérifications sur module1
        Optional<IndicateurDevopsView> r1 =
                resultat.stream().filter(r -> "Module1".equals(r.getModuleName())).findFirst();
        assertTrue(r1.isPresent());
        assertEquals("B", r1.get().getLettreDistanceCount()); // 60 → B
        assertEquals("A", r1.get().getLettreDeploymentCount()); // 7  → D
        assertEquals("D", r1.get().getLettreContributorCount()); // 1  → D
        assertEquals("Module1", r1.get().getModuleName());
        assertEquals("AppM1", r1.get().getApplicationName());
        assertEquals("SNDI-M1", r1.get().getSndi());

        // Vérifications sur module2
        Optional<IndicateurDevopsView> r2 =
                resultat.stream().filter(r -> "Module2".equals(r.getModuleName())).findFirst();
        assertTrue(r2.isPresent());
        assertEquals("SO", r2.get().getLettreDistanceCount()); // -1 → SO
        assertEquals("E", r2.get().getLettreDeploymentCount()); // 0  → X
        assertEquals("A", r2.get().getLettreContributorCount()); // 4  → A
        assertEquals("Module2", r2.get().getModuleName());
        assertEquals("AppM2", r2.get().getApplicationName());
        assertEquals("SNDI-M2", r2.get().getSndi());
    }

    @Test
    void testGetIndicateurNiveauModule_nullIndicatorView() {
        // Module sans indicateur dans mapDevops
        Module module = new Module();
        module.setId(42);
        module.setIdApplication(999);
        module.setAppName("AppX");
        module.setModName("ModuleX");
        module.setSndi("SNDI-X");
        module.setDomaineSndi("DS-X");
        module.setDomaineFonctionnel("DF-X");

        when(oscarService.getModules()).thenReturn(List.of(module));
        when(tableFaitsService.getIndicateurModuleDevops()).thenReturn(new HashMap<>());

        List<IndicateurDevopsView> resultat = service.getIndicateurNiveauModule();

        assertEquals(1, resultat.size());
        IndicateurDevopsView view = resultat.get(0);
        assertEquals("ModuleX", view.getModuleName());
        assertEquals("AppX", view.getApplicationName());
        assertEquals("SNDI-X", view.getSndi());
        assertEquals("DS-X", view.getDomaineSndi());
        assertEquals("DF-X", view.getDomaineFonctionnel());
        // Comme tout est null → NR pour chaque lettre
        assertEquals(Notation.NR.getGrade(), view.getLettreDistanceCount());
        assertEquals(Notation.NR.getGrade(), view.getLettreDeploymentCount());
        assertEquals(Notation.NR.getGrade(), view.getLettreContributorCount());
    }
}
