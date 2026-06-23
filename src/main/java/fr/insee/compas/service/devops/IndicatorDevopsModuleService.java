package fr.insee.compas.service.devops;

import static fr.insee.compas.util.DevopsConstantes.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.view.IndicateurDevopsView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndicatorDevopsModuleService {

    private final OscarService oscarService;
    private final TableFaitsService tableFaitsService;

    public List<IndicateurDevopsView> getIndicateurNiveauModule(
            Date dateReference, Date datePassee, boolean isSynthetique) {

        Map<Integer, IndicateurDevopsView> mapCurrent =
                tableFaitsService.getIndicateurModuleDevops(dateReference);
        Map<Integer, IndicateurDevopsView> mapPast =
                tableFaitsService.getIndicateurModuleDevops(datePassee);

        return oscarService.getModules().stream()
                .map(module -> buildView(module, mapCurrent, mapPast, isSynthetique))
                .toList();
    }

    private IndicateurDevopsView buildView(
            Module module,
            Map<Integer, IndicateurDevopsView> mapCurrent,
            Map<Integer, IndicateurDevopsView> mapPast,
            boolean isSynthetique) {

        IndicateurDevopsView current =
                mapCurrent.getOrDefault(module.getId(), new IndicateurDevopsView());
        IndicateurDevopsView past =
                mapPast.getOrDefault(module.getId(), new IndicateurDevopsView());

        Integer correctedCurrent = correctContributorCount(current.getNbContributorCount());
        Integer correctedPast = correctContributorCount(past.getNbContributorCount());

        IndicateurDevopsView view =
                IndicateurDevopsView.builder()
                        .moduleId(module.getId())
                        .moduleName(module.getModName())
                        .applicationId(module.getIdApplication())
                        .applicationName(module.getAppName())
                        .sndi(module.getSndi())
                        .domaineSndi(module.getDomaineSndi())
                        .domaineFonctionnel(module.getDomaineFonctionnel())
                        .distanceCount(current.getDistanceCount())
                        .pastDistanceCount(past.getDistanceCount())
                        .nbDeploymentCount(current.getNbDeploymentCount())
                        .pastNbDeploymentCount(past.getNbDeploymentCount())
                        .lettreDistanceCount(
                                IndicateurDevopsLetterUtils.calculLettreDistanceCount(
                                        current.getDistanceCount()))
                        .lettreDeploymentCount(
                                IndicateurDevopsLetterUtils.calculLettreDeploymentCount(
                                        current.getNbDeploymentCount()))
                        .lettreContributorCount(
                                IndicateurDevopsLetterUtils.calculLettreContributorCount(
                                        current.getNbContributorCount()))
                        .diffDistanceCount(
                                calculateDiff(current.getDistanceCount(), past.getDistanceCount()))
                        .diffNbDeploymentCount(
                                calculateDiff(
                                        current.getNbDeploymentCount(),
                                        past.getNbDeploymentCount()))
                        .nbContributorCount(
                                correctedCurrent != null ? String.valueOf(correctedCurrent) : null)
                        .pastNbContributorCount(
                                correctedPast != null ? String.valueOf(correctedPast) : null)
                        .diffNbContributorCount(
                                correctedCurrent != null && correctedPast != null
                                        ? correctedCurrent - correctedPast
                                        : Integer.MIN_VALUE)
                        .build();

        view.calculerLettreGlobalDevops(isSynthetique);
        return view;
    }
}
