package fr.insee.compas.service.devops.update.strat;

import static fr.insee.compas.util.DevopsConstantes.calculateRoundedAverage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

@Component("NbrMep")
@RequiredArgsConstructor
@Slf4j
public class UpdateDevopsNbrMep extends UpdateDevopsStrategy implements IUpdateDevopsStrategy {
    private final SaveTFByIndicator saveTFByIndicator;
    private final IEventManager eventObserverManager;

    @Override
    public void updateDevops(
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<Module> modules,
            Map<String, List<ModuleHistorique>> moduleHistoriques) {
        if (modules == null || modules.isEmpty()) return;
        Map<Integer, List<Integer>> valuesByApp = new HashMap<>();
        modules.forEach(
                module -> {
                    try {
                        int value = this.getValue(module);
                        this.saveModule(module, value, valuesByApp);
                    } catch (Exception e) {
                        log.error("Erreur module {} : {}", module.getId(), e.getMessage());
                        eventObserverManager.notifyObservers(
                                EventTypeObserver.EVENT_TYPE_ERROR,
                                "Update-Devops-NbrMep : Erreur module "
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
                    IndicateurType.NBR_JOUR_MEP,
                    BigDecimal.valueOf(value),
                    SourceType.OSCAR);
            valuesByApp
                    .computeIfAbsent(module.getIdApplication(), k -> new ArrayList<>())
                    .add(value);
        } catch (Exception e) {
            log.error("Erreur sauvegarde module {}", module.getId(), e);
            eventObserverManager.notifyObservers(
                    EventTypeObserver.EVENT_TYPE_ERROR,
                    "Update-Devops-NbrMep :Erreur sauvegarde module "
                            + module.getId()
                            + " pour le nbr MEP: "
                            + e.getMessage());
        }
    }

    public void saveApplication(Map<Integer, List<Integer>> valuesByApp) {
        try {
            valuesByApp.forEach(
                    (k, v) -> {
                        int avg = calculateRoundedAverage(v);
                        saveTFByIndicator.saveByIndicator(
                                null,
                                k,
                                IndicateurType.NBR_JOUR_MEP,
                                BigDecimal.valueOf(avg),
                                SourceType.OSCAR);
                    });
        } catch (Exception e) {
            log.error("Erreur sauvegarde applications", e);
            eventObserverManager.notifyObservers(
                    EventTypeObserver.EVENT_TYPE_ERROR,
                    "Update-Devops-NbrMep :Erreur sauvegarde applications : pour le nbr MEP"
                            + e.getMessage());
        }
    }

    private int getValue(Module module) {
        if (DevopsConstantes.SAISIE_MANUELLE.equals(module.getSourceCreation())) {
            return IndicatorSpecialValue.SO.getCode();
        }
        if (DevopsConstantes.EN_DEVELOPPEMENT.equals(module.getStatut())) {
            return IndicatorSpecialValue.SO.getCode();
        }

        LocalDate dateLivraison = module.getDateDerniereLivraisonEnProduction();
        return (dateLivraison == null)
                ? IndicatorSpecialValue.NR.getCode()
                : (int) ChronoUnit.DAYS.between(dateLivraison, LocalDate.now());
    }
}
