package fr.insee.compas.mapper;

import java.math.RoundingMode;
import java.util.Optional;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.greenit.GreenItScore;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.service.greenit.score.GreenItComputeScore;
import fr.insee.compas.util.greenit.GreenITutils;
import fr.insee.compas.view.IndicateurApplicationGreenITView;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IndicateurApplicationGreenITViewMapper {

    private final GreenItComputeScore greenItComputeScore;

    public IndicateurApplicationGreenITViewMapper(GreenItComputeScore calculatorScoreService) {
        super();
        this.greenItComputeScore = calculatorScoreService;
    }

    public Optional<IndicateurApplicationGreenITView> toView(IndicateurApplicationGreenIT ind) {
        return Optional.ofNullable(ind).map(this::mapToView);
    }

    private IndicateurApplicationGreenITView mapToView(IndicateurApplicationGreenIT indicateur) {
        final GreenItScore greenItScore = greenItComputeScore.computeAppScore(indicateur);
        return IndicateurApplicationGreenITView.builder()
                .applicationId(indicateur.getApplicationId())
                .applicationName(indicateur.getApplicationName())
                .ramAllocated(GreenITutils.normalizeString(indicateur.getRamAllocated()))
                .ramMaxi(GreenITutils.normalizeString(indicateur.getRamMaxi()))
                .diskAllocated(GreenITutils.normalizeString(indicateur.getDiskAllocated()))
                .diskUsed(GreenITutils.normalizeString(indicateur.getDiskUsed()))
                .cpuAllocated(GreenITutils.normalizeString(indicateur.getCpuAllocated()))
                .cpuMaxi(GreenITutils.normalizeString(indicateur.getCpuMaxi()))
                .conso(GreenITutils.normalizeString(indicateur.getConso()))
                .cpuUsed(GreenITutils.normalizeString(indicateur.getCpuUsed()))
                .ramUsed(GreenITutils.normalizeStringArroundGo(indicateur.getRamUsed()))
                .s3Used(GreenITutils.normalizeString(indicateur.getS3Used()))
                .pvcUsed(GreenITutils.normalizeString(indicateur.getPvcUsed()))
                .nbPodMaxi(GreenITutils.normalizeString(indicateur.getNbPodMaxi()))
                .nbVm(GreenITutils.normalizeString(indicateur.getNbVm()))
                .ramAllocatedProd(GreenITutils.normalizeString(indicateur.getRamAllocatedProd()))
                .ramMaxiProd(GreenITutils.normalizeString(indicateur.getRamMaxiProd()))
                .diskAllocatedProd(GreenITutils.normalizeString(indicateur.getDiskAllocatedProd()))
                .diskUsedProd(GreenITutils.normalizeString(indicateur.getDiskUsedProd()))
                .cpuAllocatedProd(GreenITutils.normalizeString(indicateur.getCpuAllocatedProd()))
                .cpuMaxiProd(GreenITutils.normalizeString(indicateur.getCpuMaxiProd()))
                .consoProd(GreenITutils.normalizeString(indicateur.getConsoProd()))
                .cpuUsedProd(GreenITutils.normalizeString(indicateur.getCpuUsedProd()))
                .ramUsedProd(GreenITutils.normalizeStringArroundGo(indicateur.getRamUsedProd()))
                .s3UsedProd(GreenITutils.normalizeString(indicateur.getS3UsedProd()))
                .pvcUsedProd(GreenITutils.normalizeString(indicateur.getPvcUsedProd()))
                .nbPodMaxiProd(GreenITutils.normalizeString(indicateur.getNbPodMaxiProd()))
                .nbVmProd(GreenITutils.normalizeString(indicateur.getNbVmProd()))
                .consoScore(greenItScore.getConso().setScale(3, RoundingMode.UP).toString())
                .impactScore(greenItScore.getImpact().setScale(3, RoundingMode.UP).toString())
                .gaspillageScore(
                        greenItScore.getGaspillage().setScale(3, RoundingMode.UP).toString())
                .lettreGreen(greenItComputeScore.computeAppScore(indicateur).getGrade())
                .dateMaj(indicateur.getDateMaj())
                .build();
    }
}
