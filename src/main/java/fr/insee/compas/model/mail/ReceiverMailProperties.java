package fr.insee.compas.model.mail;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "receiver.mail")
@Getter
@Setter
public class ReceiverMailProperties {
    private List<Responsable> responsable;
    private List<Responsable> responsableAdj;

    @Getter
    @Setter
    public static class Responsable {
        private String sndi;
        private String mail;
    }
}
