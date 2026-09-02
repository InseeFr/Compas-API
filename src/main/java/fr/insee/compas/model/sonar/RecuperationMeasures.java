package fr.insee.compas.model.sonar;

import fr.insee.compas.model.compas.IndicateurSonar;
import fr.insee.compas.util.sonar.ComparatorUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.insee.compas.util.sonar.NullSafetyUtils.getOrDefault;

@Data
@Slf4j
public class RecuperationMeasures {

    private Component component;

    public static RecuperationMeasures concatenationMeasures(RecuperationMeasures measures1, RecuperationMeasures measures2) {
        var measures1Component = getOrDefault(() -> measures1.getComponent().getMeasures(), new ArrayList<Measure>());
        var measure2Component = getOrDefault(() -> measures2.getComponent().getMeasures(), new ArrayList<Measure>());


        if (measures1Component == null || measures1Component.isEmpty()) {
            return measures2;
        }
        if (measure2Component == null) {
            return measures1;
        }
        var measure1AsMap = measures1.getComponent().getMeasures().stream()
                .collect(Collectors.toMap(Measure::getMetric, Function.identity()));
        for (var measure2 : measure2Component) {
            var metric = measure2.getMetric();
            var measure1 = measure1AsMap.get(metric);

            if (measure1 == null) {
                measures1
                        .getComponent()
                        .getMeasures()
                        .add(new Measure(measure2.getMetric(), measure2.getValue()));
                continue;
            }
            if (IndicateurSonar.FIABILITE.getKey().equalsIgnoreCase(metric)) {
                measure1.setValue(ComparatorUtils.getMaxLetter(measure1.getValue(), measure2.getValue()));
            } else {
                try {
                    measure1.setValue(String.valueOf(ComparatorUtils.getMaxNumber(measure1.getValue(), measure2.getValue())));
                } catch (NumberFormatException e) {
                    log.error("Erreur de conversion numérique pour le metric {}", metric);
                }
            }
        }
        return measures1;
    }
}
