package fr.insee.compas.client.view;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KubeOscarView {

    private Integer idApplication;
    private String namespace;
}
