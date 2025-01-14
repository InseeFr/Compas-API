package fr.insee.compas.mapper;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.view.IndicateurApplicationGreenITView;

@Component
public class IndicateurApplicationGreenITViewMapper {
    public IndicateurApplicationGreenITView toView(IndicateurApplicationGreenIT ind) {
        if (ind != null) {
            final IndicateurApplicationGreenITView view = new IndicateurApplicationGreenITView();
            view.setApplicationId(ind.getApplicationId());
            view.setApplicationName(ind.getApplicationName());
            view.setRamAllocated(ind.getRamAllocated());
            view.setRamUsed(ind.getRamUsed());
            view.setDiskAllocated(ind.getDiskAllocated());
            view.setDiskUsed(ind.getDiskUsed());
            view.setNbVm(ind.getNbVm());
            return view;
        }
        return null;
    }
}
