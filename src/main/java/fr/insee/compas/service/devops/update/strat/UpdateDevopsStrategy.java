package fr.insee.compas.service.devops.update.strat;

import java.util.List;
import java.util.Map;

import fr.insee.compas.model.oscar.Module;

public abstract class UpdateDevopsStrategy {
    public abstract void saveModule(
            Module module, int value, Map<Integer, List<Integer>> valuesByApp);

    public abstract void saveApplication(Map<Integer, List<Integer>> valuesByApp);
}
