package fr.insee.compas.service.conversion.strategie;

import org.springframework.stereotype.Component;

@Component("strategieCloudConversion")
public class StrategieCloudConversion implements IConversionStrategie<String, Integer> {
    @Override
    public String conversion(Integer valeur) {
        return switch (valeur) {
            case 1 -> "A instruire";
            case 2 -> "En cours";
            case 3 -> "Validée";
            default ->
                    throw new IllegalStateException(
                            "Valeur improbable pour la stratégie cloud : " + valeur);
        };
    }
}
