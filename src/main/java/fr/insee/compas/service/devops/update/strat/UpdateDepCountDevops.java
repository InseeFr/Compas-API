package fr.insee.compas.service.devops.update.strat;

import static fr.insee.compas.util.DevopsConstantes.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;
import fr.insee.compas.util.DevopsConstantes;
import fr.insee.compas.util.IndicatorSpecialValue;
import fr.insee.compas.util.SaveTFByIndicator;
import fr.insee.compas.util.observer.EventTypeObserver;
import fr.insee.compas.util.observer.IEventManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("CountDeploy")
@RequiredArgsConstructor
public class UpdateDepCountDevops extends UpdateDevopsStrategy implements IUpdateDevopsStrategy {

    private final SaveTFByIndicator saveTFByIndicator;
    private final IEventManager eventObserverManager;

    @Override
    public void updateDevops(
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<Module> modules,
            Map<String, List<ModuleHistorique>> moduleHistoriques) {
        LocalDateTime[] dates = normalizeDates(startDate, endDate);
        if (modules == null || modules.isEmpty()) return;
        Map<Integer, List<Integer>> valuesByApp = new HashMap<>();
        modules.forEach(
                module -> {
                    try {
                        int value = this.getValue(module, moduleHistoriques, dates);
                        this.saveModule(module, value, valuesByApp);
                    } catch (Exception e) {
                        log.error("Erreur module {} : {}", module.getId(), e.getMessage());
                        eventObserverManager.notifyObservers(
                                EventTypeObserver.EVENT_TYPE_ERROR,
                                "Update-Devops-DepCount : Erreur module "
                                        + module.getId()
                                        + " : "
                                        + e.getMessage());
                    }
                });
        this.saveApplication(valuesByApp);
    }

    public void saveModule(Module module, int value, Map<Integer, List<Integer>> valuesByApp) {
        try {
            saveTFByIndicator.saveByIndicator(
                    module.getId(),
                    module.getIdApplication(),
                    IndicateurType.DEPLOYMENT_COUNT,
                    BigDecimal.valueOf(value),
                    SourceType.OSCAR);
            valuesByApp
                    .computeIfAbsent(module.getIdApplication(), k -> new ArrayList<>())
                    .add(value);
        } catch (Exception e) {
            log.error("Erreur sauvegarde module {} : {}", module.getId(), e.getMessage());
            eventObserverManager.notifyObservers(
                    EventTypeObserver.EVENT_TYPE_ERROR,
                    "Update-Devops-DepCount : Erreur sauvegarde module "
                            + module.getId()
                            + " : "
                            + e.getMessage());
        }
    }

    public void saveApplication(Map<Integer, List<Integer>> valuesByApp) {
        valuesByApp.forEach(
                (k, v) -> {
                    try {
                        int avg = calculateRoundedAverage(v);
                        saveTFByIndicator.saveByIndicator(
                                null,
                                k,
                                IndicateurType.DEPLOYMENT_COUNT,
                                BigDecimal.valueOf(avg),
                                SourceType.OSCAR);
                    } catch (Exception e) {
                        log.error("Erreur sauvegarde application {} : {}", k, e.getMessage());
                        eventObserverManager.notifyObservers(
                                EventTypeObserver.EVENT_TYPE_ERROR,
                                "Update-Devops-DepCount : Erreur sauvegarde application "
                                        + k
                                        + " : "
                                        + e.getMessage());
                    }
                });
    }

    private int getValue(
            Module module,
            Map<String, List<ModuleHistorique>> allHistoriqueMap,
            LocalDateTime[] dates) {
        List<ModuleHistorique> historique = allHistoriqueMap.get(String.valueOf(module.getId()));
        if (DevopsConstantes.SAISIE_MANUELLE.equals(module.getSourceCreation())) {
            return IndicatorSpecialValue.NR.getCode();
        }

        if (DevopsConstantes.EN_DEVELOPPEMENT.equals(module.getStatut())) {
            return IndicatorSpecialValue.SO.getCode();
        }

        return (historique == null)
                ? IndicatorSpecialValue.NR.getCode()
                : (int)
                        historique.stream()
                                .filter(h -> isValidDeployment(h, dates[0], dates[1]))
                                .count();
    }
}
