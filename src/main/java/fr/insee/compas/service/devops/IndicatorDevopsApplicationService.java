package fr.insee.compas.service.devops;

import static fr.insee.compas.util.DevopsConstantes.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.view.IndicateurDevopsView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndicatorDevopsApplicationService {

    private final OscarService oscarService;
    private final TableFaitsService tableFaitsService;

    public List<IndicateurDevopsView> getIndicateurNiveauApplication(
            Date dateReference, Date datePassee, boolean isSynthetique) {

        Map<Integer, IndicateurDevopsView> mapCurrent =
                tableFaitsService.getIndicateurApplicationDevops(dateReference);
        Map<Integer, IndicateurDevopsView> mapPast =
                tableFaitsService.getIndicateurApplicationDevops(datePassee);

        return oscarService.getApplications().stream()
                .map(app -> buildView(app, mapCurrent, mapPast, isSynthetique))
                .toList();
    }

    private IndicateurDevopsView buildView(
            Application app,
            Map<Integer, IndicateurDevopsView> mapCurrent,
            Map<Integer, IndicateurDevopsView> mapPast,
            boolean isSynthetique) {

        IndicateurDevopsView current =
                mapCurrent.getOrDefault(app.getIdApplication(), new IndicateurDevopsView());
        IndicateurDevopsView past =
                mapPast.getOrDefault(app.getIdApplication(), new IndicateurDevopsView());

        Integer correctedCurrent = correctContributorCount(current.getNbContributorCount());
        Integer correctedPast = correctContributorCount(past.getNbContributorCount());

        IndicateurDevopsView view =
                IndicateurDevopsView.builder()
                        .applicationId(app.getIdApplication())
                        .applicationName(app.getAppName())
                        .sndi(app.getSndi())
                        .domaineSndi(app.getDomaineSndi())
                        .domaineFonctionnel(app.getDomaineFonctionnel())
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
