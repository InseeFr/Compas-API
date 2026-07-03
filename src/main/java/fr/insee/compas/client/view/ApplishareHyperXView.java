package fr.insee.compas.client.view;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplishareHyperXView implements Serializable {
    private String application;
    private String plateforme;

    @JsonProperty("taille_applishare_go")
    private BigDecimal tailleApplishareGo;

    @JsonProperty("taille_applishare_tot_go")
    private BigDecimal tailleApplishareTotGo;
}
