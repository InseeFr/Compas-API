package fr.insee.compas.mapper.green;

import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import fr.insee.compas.dto.GreenItAppDto;
import fr.insee.compas.model.greenit.GreenItScore;
import fr.insee.compas.service.greenit.score.GreenItComputeScore;
import fr.insee.compas.util.greenit.GreenITutils;
import fr.insee.compas.view.IndicateurApplicationGreenITView;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GreenItMapper {
    private final GreenItComputeScore greenItComputeScore;

    public IndicateurApplicationGreenITView mapToView(GreenItAppDto greenItAppDto) {
        GreenItScore greenItScore = greenItComputeScore.computeAppScore(greenItAppDto);
        String grade = greenItScore.getGrade();
        return IndicateurApplicationGreenITView.builder()
                .applicationId(greenItAppDto.getApplicationId())
                .applicationName(greenItAppDto.getApplicationName())
                .serviceDev(greenItAppDto.getServiceDev())
                .domaineDev(greenItAppDto.getDomaineDev())
                .domaineFonc(greenItAppDto.getDomaineFonc())
                .ramAllocated(GreenITutils.normalizeString(greenItAppDto.getRamAllocated()))
                .ramAllocatedHist(GreenITutils.normalizeString(greenItAppDto.getRamAllocatedHist()))
                .ramMaxi(GreenITutils.normalizeString(greenItAppDto.getRamMaxi()))
                .ramMaxiHist(GreenITutils.normalizeString(greenItAppDto.getRamMaxiHist()))
                .diskAllocated(GreenITutils.normalizeString(greenItAppDto.getDiskAllocated()))
                .diskAllocatedHist(
                        GreenITutils.normalizeString(greenItAppDto.getDiskAllocatedHist()))
                .diskUsed(GreenITutils.normalizeString(greenItAppDto.getDiskUsed()))
                .diskUsedHist(GreenITutils.normalizeString(greenItAppDto.getDiskUsedHist()))
                .cpuAllocated(GreenITutils.normalizeString(greenItAppDto.getCpuAllocated()))
                .cpuAllocatedHist(GreenITutils.normalizeString(greenItAppDto.getCpuAllocatedHist()))
                .cpuMaxi(GreenITutils.normalizeString(greenItAppDto.getCpuMaxi()))
                .cpuMaxiHist(GreenITutils.normalizeString(greenItAppDto.getCpuMaxiHist()))
                .conso(GreenITutils.normalizeString(greenItAppDto.getConso()))
                .consoHist(GreenITutils.normalizeString(greenItAppDto.getConsoHist()))
                .asUsed(GreenITutils.normalizeString(greenItAppDto.getAsUsed()))
                .asUsedHist(GreenITutils.normalizeString(greenItAppDto.getAsUsedHist()))
                .asAllocated(GreenITutils.normalizeString(greenItAppDto.getAsAllocated()))
                .asAllocatedHist(GreenITutils.normalizeString(greenItAppDto.getAsAllocatedHist()))
                .cpuUsed(GreenITutils.normalizeString(greenItAppDto.getCpuUsed()))
                .cpuUsedHist(GreenITutils.normalizeString(greenItAppDto.getCpuUsedHist()))
                .ramUsed(GreenITutils.normalizeStringAroundGo(greenItAppDto.getRamUsed()))
                .ramUsedHist(GreenITutils.normalizeString(greenItAppDto.getRamUsedHist()))
                .s3Used(GreenITutils.normalizeStringAroundGo(greenItAppDto.getS3Used()))
                .s3UsedHist(GreenITutils.normalizeString(greenItAppDto.getS3UsedHist()))
                .pvcUsed(GreenITutils.normalizeStringAroundGo(greenItAppDto.getPvcUsed()))
                .pvcUsedHist(GreenITutils.normalizeString(greenItAppDto.getPvcUsedHist()))
                .nbPodMaxi(GreenITutils.normalizeString(greenItAppDto.getNbPodMaxi()))
                .nbPodMaxiHist(GreenITutils.normalizeString(greenItAppDto.getNbPodMaxiHist()))
                .nbVm(GreenITutils.normalizeString(greenItAppDto.getNbVm()))
                .nbVmHist(GreenITutils.normalizeString(greenItAppDto.getNbVmHist()))
                .ramAllocatedProd(GreenITutils.normalizeString(greenItAppDto.getRamAllocatedProd()))
                .ramAllocatedHistProd(
                        GreenITutils.normalizeString(greenItAppDto.getRamAllocatedHistProd()))
                .ramMaxiProd(GreenITutils.normalizeString(greenItAppDto.getRamMaxiProd()))
                .ramMaxiHistProd(GreenITutils.normalizeString(greenItAppDto.getRamMaxiHistProd()))
                .diskAllocatedProd(
                        GreenITutils.normalizeString(greenItAppDto.getDiskAllocatedProd()))
                .diskAllocatedHistProd(
                        GreenITutils.normalizeString(greenItAppDto.getDiskAllocatedHistProd()))
                .diskUsedProd(GreenITutils.normalizeString(greenItAppDto.getDiskUsedProd()))
                .diskUsedHistProd(GreenITutils.normalizeString(greenItAppDto.getDiskUsedHistProd()))
                .cpuAllocatedProd(GreenITutils.normalizeString(greenItAppDto.getCpuAllocatedProd()))
                .cpuAllocatedHistProd(
                        GreenITutils.normalizeString(greenItAppDto.getCpuAllocatedHistProd()))
                .cpuMaxiProd(GreenITutils.normalizeString(greenItAppDto.getCpuMaxiProd()))
                .cpuMaxiHistProd(GreenITutils.normalizeString(greenItAppDto.getCpuMaxiHistProd()))
                .consoProd(GreenITutils.normalizeString(greenItAppDto.getConsoProd()))
                .consoHistProd(GreenITutils.normalizeString(greenItAppDto.getConsoHistProd()))
                .asUsedProd(GreenITutils.normalizeString(greenItAppDto.getAsUsedProd()))
                .asUsedHistProd(GreenITutils.normalizeString(greenItAppDto.getAsUsedHistProd()))
                .asAllocatedProd(GreenITutils.normalizeString(greenItAppDto.getAsAllocatedProd()))
                .asAllocatedHistProd(
                        GreenITutils.normalizeString(greenItAppDto.getAsAllocatedHistProd()))
                .cpuUsedProd(GreenITutils.normalizeString(greenItAppDto.getCpuUsedProd()))
                .cpuUsedHistProd(GreenITutils.normalizeString(greenItAppDto.getCpuUsedHistProd()))
                .ramUsedProd(GreenITutils.normalizeStringAroundGo(greenItAppDto.getRamUsedProd()))
                .ramUsedHistProd(GreenITutils.normalizeString(greenItAppDto.getRamUsedHistProd()))
                .s3UsedProd(GreenITutils.normalizeStringAroundGo(greenItAppDto.getS3UsedProd()))
                .s3UsedHistProd(GreenITutils.normalizeString(greenItAppDto.getS3UsedHistProd()))
                .pvcUsedProd(GreenITutils.normalizeStringAroundGo(greenItAppDto.getPvcUsedProd()))
                .pvcUsedHistProd(GreenITutils.normalizeString(greenItAppDto.getPvcUsedHistProd()))
                .nbPodMaxiProd(GreenITutils.normalizeString(greenItAppDto.getNbPodMaxiProd()))
                .nbPodMaxiHistProd(
                        GreenITutils.normalizeString(greenItAppDto.getNbPodMaxiHistProd()))
                .nbVmProd(GreenITutils.normalizeString(greenItAppDto.getNbVmProd()))
                .nbVmHistProd(GreenITutils.normalizeString(greenItAppDto.getNbVmHistProd()))
                .lettreGreen(grade)
                .impactScore(greenItScore.getImpact().setScale(3, RoundingMode.UP).toString())
                .consoScore(greenItScore.getScore().setScale(3, RoundingMode.UP).toString())
                .gaspillageScore(
                        greenItScore.getGaspillage().setScale(3, RoundingMode.UP).toString())
                .dateMaj(greenItAppDto.getDateMaj())
                .build();
    }
}
