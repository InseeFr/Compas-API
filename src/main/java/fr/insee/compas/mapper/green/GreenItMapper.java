package fr.insee.compas.mapper.green;

import static fr.insee.compas.util.greenit.GreenITutils.*;

import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import fr.insee.compas.dto.green.GreenItBaseDto;
import fr.insee.compas.dto.green.GreenKubeDto;
import fr.insee.compas.dto.green.GreenVmDto;
import fr.insee.compas.model.greenit.GreenItScore;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.repository.projection.green.GreenItAppKubeProjection;
import fr.insee.compas.repository.projection.green.GreenItAppVmProjection;
import fr.insee.compas.service.greenit.score.GreenItComputeScore;
import fr.insee.compas.util.greenit.GreenITutils;
import fr.insee.compas.view.green.IndicateurAppGreenBaseView;
import fr.insee.compas.view.green.IndicateurAppGreenKubeView;
import fr.insee.compas.view.green.IndicateurAppGreenKubeView.IndicateurAppGreenKubeViewBuilder;
import fr.insee.compas.view.green.IndicateurAppGreenVmView;
import fr.insee.compas.view.green.IndicateurAppGreenVmView.IndicateurAppGreenVmViewBuilder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GreenItMapper {

    private final GreenItComputeScore greenItComputeScore;

    public IndicateurAppGreenBaseView mapToView(GreenItBaseDto greenItAppDto) {
        if (greenItAppDto instanceof GreenVmDto vmDto) {
            return buildVmView(vmDto);
        } else if (greenItAppDto instanceof GreenKubeDto kubeDto) {
            return buildKubeView(kubeDto);
        } else {
            throw new IllegalArgumentException(
                    "Type de DTO GreenIT inconnu : " + greenItAppDto.getClass());
        }
    }

    private IndicateurAppGreenVmView buildVmView(GreenVmDto vmDto) {
        IndicateurAppGreenVmViewBuilder<?, ?> builder =
                IndicateurAppGreenVmView.builder()
                        .applicationId(vmDto.getApplicationId())
                        .applicationName(vmDto.getApplicationName())
                        .serviceDev(vmDto.getServiceDev())
                        .domaineDev(vmDto.getDomaineDev())
                        .domaineFonc(vmDto.getDomaineFonc())
                        .ramAllocated(normalizeString(vmDto.getRamAllocated()))
                        .ramAllocatedHist(normalizeString(vmDto.getRamAllocatedHist()))
                        .ramAllocatedHp(normalizeString(vmDto.getRamAllocatedHp()))
                        .ramAllocatedHistHp(normalizeString(vmDto.getRamAllocatedHpHist()))
                        .ramMaxi(normalizeString(vmDto.getRamMaxi()))
                        .ramMaxiHist(normalizeString(vmDto.getRamMaxiHist()))
                        .ramMaxiHp(normalizeString(vmDto.getRamMaxiHp()))
                        .ramMaxiHistHp(normalizeString(vmDto.getRamMaxiHpHist()))
                        .diskAllocated(normalizeString(vmDto.getDiskAllocated()))
                        .diskAllocatedHist(normalizeString(vmDto.getDiskAllocatedHist()))
                        .diskAllocatedHp(normalizeString(vmDto.getDiskAllocatedHp()))
                        .diskAllocatedHistHp(normalizeString(vmDto.getDiskAllocatedHpHist()))
                        .diskUsed(normalizeString(vmDto.getDiskUsed()))
                        .diskUsedHist(normalizeString(vmDto.getDiskUsedHist()))
                        .diskUsedHp(normalizeString(vmDto.getDiskUsedHp()))
                        .diskUsedHistHp(normalizeString(vmDto.getDiskUsedHpHist()))
                        .cpuAllocated(normalizeStringToHourCpu(vmDto.getCpuAllocated()))
                        .cpuAllocatedHist(normalizeStringToHourCpu(vmDto.getCpuAllocatedHist()))
                        .cpuAllocatedHp(normalizeStringToHourCpu(vmDto.getCpuAllocatedHp()))
                        .cpuAllocatedHistHp(normalizeStringToHourCpu(vmDto.getCpuAllocatedHpHist()))
                        .cpuMaxi(normalizeStringToHourCpu(vmDto.getCpuMaxi()))
                        .cpuMaxiHist(normalizeStringToHourCpu(vmDto.getCpuMaxiHist()))
                        .cpuMaxiHp(normalizeStringToHourCpu(vmDto.getCpuMaxiHp()))
                        .cpuMaxiHistHp(normalizeStringToHourCpu(vmDto.getCpuMaxiHpHist()))
                        .conso(normalizeString(vmDto.getConso()))
                        .consoHist(normalizeString(vmDto.getConsoHist()))
                        .consoHp(normalizeString(vmDto.getConsoHp()))
                        .consoHistHp(normalizeString(vmDto.getConsoHpHist()))
                        .asUsed(normalizeString(vmDto.getAsUsed()))
                        .asUsedHist(normalizeString(vmDto.getAsUsedHist()))
                        .asUsedHp(normalizeString(vmDto.getAsUsedHp()))
                        .asUsedHistHp(normalizeString(vmDto.getAsUsedHpHist()))
                        .asAllocated(normalizeString(vmDto.getAsAllocated()))
                        .asAllocatedHist(normalizeString(vmDto.getAsAllocatedHist()))
                        .asAllocatedHp(normalizeString(vmDto.getAsAllocatedHp()))
                        .asAllocatedHistHp(normalizeString(vmDto.getAsAllocatedHpHist()))
                        .nbVm(normalizeString(vmDto.getNbVm()))
                        .nbVmHist(normalizeString(vmDto.getNbVmHist()))
                        .nbVmHp(normalizeString(vmDto.getNbVmHp()))
                        .nbVmHistHp(normalizeString(vmDto.getNbVmHpHist()))
                        .nbVmProd(normalizeString(vmDto.getNbVmProd()))
                        .nbVmHistProd(normalizeString(vmDto.getNbVmHistProd()))
                        .ramAllocatedProd(normalizeString(vmDto.getRamAllocatedProd()))
                        .ramAllocatedHistProd(normalizeString(vmDto.getRamAllocatedHistProd()))
                        .ramMaxiProd(normalizeString(vmDto.getRamMaxiProd()))
                        .ramMaxiHistProd(normalizeString(vmDto.getRamMaxiHistProd()))
                        .diskAllocatedProd(normalizeString(vmDto.getDiskAllocatedProd()))
                        .diskAllocatedHistProd(normalizeString(vmDto.getDiskAllocatedHistProd()))
                        .diskUsedProd(normalizeString(vmDto.getDiskUsedProd()))
                        .diskUsedHistProd(normalizeString(vmDto.getDiskUsedHistProd()))
                        .cpuAllocatedProd(normalizeStringToHourCpu(vmDto.getCpuAllocatedProd()))
                        .cpuAllocatedHistProd(
                                normalizeStringToHourCpu(vmDto.getCpuAllocatedHistProd()))
                        .cpuMaxiProd(normalizeStringToHourCpu(vmDto.getCpuMaxiProd()))
                        .cpuMaxiHistProd(normalizeStringToHourCpu(vmDto.getCpuMaxiHistProd()))
                        .consoProd(normalizeString(vmDto.getConsoProd()))
                        .consoHistProd(normalizeString(vmDto.getConsoHistProd()))
                        .asUsedProd(normalizeString(vmDto.getAsUsedProd()))
                        .asUsedHistProd(normalizeString(vmDto.getAsUsedHistProd()))
                        .asAllocatedProd(normalizeString(vmDto.getAsAllocatedProd()))
                        .asAllocatedHistProd(normalizeString(vmDto.getAsAllocatedHistProd()))
                        .dateMaj(vmDto.getDateMaj());

        applyVmScore(builder, vmDto);

        return builder.build();
    }

    private void applyVmScore(IndicateurAppGreenVmViewBuilder<?, ?> builder, GreenVmDto vmDto) {
        GreenItScore greenItScore = greenItComputeScore.computeAppScore(vmDto);
        builder.lettreGreen(greenItScore.getGrade())
                .impactScore(greenItScore.getImpact().setScale(3, RoundingMode.UP).toString())
                .consoScore(greenItScore.getScore().setScale(3, RoundingMode.UP).toString())
                .gaspillageScore(
                        greenItScore.getGaspillage().setScale(3, RoundingMode.UP).toString());
    }

    private IndicateurAppGreenKubeView buildKubeView(GreenKubeDto kubeDto) {
        IndicateurAppGreenKubeViewBuilder<?, ?> builder =
                IndicateurAppGreenKubeView.builder()
                        .applicationId(kubeDto.getApplicationId())
                        .applicationName(kubeDto.getApplicationName())
                        .serviceDev(kubeDto.getServiceDev())
                        .domaineDev(kubeDto.getDomaineDev())
                        .domaineFonc(kubeDto.getDomaineFonc())
                        .cpuUsed(normalizeStringToHourCpu(kubeDto.getCpuUsed()))
                        .cpuUsedHist(normalizeStringToHourCpu(kubeDto.getCpuUsedHist()))
                        .cpuUsedHp(normalizeStringToHourCpu(kubeDto.cpuUsedHp()))
                        .cpuUsedHistHp(normalizeStringToHourCpu(kubeDto.cpuUsedHistHp()))
                        .ramUsed(GreenITutils.normalizeStringAroundGo(kubeDto.getRamUsed()))
                        .ramUsedHist(GreenITutils.normalizeStringAroundGo(kubeDto.getRamUsedHist()))
                        .ramUsedHp(normalizeStringAroundGo(kubeDto.ramUsedHp()))
                        .ramUsedHistHp(normalizeStringAroundGo(kubeDto.ramUsedHistHp()))
                        .s3Used(GreenITutils.normalizeStringAroundGo(kubeDto.getS3Used()))
                        .s3UsedHist(GreenITutils.normalizeStringAroundGo(kubeDto.getS3UsedHist()))
                        .s3UsedHp(normalizeStringAroundGo(kubeDto.s3UsedHp()))
                        .s3UsedHistHp(normalizeStringAroundGo(kubeDto.s3UsedHistHp()))
                        .pvcUsed(GreenITutils.normalizeStringAroundGo(kubeDto.getPvcUsed()))
                        .pvcUsedHist(GreenITutils.normalizeStringAroundGo(kubeDto.getPvcUsedHist()))
                        .pvcUsedHp(normalizeStringAroundGo(kubeDto.pvcUsedHp()))
                        .pvcUsedHistHp(normalizeStringAroundGo(kubeDto.pvcUsedHistHp()))
                        .nbPodMaxi(normalizeString(kubeDto.getNbPodMaxi()))
                        .nbPodMaxiHist(normalizeString(kubeDto.getNbPodMaxiHist()))
                        .nbPodMaxiHp(normalizeString(kubeDto.nbPodMaxiHp()))
                        .nbPodMaxiHistHp(normalizeString(kubeDto.nbPodMaxiHistHp()))
                        .cpuUsedProd(normalizeStringToHourCpu(kubeDto.getCpuUsedProd()))
                        .cpuUsedHistProd(normalizeStringToHourCpu(kubeDto.getCpuUsedHistProd()))
                        .ramUsedProd(GreenITutils.normalizeStringAroundGo(kubeDto.getRamUsedProd()))
                        .ramUsedHistProd(
                                GreenITutils.normalizeStringAroundGo(kubeDto.getRamUsedHistProd()))
                        .s3UsedProd(GreenITutils.normalizeStringAroundGo(kubeDto.getS3UsedProd()))
                        .s3UsedHistProd(
                                GreenITutils.normalizeStringAroundGo(kubeDto.getS3UsedHistProd()))
                        .pvcUsedProd(GreenITutils.normalizeStringAroundGo(kubeDto.getPvcUsedProd()))
                        .pvcUsedHistProd(
                                GreenITutils.normalizeStringAroundGo(kubeDto.getPvcUsedHistProd()))
                        .nbPodMaxiProd(normalizeString(kubeDto.getNbPodMaxiProd()))
                        .nbPodMaxiHistProd(normalizeString(kubeDto.getNbPodMaxiHistProd()))
                        .dateMaj(kubeDto.getDateMaj());

        return builder.build();
    }

    @SuppressWarnings("java:S3252")
    public GreenKubeDto buildIndicateurKubeDto(
            Integer idApplication,
            Application applicationFetch,
            GreenItAppKubeProjection projection,
            GreenItAppKubeProjection projectionHist,
            LocalDate date) {

        AppInfo appInfo = resolveAppInfo(applicationFetch);
        return GreenKubeDto.builder()
                .applicationId(idApplication)
                .applicationName(appInfo.applicationName())
                .serviceDev(appInfo.serviceDev())
                .domaineDev(appInfo.domaineDev())
                .domaineFonc(appInfo.domaineFonc())
                .cpuUsed(safeGet(projection, GreenItAppKubeProjection::getCpuConsomme))
                .cpuUsedHist(safeGet(projectionHist, GreenItAppKubeProjection::getCpuConsomme))
                .cpuUsedProd(safeGet(projection, GreenItAppKubeProjection::getCpuConsommeePd))
                .cpuUsedHistProd(
                        safeGet(projectionHist, GreenItAppKubeProjection::getCpuConsommeePd))
                .ramUsed(safeGet(projection, GreenItAppKubeProjection::getRamConsommee))
                .ramUsedProd(safeGet(projection, GreenItAppKubeProjection::getRamConsommeePd))
                .ramUsedHistProd(
                        safeGet(projectionHist, GreenItAppKubeProjection::getRamConsommeePd))
                .ramUsedHist(safeGet(projectionHist, GreenItAppKubeProjection::getRamConsommee))
                .s3Used(safeGet(projection, GreenItAppKubeProjection::getS3Consomme))
                .s3UsedHist(safeGet(projectionHist, GreenItAppKubeProjection::getS3Consomme))
                .s3UsedProd(safeGet(projection, GreenItAppKubeProjection::getS3ConsommePd))
                .s3UsedHistProd(safeGet(projectionHist, GreenItAppKubeProjection::getS3ConsommePd))
                .pvcUsed(safeGet(projection, GreenItAppKubeProjection::getPvcConsomme))
                .pvcUsedHist(safeGet(projectionHist, GreenItAppKubeProjection::getPvcConsomme))
                .pvcUsedProd(safeGet(projection, GreenItAppKubeProjection::getPvcConsommePd))
                .pvcUsedHistProd(
                        safeGet(projectionHist, GreenItAppKubeProjection::getPvcConsommePd))
                .nbPodMaxi(safeGet(projection, GreenItAppKubeProjection::getNbPodMaxi))
                .nbPodMaxiHist(safeGet(projectionHist, GreenItAppKubeProjection::getNbPodMaxi))
                .nbPodMaxiProd(safeGet(projection, GreenItAppKubeProjection::getNbPodMaxiPd))
                .nbPodMaxiHistProd(
                        safeGet(projectionHist, GreenItAppKubeProjection::getNbPodMaxiPd))
                .dateMaj(date)
                .build();
    }

    @SuppressWarnings("java:S3252")
    public GreenVmDto buildIndicateurDtoVm(
            Integer idApplication,
            Application applicationFetch,
            GreenItAppVmProjection projection,
            GreenItAppVmProjection projectionHist,
            LocalDate date) {

        AppInfo appInfo = resolveAppInfo(applicationFetch);
        return GreenVmDto.builder()
                .applicationId(idApplication)
                .applicationName(appInfo.applicationName())
                .serviceDev(appInfo.serviceDev())
                .domaineDev(appInfo.domaineDev())
                .domaineFonc(appInfo.domaineFonc())
                .asAllocated(safeGet(projection, GreenItAppVmProjection::getAsAlloue))
                .asAllocatedHist(safeGet(projectionHist, GreenItAppVmProjection::getAsAlloue))
                .asAllocatedProd(safeGet(projection, GreenItAppVmProjection::getAsAllouePd))
                .asAllocatedHistProd(safeGet(projectionHist, GreenItAppVmProjection::getAsAllouePd))
                .asUsed(safeGet(projection, GreenItAppVmProjection::getAsConsomme))
                .asUsedHist(safeGet(projectionHist, GreenItAppVmProjection::getAsConsomme))
                .asUsedProd(safeGet(projection, GreenItAppVmProjection::getAsConsommePd))
                .asUsedHistProd(safeGet(projectionHist, GreenItAppVmProjection::getAsConsommePd))
                .conso(safeGet(projection, GreenItAppVmProjection::getConsoElec))
                .consoHist(safeGet(projectionHist, GreenItAppVmProjection::getConsoElec))
                .consoProd(safeGet(projection, GreenItAppVmProjection::getConsoElecPd))
                .consoHistProd(safeGet(projectionHist, GreenItAppVmProjection::getConsoElecPd))
                .cpuAllocated(safeGet(projection, GreenItAppVmProjection::getCpuAllouee))
                .cpuAllocatedHist(safeGet(projectionHist, GreenItAppVmProjection::getCpuAllouee))
                .cpuAllocatedProd(safeGet(projection, GreenItAppVmProjection::getCpuAlloueePd))
                .cpuAllocatedHistProd(
                        safeGet(projectionHist, GreenItAppVmProjection::getCpuAlloueePd))
                .cpuMaxi(safeGet(projection, GreenItAppVmProjection::getCpuMaxi))
                .cpuMaxiHist(safeGet(projectionHist, GreenItAppVmProjection::getCpuMaxi))
                .cpuMaxiProd(safeGet(projection, GreenItAppVmProjection::getCpuMaxiPd))
                .cpuMaxiHistProd(safeGet(projectionHist, GreenItAppVmProjection::getCpuMaxiPd))
                .diskAllocated(safeGet(projection, GreenItAppVmProjection::getDisqueAlloue))
                .diskAllocatedHist(safeGet(projectionHist, GreenItAppVmProjection::getDisqueAlloue))
                .diskAllocatedProd(safeGet(projection, GreenItAppVmProjection::getDisqueAllouePd))
                .diskAllocatedHistProd(
                        safeGet(projectionHist, GreenItAppVmProjection::getDisqueAllouePd))
                .diskUsed(safeGet(projection, GreenItAppVmProjection::getDisqueConsomme))
                .diskUsedHist(safeGet(projectionHist, GreenItAppVmProjection::getDisqueConsomme))
                .diskUsedProd(safeGet(projection, GreenItAppVmProjection::getDisqueConsommePd))
                .diskUsedHistProd(
                        safeGet(projectionHist, GreenItAppVmProjection::getDisqueConsommePd))
                .nbVm(safeGet(projection, GreenItAppVmProjection::getNbrVM))
                .nbVmHist(safeGet(projectionHist, GreenItAppVmProjection::getNbrVM))
                .nbVmProd(safeGet(projection, GreenItAppVmProjection::getNbrVmPd))
                .nbVmHistProd(safeGet(projectionHist, GreenItAppVmProjection::getNbrVmPd))
                .ramAllocated(safeGet(projection, GreenItAppVmProjection::getRamAlloue))
                .ramAllocatedHist(safeGet(projectionHist, GreenItAppVmProjection::getRamAlloue))
                .ramAllocatedProd(safeGet(projection, GreenItAppVmProjection::getRamAlloueePd))
                .ramAllocatedHistProd(
                        safeGet(projectionHist, GreenItAppVmProjection::getRamAlloueePd))
                .ramMaxi(safeGet(projection, GreenItAppVmProjection::getRamMaxi))
                .ramMaxiHist(safeGet(projectionHist, GreenItAppVmProjection::getRamMaxi))
                .ramMaxiProd(safeGet(projection, GreenItAppVmProjection::getRamMaxiPd))
                .ramMaxiHistProd(safeGet(projectionHist, GreenItAppVmProjection::getRamMaxiPd))
                .dateMaj(date)
                .build();
    }

    private AppInfo resolveAppInfo(Application applicationFetch) {
        if (applicationFetch == null) {
            return new AppInfo("anonyme", null, null, null);
        }
        return new AppInfo(
                applicationFetch.getAppName(),
                applicationFetch.getSndi(),
                applicationFetch.getDomaineSndi(),
                applicationFetch.getDomaineFonctionnel());
    }

    private record AppInfo(
            String applicationName, String serviceDev, String domaineDev, String domaineFonc) {}
}
