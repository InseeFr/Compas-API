package fr.insee.compas.logic.update.greenit.vm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.insee.compas.client.HyperXClient;
import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.ApplicationOscarView;
import fr.insee.compas.client.view.ApplishareHyperXView;
import fr.insee.compas.exception.CompasClientException;
import fr.insee.compas.exception.ErrorVM;
import fr.insee.compas.logic.update.greenit.GreenItMetricsApiUpdater;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.util.greenit.ScoreUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApplishareMetricsApiUpdater implements GreenItMetricsApiUpdater {
    private HyperXClient hyperXClient;
    private OscarClient oscarClient;
    private TableFaitsRepository tableFaitsRepository;

    public ApplishareMetricsApiUpdater(
            HyperXClient hyperXClient,
            OscarClient oscarClient,
            TableFaitsRepository tableFaitsRepository) {
        this.hyperXClient = hyperXClient;
        this.oscarClient = oscarClient;
        this.tableFaitsRepository = tableFaitsRepository;
    }

    @Override
    public void miseAJourIndicateursGreenItFromApi() {
        ResponseEntity<List<ApplishareHyperXView>> applishareHyperXs =
                hyperXClient.getApplishareHyperX();
        if (applishareHyperXs.getBody() == null) {
            final ErrorVM errorVM = new ErrorVM();
            errorVM.setMessage("Erreur retour body Hyperx");
            throw new CompasClientException(500, errorVM);
        }
        if (applishareHyperXs.getBody().isEmpty()) {
            log.info("Aucune métrique Applishare retournée  par HyperX, pas de mise à jour");
            return;
        }
        log.debug("nombre d'environnements applishare : " + applishareHyperXs.getBody().size());
        miseAJourIndicateursApplishare(applishareHyperXs.getBody());
    }

    @Transactional
    public void miseAJourIndicateursApplishare(List<ApplishareHyperXView> applishareHyperXViews) {
        LocalDate applishareDate = LocalDate.now();
        ResponseEntity<List<ApplicationOscarView>> applicationsOscars =
                oscarClient.getAllApplicationOscar();
        if (applicationsOscars.getBody() == null) {
            final ErrorVM errorVM = new ErrorVM();
            errorVM.setMessage("Erreur retour body Oscar");
            throw new CompasClientException(500, errorVM);
        }
        List<ApplicationOscarView> applicationsViews = applicationsOscars.getBody();
        Map<String, Integer> applicationsIdByName =
                applicationsViews.stream()
                        .flatMap(
                                application ->
                                        Optional.ofNullable(
                                                        application.getApplicationNomAlternatifs())
                                                .orElse(List.of())
                                                .stream()
                                                .filter(
                                                        nomAlternatif ->
                                                                "applishare"
                                                                        .equalsIgnoreCase(
                                                                                nomAlternatif
                                                                                        .getSource()))
                                                .filter(
                                                        nomAlternatif ->
                                                                nomAlternatif.getNomAlternatif()
                                                                        != null)
                                                .map(
                                                        nomAlternatif ->
                                                                Map.entry(
                                                                        nomAlternatif
                                                                                .getNomAlternatif(),
                                                                        application.getId())))
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (existing, duplicate) -> existing));
        applicationsIdByName.forEach((key, value) -> log.info("applications {} : {} ", key, value));

        final Map<Integer, List<ApplishareHyperXView>> applishareApplis =
                applishareHyperXViews.stream()
                        .filter(
                                view -> {
                                    Integer applicationId =
                                            applicationsIdByName.get(view.getApplication());
                                    if (applicationId == null) {
                                        log.warn(
                                                "Application HyperX '{}' introuvable dans Oscar,"
                                                        + " indicateurs ignorés",
                                                view.getApplication());
                                        return false;
                                    }
                                    return true;
                                })
                        .collect(
                                Collectors.groupingBy(
                                        view -> applicationsIdByName.get(view.getApplication())));

        List<TableFaits> tablesFaits = new ArrayList<>();
        applishareApplis.forEach(
                (applicationId, views) -> {
                    BigDecimal asConsomme =
                            sommeApplishare(views, ApplishareHyperXView::getTailleApplishareGo);
                    BigDecimal asConsommePd =
                            sommeApplishareProd(views, ApplishareHyperXView::getTailleApplishareGo);
                    BigDecimal asAlloue =
                            sommeApplishare(views, ApplishareHyperXView::getTailleApplishareTotGo);
                    BigDecimal asAllouePd =
                            sommeApplishareProd(
                                    views, ApplishareHyperXView::getTailleApplishareTotGo);

                    tablesFaits.add(
                            peuplerIndicateurs(
                                    applicationId,
                                    applishareDate,
                                    asConsomme,
                                    IndicateurType.AS_CONSOMME));
                    tablesFaits.add(
                            peuplerIndicateurs(
                                    applicationId,
                                    applishareDate,
                                    asConsommePd,
                                    IndicateurType.AS_CONSOMME_PD));
                    tablesFaits.add(
                            peuplerIndicateurs(
                                    applicationId,
                                    applishareDate,
                                    asAlloue,
                                    IndicateurType.AS_ALLOUE));
                    tablesFaits.add(
                            peuplerIndicateurs(
                                    applicationId,
                                    applishareDate,
                                    asAllouePd,
                                    IndicateurType.AS_ALLOUE_PD));
                });
        tableFaitsRepository.saveAll(tablesFaits);
    }

    private TableFaits peuplerIndicateurs(
            Integer appId, LocalDate dataDate, BigDecimal valeur, IndicateurType indicateurType) {
        log.info("date : {} et valeur {}", dataDate, valeur);
        return TableFaits.builder()
                .idApplication(appId)
                .date(dataDate)
                .idSource(SourceType.HYPERX.getValue())
                .idIndicateur(indicateurType.getValue())
                .valeur(valeur)
                .build();
    }

    private BigDecimal sommeApplishare(
            List<ApplishareHyperXView> views,
            Function<ApplishareHyperXView, BigDecimal> extractor) {
        return views.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sommeApplishareProd(
            List<ApplishareHyperXView> views,
            Function<ApplishareHyperXView, BigDecimal> extractor) {
        return views.stream()
                .filter(view -> ScoreUtils.isPlateformeProd(view.getPlateforme()))
                .map(extractor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
