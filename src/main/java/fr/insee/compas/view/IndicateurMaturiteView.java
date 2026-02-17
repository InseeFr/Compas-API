package fr.insee.compas.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndicateurMaturiteView {
    private Integer idApp;
    private Integer idModule;
    private String appName;
    private String moduleName;
    private Boolean isModule;
    private String serviceName;
    private String domaineDev;
    private String domaineFonctionnel;
    private String tauxCloud;
    private String envActuelProd;
    private String envCibleProd;
    private String ecartCible;
    private String stratCloud;
    private String commentaire;
    private String maturiteCloud;
}
