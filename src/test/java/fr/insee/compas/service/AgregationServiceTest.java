package fr.insee.compas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Module;

public class AgregationServiceTest {

    private final AgregationService agregationService = new AgregationService();

    @Test
    public void calculAgregationSumTest() {
        Integer indicateur = 1;
        Module module1 =
                new Module(
                        1,
                        "module1",
                        1,
                        "application1",
                        "domaine1",
                        "fonctionnel1",
                        "keySonar1",
                        "sndi1");
        Module module2 =
                new Module(
                        2,
                        "module2",
                        1,
                        "application1",
                        "domaine1",
                        "keySonar1",
                        "fonctionnel1",
                        "sndi1");
        Module module3 =
                new Module(
                        3,
                        "module3",
                        1,
                        "application2",
                        "domaine1",
                        "fonctionnel1",
                        "keySonar1",
                        "sndi1");
        List<Module> modules = new ArrayList<>();
        modules.add(module1);
        modules.add(module2);
        modules.add(module3);
        TableFaits fait1 = new TableFaits(1, 1, null, new BigDecimal(12), null);
        TableFaits fait2 = new TableFaits(2, 1, null, new BigDecimal(10), null);
        Map<Integer, TableFaits> metrics = new HashMap<>();
        metrics.put(1, fait1);
        metrics.put(2, fait2);
        Map<Integer, TableFaits> result =
                agregationService.calculAgregationSum(indicateur, metrics, modules);
        assertEquals(1, result.size());
        assertEquals(new BigDecimal(22), result.get(1).getValeur());
    }
}
