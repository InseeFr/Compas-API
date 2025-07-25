package fr.insee.compas.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.springframework.stereotype.Component;

import fr.insee.compas.logic.GreenItScoreCalculator;
import fr.insee.compas.model.greenit.GreenItScore;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.view.IndicateurModuleGreenITView;

@Component
public class IndicateurModuleGreenITViewMapper {

    private final GreenItScoreCalculator greenItScoreCalculator;

    public IndicateurModuleGreenITViewMapper(GreenItScoreCalculator greenItScoreCalculator) {
        super();
        this.greenItScoreCalculator = greenItScoreCalculator;
    }

    public Optional<IndicateurModuleGreenITView> toView(IndicateurModuleGreenIT ind) {
        return Optional.ofNullable(ind).map(this::mapToView);
    }

    private IndicateurModuleGreenITView mapToView(IndicateurModuleGreenIT ind) {
        final GreenItScore greenItScore = greenItScoreCalculator.compute(ind);
        return IndicateurModuleGreenITView.builder()
                .moduleId(ind.getModuleId())
                .moduleName(ind.getModuleName())
                .cpuAllocated(ind.getCpuAllocated() + " Mhz")
                .cpuMaxi(gestionPourcentageOuSansObjet(ind.getCpuMaxi()))
                .diskAllocated(ind.getDiskAllocated() + " Go")
                .diskUsed(gestionPourcentageOuSansObjet(ind.getDiskUsed()))
                .ramAllocated(ind.getRamAllocated() + " Go")
                .ramMaxi(gestionPourcentageOuSansObjet(ind.getRamMaxi()))
                .conso(ind.getConso() + " Wh")
                .nbVm(ind.getNbVm() + " vm")
                .consoScore(greenItScore.getConso().setScale(3, RoundingMode.UP).toString())
                .impactScore(greenItScore.getImpact().setScale(3, RoundingMode.UP).toString())
                .gaspillageScore(
                        greenItScore.getGaspillage().setScale(3, RoundingMode.UP).toString())
                .lettreGreen(greenItScoreCalculator.compute(ind).getGrade())
                .build();
    }

    private String gestionPourcentageOuSansObjet(BigDecimal b) {
        return b != null ? b + " %" : "SO";
    }
}
