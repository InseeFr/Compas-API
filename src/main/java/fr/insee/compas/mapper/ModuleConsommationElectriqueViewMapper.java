package fr.insee.compas.mapper;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.compas.dto.MetriqueModuleDTO;
import fr.insee.compas.util.IndicateurViewUtil;
import fr.insee.compas.view.ModuleConsommationElectriqueView;

@Component
public class ModuleConsommationElectriqueViewMapper {
    public ModuleConsommationElectriqueView toView(MetriqueModuleDTO metrique) {
        return ModuleConsommationElectriqueView.builder()
                .moduleId(metrique.getIdModule())
                .date(metrique.getDate().toString())
                .conso(metrique.getTotalValeur() + " Wh")
                .lettreGreen(
                        IndicateurViewUtil.getGradeFromConsommationElectrique(
                                metrique.getTotalValeur()))
                .build();
    }
}
