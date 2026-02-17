package fr.insee.compas.service.conversion.strategie;

import org.springframework.stereotype.Component;

@Component("maturiteEnvCibleConversion")
public class MaturiteEnvCibleConversion implements IConversionStrategie<String, Integer> {
    @Override
    public String conversion(Integer value) {
        return switch (value) {
            case 1 -> "Kube";
            case 2 -> "Puppet";
            case 3 -> "Cloud Externe";
            case 4 -> "Autre";
            default ->
                    throw new IllegalStateException(
                            "Valeur improbable pour l'env cible : " + value);
        };
    }
}
