package fr.insee.compas.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Module;

@Service
public class AgregationService {

    /**
     * Méthode faisant la somme des modules pour agréger au niveau application. Elle renvoie une Map
     * key : idapplication valeur : table faits avec la somme de l'indicateur
     *
     * @param indicateur
     * @param metrics
     * @param modules
     * @return
     */
    public Map<Integer, TableFaits> calculAgregationSum(
            Integer indicateur, Map<Integer, TableFaits> metrics, List<Module> modules) {

        Map<Integer, TableFaits> result = new HashMap<>();

        for (Module module : modules) {
            TableFaits table =
                    new TableFaits(module.getIdApplication(), indicateur, null, null, null);
            if (!result.containsKey(module.getIdApplication())) {

                if (metrics.get(module.getId()) != null) {
                    table.setValeur(metrics.get(module.getId()).getValeur());
                    result.put(module.getIdApplication(), table);
                }
            } else {
                if (metrics.get(module.getId()) != null) {
                    int somme =
                            metrics.get(module.getId()).getValeur().intValue()
                                    + result.get(module.getIdApplication()).getValeur().intValue();
                    table.setValeur(BigDecimal.valueOf(somme));
                    result.put(module.getIdApplication(), table);
                }
            }
        }

        return result;
    }
}
