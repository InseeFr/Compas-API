package fr.insee.compas.client.view;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VmOscarView {
    private Integer idApplication;
    private String nom;
    private Integer idModule;
    private String plateforme;
}
