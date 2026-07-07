package fr.insee.compas.service.greenit;

import static fr.insee.compas.util.greenit.GreenITutils.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.compas.dto.GreenItAppDto;
import fr.insee.compas.logic.update.greenit.kube.KubeMetricsCsvUpdater;
import fr.insee.compas.logic.update.greenit.vm.ApplishareMetricsApiUpdater;
import fr.insee.compas.logic.update.greenit.vm.VmMetricsCsvUpdater;
import fr.insee.compas.mapper.green.GreenItMapper;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.repository.projection.GreenItAppProjection;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.view.IndicateurApplicationGreenITView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GreenItService {

    private final OscarService oscarService;

    private final GreenItMapper greenItMapper;

    protected final TableFaitsRepository tableFaitsRepository;

    private final ApplishareMetricsApiUpdater applishareMetricsApiUpdater;

    private final KubeMetricsCsvUpdater kubeMetricsCsvUpdater;

    private final VmMetricsCsvUpdater vmMetricsCsvUpdater;

    public Set<LocalDate> getValidDates() {
        return new HashSet<>(tableFaitsRepository.findLastDateIndicateur());
    }

    public List<IndicateurApplicationGreenITView> getIndicateursApplicationGreenIT(
            Date origine, Date passee) {
        if (!isValidDate(origine, passee))
            throw new IllegalArgumentException("Les dates fournies ne sont pas valides");
        LocalDate date = origine.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Map<Integer, GreenItAppProjection> greenItAppProjectionMap =
                getGreenItAppProjectionMap(origine);
        Map<Integer, GreenItAppProjection> greenItAppProjectionMapHist =
                getGreenItAppProjectionMap(passee);
        Map<Integer, Application> applications =
                oscarService.getApplications().stream()
                        .collect(
                                Collectors.toMap(
                                        Application::getIdApplication, application -> application));
        return greenItAppProjectionMap.entrySet().stream()
                .map(
                        entry -> {
                            Integer idApplication = entry.getKey();
                            GreenItAppProjection projection = entry.getValue();
                            GreenItAppProjection projectionHist =
                                    greenItAppProjectionMapHist.get(idApplication);
                            Application applicationFetch = applications.get(idApplication);
                            GreenItAppDto greenItAppDto =
                                    buildIndicateurApplicationGreenITDto(
                                            idApplication,
                                            applicationFetch,
                                            projection,
                                            projectionHist,
                                            date);
                            return greenItMapper.mapToView(greenItAppDto);
                        })
                .toList();
    }

    private Map<Integer, GreenItAppProjection> getGreenItAppProjectionMap(Date dateReference) {
        return tableFaitsRepository.getGreenItApp(dateReference).stream()
                .collect(
                        Collectors.toMap(
                                GreenItAppProjection::getIdApplication,
                                greenItAppProjection -> greenItAppProjection));
    }

    private GreenItAppDto buildIndicateurApplicationGreenITDto(
            Integer idApplication,
            Application applicationFetch,
            GreenItAppProjection projection,
            GreenItAppProjection projectionHist,
            LocalDate date) {
        return GreenItAppDto.builder()
                .applicationId(idApplication)
                .applicationName(
                        applicationFetch != null ? applicationFetch.getAppName() : "anonyme")
                .domaineDev(applicationFetch != null ? applicationFetch.getDomaineSndi() : null)
                .serviceDev(applicationFetch != null ? applicationFetch.getSndi() : null)
                .domaineFonc(
                        applicationFetch != null ? applicationFetch.getDomaineFonctionnel() : null)
                .ramAllocated(orZeroBigDecimal(projection.getRamAlloue()))
                .ramAllocatedHist(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getRamAlloue))
                .ramMaxi(orZeroBigDecimal(projection.getRamMaxi()))
                .ramMaxiHist(orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getRamMaxi))
                .diskAllocated(orZeroBigDecimal(projection.getDisqueAlloue()))
                .diskAllocatedHist(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getDisqueAlloue))
                .diskUsed(orZeroBigDecimal(projection.getDisqueConsomme()))
                .diskUsedHist(
                        orZeroHistBigDecimal(
                                projectionHist, GreenItAppProjection::getDisqueConsomme))
                .cpuAllocated(orZeroBigDecimal(projection.getCpuAllouee()))
                .cpuAllocatedHist(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getCpuAllouee))
                .cpuMaxi(orZeroBigDecimal(projection.getCpuMaxi()))
                .cpuMaxiHist(orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getCpuMaxi))
                .conso(orZeroBigDecimal(projection.getConsoElec()))
                .consoHist(orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getConsoElec))
                .asUsed(orZeroBigDecimal(projection.getAsConsomme()))
                .asUsedHist(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getAsConsomme))
                .asAllocated(orZeroBigDecimal(projection.getAsAlloue()))
                .asAllocatedHist(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getAsAlloue))
                .nbVm(orZeroBigDecimal(projection.getNbrVM()))
                .nbVmHist(orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getNbrVM))
                .cpuUsed(orZeroBigDecimal(projection.getCpuConsomme()))
                .cpuUsedHist(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getCpuConsomme))
                .ramUsed(orZero(projection.getRamConsommee()))
                .ramUsedHist(orZeroHistLong(projectionHist, GreenItAppProjection::getRamConsommee))
                .s3Used(orZero(projection.getS3Consomme()))
                .s3UsedHist(orZeroHistLong(projectionHist, GreenItAppProjection::getS3Consomme))
                .pvcUsed(orZero(projection.getPvcConsomme()))
                .pvcUsedHist(orZeroHistLong(projectionHist, GreenItAppProjection::getPvcConsomme))
                .nbPodMaxi(orZeroBigDecimal(projection.getNbPodMaxi()))
                .nbPodMaxiHist(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getNbPodMaxi))
                .ramAllocatedProd(orZeroBigDecimal(projection.getRamAlloueePd()))
                .ramAllocatedHistProd(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getRamAlloueePd))
                .ramMaxiProd(orZeroBigDecimal(projection.getRamMaxiPd()))
                .ramMaxiHistProd(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getRamMaxiPd))
                .diskAllocatedProd(orZeroBigDecimal(projection.getDisqueAllouePd()))
                .diskAllocatedHistProd(
                        orZeroHistBigDecimal(
                                projectionHist, GreenItAppProjection::getDisqueAllouePd))
                .diskUsedProd(orZeroBigDecimal(projection.getDisqueConsommePd()))
                .diskUsedHistProd(
                        orZeroHistBigDecimal(
                                projectionHist, GreenItAppProjection::getDisqueConsommePd))
                .cpuAllocatedProd(orZeroBigDecimal(projection.getCpuAlloueePd()))
                .cpuAllocatedHistProd(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getCpuAlloueePd))
                .cpuMaxiProd(orZeroBigDecimal(projection.getCpuMaxiPd()))
                .cpuMaxiHistProd(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getCpuMaxiPd))
                .consoProd(orZeroBigDecimal(projection.getConsoElecPd()))
                .consoHistProd(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getConsoElecPd))
                .asUsedProd(orZeroBigDecimal(projection.getAsConsommePd()))
                .asUsedHistProd(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getAsConsommePd))
                .asAllocatedProd(orZeroBigDecimal(projection.getAsAllouePd()))
                .asAllocatedHistProd(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getAsAllouePd))
                .nbVmProd(orZeroBigDecimal(projection.getNbrVmPd()))
                .nbVmHistProd(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getNbrVmPd))
                .cpuUsedProd(orZeroBigDecimal(projection.getCpuConsommeePd()))
                .cpuUsedHistProd(
                        orZeroHistBigDecimal(
                                projectionHist, GreenItAppProjection::getCpuConsommeePd))
                .ramUsedProd(orZero(projection.getRamConsommeePd()))
                .ramUsedHistProd(
                        orZeroHistLong(projectionHist, GreenItAppProjection::getRamConsommeePd))
                .s3UsedProd(orZero(projection.getS3ConsommePd()))
                .s3UsedHistProd(
                        orZeroHistLong(projectionHist, GreenItAppProjection::getS3ConsommePd))
                .pvcUsedProd(orZero(projection.getPvcConsommePd()))
                .pvcUsedHistProd(
                        orZeroHistLong(projectionHist, GreenItAppProjection::getPvcConsommePd))
                .nbPodMaxiProd(orZeroBigDecimal(projection.getNbPodMaxiPd()))
                .nbPodMaxiHistProd(
                        orZeroHistBigDecimal(projectionHist, GreenItAppProjection::getNbPodMaxiPd))
                .dateMaj(date)
                .build();
    }

    public void miseAJourVmMetricsGreenItFromFile(MultipartFile file, LocalDate fileDate) {
        vmMetricsCsvUpdater.miseAJourIndicateursGreenItFromFile(file, fileDate);
    }

    public void miseAJourKubeMetricsGreenItFromFile(MultipartFile file, LocalDate fileDate) {
        kubeMetricsCsvUpdater.miseAJourIndicateursGreenItFromFile(file, fileDate);
    }

    public void miseAJourApplishareMetricsGreenItFromApi() {
        applishareMetricsApiUpdater.miseAJourIndicateursGreenItFromApi();
    }

    private boolean isValidDate(Date origine, Date passee) {
        Set<LocalDate> validDates = getValidDates();

        LocalDate origineDate = origine.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        LocalDate passeeDate = passee.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return validDates.contains(origineDate) && validDates.contains(passeeDate);
    }
}
