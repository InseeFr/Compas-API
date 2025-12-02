package fr.insee.compas.mapper;

import java.math.RoundingMode;
import java.util.Optional;

import org.springframework.stereotype.Component;

import fr.insee.compas.logic.GreenItScoreCalculator;
import fr.insee.compas.model.greenit.GreenItScore;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.util.GreenITutils;
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
                .cpuAllocated(GreenITutils.normalizeString(ind.getCpuAllocated()))
                .cpuMaxi(GreenITutils.gestionPourcentageOuSansObjet(ind.getCpuMaxi()))
                .diskAllocated(GreenITutils.normalizeString(ind.getDiskAllocated()))
                .diskUsed(GreenITutils.gestionPourcentageOuSansObjet(ind.getDiskUsed()))
                .ramAllocated(GreenITutils.normalizeString(ind.getRamAllocated()))
                .ramMaxi(GreenITutils.gestionPourcentageOuSansObjet(ind.getRamMaxi()))
                .conso(GreenITutils.normalizeString(ind.getConso()))
                .nbVm(GreenITutils.normalizeString(ind.getNbVm()))
                .consoScore(greenItScore.getConso().setScale(3, RoundingMode.UP).toString())
                .impactScore(greenItScore.getImpact().setScale(3, RoundingMode.UP).toString())
                .gaspillageScore(
                        greenItScore.getGaspillage().setScale(3, RoundingMode.UP).toString())
                .lettreGreen(greenItScoreCalculator.compute(ind).getGrade())
                .dateMaj(ind.getDateMaj())
                .build();
    }
}
