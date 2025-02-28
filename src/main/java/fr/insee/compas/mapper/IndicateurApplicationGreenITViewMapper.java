package fr.insee.compas.mapper;

import java.util.Optional;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.view.IndicateurApplicationGreenITView;

@Component
public class IndicateurApplicationGreenITViewMapper {
    public Optional<IndicateurApplicationGreenITView> toView(IndicateurApplicationGreenIT ind) {
        return Optional.ofNullable(ind).map(this::mapToView);
    }

    private IndicateurApplicationGreenITView mapToView(IndicateurApplicationGreenIT indicateur) {
        return IndicateurApplicationGreenITView.builder()
                .applicationId(indicateur.getApplicationId())
                .applicationName(indicateur.getApplicationName())
                .ramAllocated(indicateur.getRamAllocated() + " Go")
                .ramMaxi(indicateur.getRamMaxi() + " %")
                .diskAllocated(indicateur.getDiskAllocated() + " Go")
                .diskUsed(indicateur.getDiskUsed() + " %")
                .cpuAllocated(indicateur.getCpuAllocated() + " Mhz")
                .cpuMaxi(indicateur.getCpuMaxi() + " %")
                .conso(indicateur.getConso() + " Wh")
                .nbVm(indicateur.getNbVm() + " vm")
                .build();
    }
}
