package fr.insee.compas.client.view;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VmOscarView {
    private Integer idApplication;
    private String nom;
    private Integer idModule;
    private String plateforme;
}
