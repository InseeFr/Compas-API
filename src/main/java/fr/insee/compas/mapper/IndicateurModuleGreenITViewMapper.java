package fr.insee.compas.mapper;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.view.IndicateurModuleGreenITView;

@Component
public class IndicateurModuleGreenITViewMapper {
    public IndicateurModuleGreenITView toView(IndicateurModuleGreenIT ind) {
        if (ind != null) {
            final IndicateurModuleGreenITView view = new IndicateurModuleGreenITView();
            view.setCpuAllocated(ind.getCpuAllocated());
            view.setDiskAllocated(ind.getDiskAllocated());
            view.setRamAllocated(ind.getRamAllocated());
            view.setDiskUsed(ind.getDiskUsed());
            view.setNbVm(ind.getNbVm());
            view.setModuleId(ind.getModuleId());
            view.setModuleName(ind.getModuleName());
            return view;
        }
        return null;
    }
}
