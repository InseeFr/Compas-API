package fr.insee.compas.client.view;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ModuleOscarView implements Serializable {
    /** */
    @Serial private static final long serialVersionUID = 1L;

    private Integer id;
    private String nomTechnique;
    private String nom;
    private String statut;
    private String zoneProduction;
    private String projectKeySonar;
    private ApplicationTechnique applicationTechnique;
    private String urlCodeSource;
}
