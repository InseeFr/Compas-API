package fr.insee.compas.dto.maturite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MaturiteIndicateurDto {
    private Integer idMod;
    private Integer idApp;
    private String nameMod;
    private String nameApp;
    private String domaineSndi;
    private String domaineDev;
    private String domaineFonctionnel;
    private String tauxCloudProd;
    private String envActuelProd;
    private String envCibleProd;
    private String ecartCible;
    private String strategieCloud;
    private String commentaire;
    private String maturite;
}
