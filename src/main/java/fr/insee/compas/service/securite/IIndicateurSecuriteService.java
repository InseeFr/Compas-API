package fr.insee.compas.service.securite;

import java.util.Date;
import java.util.List;

import fr.insee.compas.view.IndicateurSecuriteView;

public interface IIndicateurSecuriteService {
    List<IndicateurSecuriteView> getIndicateursApplicationView(Date dateReference, Date datePassee);

    List<IndicateurSecuriteView> getIndicateursModuleView(Date dateReference, Date datePassee);
}
