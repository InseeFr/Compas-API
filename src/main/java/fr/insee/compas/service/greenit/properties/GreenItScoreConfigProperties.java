package fr.insee.compas.service.greenit.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fr.insee.compas.score")
public class GreenItScoreConfigProperties {

    private ConfigurationApplicationModule application;
    private ConfigurationApplicationModule module;

    @Getter
    @Setter
    public static class ConfigurationApplicationModule {

        private double consoMax;
        private double pressionMaxRam;
        private double pressionMaxCpu;
        private double pressionMaxDisk;
        private double consoFacteurUtilisation;
    }
}
