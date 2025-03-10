package fr.insee.compas.mapper;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.compas.dto.MetriqueApplicationDTO;
import fr.insee.compas.util.IndicateurViewUtil;
import fr.insee.compas.view.ApplicationConsommationElectriqueView;

@Component
public class ApplicationConsommationElectriqueViewMapper {
    public ApplicationConsommationElectriqueView toView(MetriqueApplicationDTO metrique) {
        return ApplicationConsommationElectriqueView.builder()
                .applicationId(metrique.getIdApplication())
                .date(metrique.getDate().toString())
                .conso(metrique.getTotalValeur() + " Wh")
                .lettreGreen(
                        IndicateurViewUtil.getGradeFromConsommationElectrique(
                                metrique.getTotalValeur()))
                .build();
    }
}
