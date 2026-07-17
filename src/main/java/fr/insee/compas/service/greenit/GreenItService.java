package fr.insee.compas.service.greenit;

import static fr.insee.compas.util.greenit.GreenITutils.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.compas.dto.green.GreenItBaseDto;
import fr.insee.compas.logic.update.greenit.kube.KubeMetricsCsvUpdater;
import fr.insee.compas.logic.update.greenit.vm.ApplishareMetricsApiUpdater;
import fr.insee.compas.logic.update.greenit.vm.VmMetricsCsvUpdater;
import fr.insee.compas.mapper.green.GreenItMapper;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.repository.projection.green.GreenItAppBaseProjection;
import fr.insee.compas.repository.projection.green.GreenItAppKubeProjection;
import fr.insee.compas.repository.projection.green.GreenItAppVmProjection;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.view.green.IndicateurAppGreenBaseView;

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

    public List<IndicateurAppGreenBaseView> getIndicateursApplicationGreenIT(
            ViewGreen viewGreen, Date origine, Date passee) {
        if (!isValidDate(origine, passee))
            throw new IllegalArgumentException("Les dates fournies ne sont pas valides");

        LocalDate date = origine.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Map<Integer, GreenItAppBaseProjection> greenItAppProjectionMap =
                getGreenItAppProjectionMap(viewGreen, origine);
        Map<Integer, GreenItAppBaseProjection> greenItAppProjectionMapHist =
                getGreenItAppProjectionMap(viewGreen, passee);

        Map<Integer, Application> applications =
                oscarService.getApplications().stream()
                        .collect(Collectors.toMap(Application::getIdApplication, a -> a));

        return greenItAppProjectionMap.entrySet().stream()
                .map(
                        entry -> {
                            Integer idApplication = entry.getKey();
                            GreenItAppBaseProjection projection = entry.getValue();
                            GreenItAppBaseProjection projectionHist =
                                    greenItAppProjectionMapHist.get(idApplication);
                            Application applicationFetch = applications.get(idApplication);

                            GreenItBaseDto greenItBaseDto =
                                    switch (viewGreen) {
                                        case KUBE ->
                                                greenItMapper.buildIndicateurKubeDto(
                                                        idApplication,
                                                        applicationFetch,
                                                        (GreenItAppKubeProjection) projection,
                                                        (GreenItAppKubeProjection) projectionHist,
                                                        date);
                                        case VM ->
                                                greenItMapper.buildIndicateurDtoVm(
                                                        idApplication,
                                                        applicationFetch,
                                                        (GreenItAppVmProjection) projection,
                                                        (GreenItAppVmProjection) projectionHist,
                                                        date);
                                    };
                            return greenItMapper.mapToView(greenItBaseDto);
                        })
                .toList();
    }

    private Map<Integer, GreenItAppBaseProjection> getGreenItAppProjectionMap(
            ViewGreen viewGreen, Date dateReference) {
        return switch (viewGreen) {
            case KUBE ->
                    tableFaitsRepository.getGreenItAppKube(dateReference).stream()
                            .collect(
                                    Collectors.toMap(
                                            GreenItAppKubeProjection::getIdApplication,
                                            greenItAppKubeProjection -> greenItAppKubeProjection));
            case VM ->
                    tableFaitsRepository.getGreenItAppVm(dateReference).stream()
                            .collect(
                                    Collectors.toMap(
                                            GreenItAppVmProjection::getIdApplication,
                                            greenItAppVmProjection -> greenItAppVmProjection));
        };
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
