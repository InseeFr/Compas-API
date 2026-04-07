package fr.insee.compas.service.devops.update.strat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;

public interface IUpdateDevopsStrategy {
    void updateDevops(
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<Module> moduleList,
            Map<String, List<ModuleHistorique>> moduleHistoriques);
}
