package fr.insee.compas.service.conversion.strategie;

import org.springframework.stereotype.Component;

@Component("maturiteZoneDeProdConversion")
public class MaturiteZoneDeProdConversion implements IConversionStrategie<String, String> {

    @Override
    public String conversion(String zoneDeProduction) {
        return "cloud".equals(zoneDeProduction) ? "Cloud Externe" : "Autre";
    }
}
