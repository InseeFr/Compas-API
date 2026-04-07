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

import lombok.RequiredArgsConstructor;

@Component("NbrMep")
@RequiredArgsConstructor
public class UpdateDevopsNbrMep extends UpdateDevopsStrategy implements IUpdateDevopsStrategy {
    private final SaveTFByIndicator saveTFByIndicator;

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
                    int value = this.getValue(module);
                    this.saveModule(module, value, valuesByApp);
                });
        this.saveApplication(valuesByApp);
    }

    public void saveModule(Module module, int value, Map<Integer, List<Integer>> valuesByApp) {
        saveTFByIndicator.saveByIndicator(
                module.getId(),
                module.getIdApplication(),
                IndicateurType.NBR_JOUR_MEP,
                BigDecimal.valueOf(value),
                SourceType.OSCAR);
        valuesByApp.computeIfAbsent(module.getIdApplication(), k -> new ArrayList<>()).add(value);
    }

    public void saveApplication(Map<Integer, List<Integer>> valuesByApp) {
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
