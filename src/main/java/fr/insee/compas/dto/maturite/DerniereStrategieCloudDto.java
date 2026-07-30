package fr.insee.compas.dto.maturite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DerniereStrategieCloudDto {
    private Integer idMod;

    private String strategieCloud;

    private String envCibleProd;

    private String commentaire;
}
