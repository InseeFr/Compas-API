package fr.insee.compas.mapper;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.util.IndicateurViewUtil;
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
                .cpuMaxi(gestionPourcentageOuSansObjet(ind.getCpuMaxi()))
                .diskAllocated(ind.getDiskAllocated() + " Go")
                .diskUsed(gestionPourcentageOuSansObjet(ind.getDiskUsed()))
                .ramAllocated(ind.getRamAllocated() + "Go")
                .ramMaxi(gestionPourcentageOuSansObjet(ind.getRamMaxi()))
                .conso(ind.getConso() + " Wh")
                .nbVm(ind.getNbVm() + " vm")
                .lettreGreen(IndicateurViewUtil.getGradeFromConsommationElectrique(ind.getConso()))
                .build();
    }

    private String gestionPourcentageOuSansObjet(BigDecimal b) {
        return b != null ? b + " %" : "SO";
    }
}
