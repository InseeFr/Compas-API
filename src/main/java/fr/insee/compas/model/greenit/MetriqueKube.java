package fr.insee.compas.model.greenit;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetriqueKube {
    private String namespace;
    private String namespaceOwner;
    private BigDecimal cpuUsed;
    private BigDecimal ramUsed;
    private BigDecimal s3Used;
    private BigDecimal pvcUsed;
    private BigDecimal nbPodMaxi;
    private String environnement;
}
