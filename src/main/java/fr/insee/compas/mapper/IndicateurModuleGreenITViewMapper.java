package fr.insee.compas.mapper;

import java.util.Optional;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.view.IndicateurModuleGreenITView;

@Component
public class IndicateurModuleGreenITViewMapper {
    public Optional<IndicateurModuleGreenITView> toView(IndicateurModuleGreenIT ind) {
        return Optional.ofNullable(ind).map(this::mapToView);
    }

    private IndicateurModuleGreenITView mapToView(IndicateurModuleGreenIT ind) {
        return IndicateurModuleGreenITView.builder()
                .moduleId(ind.getModuleId())
                .moduleName(ind.getModuleName())
                .cpuAllocated(ind.getCpuAllocated() + " Mhz")
                .cpuMaxi(ind.getCpuMaxi() + " %")
                .diskAllocated(ind.getDiskAllocated() + " Go")
                .diskUsed(ind.getDiskUsed() + " %")
                .ramAllocated(ind.getRamAllocated() + "Go")
                .ramMaxi(ind.getRamMaxi() + " %")
                .conso(ind.getConso() + " Kwh ?")
                .nbVm(ind.getNbVm() + " vm")
                .build();
    }
}
