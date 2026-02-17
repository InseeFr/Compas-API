package fr.insee.compas.service.conversion.strategie;

import org.springframework.stereotype.Component;

@Component("maturiteCloudConversion")
public class MaturiteCloudConversion implements IConversionStrategie<String, Integer> {
    @Override
    public String conversion(Integer value) {
        return switch (value) {
            case 1 -> "E";
            case 2 -> "D";
            case 3 -> "C";
            case 4 -> "B";
            case 5 -> "A";
            default -> "SO";
        };
    }
}
