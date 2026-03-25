package fr.insee.compas.service.conversion.strategie;

import static fr.insee.compas.util.MaturiteConstantes.NON_DEFINI;

import org.springframework.stereotype.Component;

@Component("maturiteEnvCibleConversion")
public class MaturiteEnvCibleConversion implements IConversionStrategie<String, Integer> {
    @Override
    public String conversion(Integer value) {
        return switch (value) {
            case 0 -> NON_DEFINI;
            case 1 -> "Kube";
            case 2 -> "VM";
            case 3 -> "Cloud Externe";
            case 4 -> "Autre";
            default ->
                    throw new IllegalStateException(
                            "Valeur improbable pour l'env cible : " + value);
        };
    }
}
