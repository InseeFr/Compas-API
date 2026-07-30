package fr.insee.compas.service.maturitecloud.indicateur;

import java.util.List;

import fr.insee.compas.dto.maturite.DerniereStrategieCloudDto;
import fr.insee.compas.view.IndicateurMaturiteView;

public interface IMaturiteIndicateur {
    List<IndicateurMaturiteView> getIndicateurMaturite();
    DerniereStrategieCloudDto getDerniereStrategieCloud(Integer idModule);

}
