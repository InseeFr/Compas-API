package fr.insee.compas.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.compas.dto.AggregatedResultDto;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.repository.projection.DevopsProjection;
import fr.insee.compas.view.IndicateurDevopsView;
import fr.insee.compas.view.IndicateurQualiteView;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TableFaitsService {

    private final TableFaitsRepository tableFaitsRepository;

    public TableFaitsService(TableFaitsRepository tableFaitsRepository) {
        this.tableFaitsRepository = tableFaitsRepository;
    }

    public Map<Integer, TableFaits> getMapMetricByModule(int indicateur) {
        final List<TableFaits> metrics =
                tableFaitsRepository.findLatestValueByIndicateurByModule(indicateur);
        return metrics.stream()
                .collect(
                        Collectors.toMap(
                                TableFaits::getIdModule, // Clé : idModule
                                tableFaits -> tableFaits // Valeur : l'objet TableFaits
                                ));
    }

    public Map<Integer, TableFaits> getMapMetricByApplication(int indicateur) {
        final List<TableFaits> metrics =
                tableFaitsRepository.findLatestValueByIndicateurByApplication(indicateur);
        return metrics.stream()
                .collect(
                        Collectors.toMap(
                                TableFaits::getIdApplication, // Clé : idModule
                                tableFaits -> tableFaits // Valeur : l'objet TableFaits
                                ));
    }

    public Map<Integer, AggregatedResultDto> findAgregationSumByIndicateurAndApplication(
            int indicateur) {
        final List<Object[]> results = tableFaitsRepository.findAggregatedSumResults(indicateur);
        return results.stream()
                .collect(
                        Collectors.toMap(
                                obj -> ((Number) obj[0]).intValue(), // Clé : ID Application
                                obj ->
                                        new AggregatedResultDto(
                                                new BigDecimal(((Number) obj[1]).toString()),
                                                ((Number) obj[0]).intValue())));
    }

    public Map<Integer, AggregatedResultDto> findAgregationMaxByIndicateurAndApplication(
            int indicateur) {
        final List<Object[]> results = tableFaitsRepository.findAggregatedMaxResults(indicateur);
        return results.stream()
                .collect(
                        Collectors.toMap(
                                obj -> ((Number) obj[0]).intValue(), // Clé : ID Application
                                obj ->
                                        new AggregatedResultDto(
                                                new BigDecimal(((Number) obj[1]).toString()),
                                                ((Number) obj[0]).intValue())));
    }

    public Map<Integer, AggregatedResultDto> findAgregationAvgByIndicateurAndApplication(
            int indicateur) {
        final List<Object[]> results = tableFaitsRepository.findAggregatedAvgResults(indicateur);
        return results.stream()
                .collect(
                        Collectors.toMap(
                                obj -> ((Number) obj[0]).intValue(), // Clé : ID Application
                                obj ->
                                        new AggregatedResultDto(
                                                new BigDecimal(((Number) obj[1]).toString()),
                                                ((Number) obj[0]).intValue())));
    }

    public Map<Integer, IndicateurQualiteView> getIndicateurModuleQualite(Date date) {
        final List<Object[]> faits =
                tableFaitsRepository.findValueIndicateurModuleQualiteBrute(date);

        return faits.stream()
                .collect(
                        Collectors.toMap(
                                obj -> ((Number) obj[0]).intValue(),
                                obj ->
                                        IndicateurQualiteView.builder()
                                                .moduleId(((Number) obj[0]).intValue())
                                                .nbLigneCode(toStringOrEmpty(obj[1]))
                                                .nbLigneCodeNonTeste(toStringOrEmpty(obj[2]))
                                                .detteTechnique(toStringOrEmpty(obj[3]))
                                                .fiabilite(toStringOrEmpty(obj[4]))
                                                .build()));
    }

    public Map<Integer, IndicateurQualiteView> getIndicateurApplicationQualite(Date date) {
        final List<Object[]> faits =
                tableFaitsRepository.findValueIndicateurApplicationQualiteBrute(date);

        return faits.stream()
                .collect(
                        Collectors.toMap(
                                obj -> ((Number) obj[0]).intValue(),
                                obj ->
                                        IndicateurQualiteView.builder()
                                                .applicationId(((Number) obj[0]).intValue())
                                                .nbLigneCode(toStringOrEmpty(obj[1]))
                                                .nbLigneCodeNonTeste(toStringOrEmpty(obj[2]))
                                                .detteTechnique(toStringOrEmpty(obj[3]))
                                                .fiabilite(toStringOrEmpty(obj[4]))
                                                .build()));
    }

    public Map<Integer, IndicateurDevopsView> getIndicateurModuleDevops(Date date) {
        final List<DevopsProjection> faits =
                tableFaitsRepository.findValueIndicateurModuleDevopsBrute(date);

        return faits.stream()
                .collect(
                        Collectors.toMap(
                                DevopsProjection::getIdModule,
                                obj ->
                                        IndicateurDevopsView.builder()
                                                .moduleId(obj.getIdModule())
                                                .distanceCount(toIntString(obj.getDistanceCount()))
                                                .nbDeploymentCount(
                                                        toIntString(obj.getNbDeploymentCount()))
                                                .nbContributorCount(
                                                        toIntString(obj.getNbContributorCount()))
                                                .build()));
    }

    public Map<Integer, IndicateurDevopsView> getIndicateurApplicationDevops(Date date) {
        List<DevopsProjection> faits =
                tableFaitsRepository.findValueIndicateurApplicationDevopsBrute(date);

        return faits.stream()
                .collect(
                        Collectors.toMap(
                                DevopsProjection::getIdApplication,
                                obj ->
                                        IndicateurDevopsView.builder()
                                                .applicationId(obj.getIdApplication())
                                                .distanceCount(toIntString(obj.getDistanceCount()))
                                                .nbDeploymentCount(
                                                        toIntString(obj.getNbDeploymentCount()))
                                                .nbContributorCount(
                                                        toIntString(obj.getNbContributorCount()))
                                                .build()));
    }

    private String toIntString(Object value) {
        if (value instanceof Number number) {
            return String.valueOf(number.intValue());
        } else if (value != null) {
            return value.toString();
        } else {
            return "";
        }
    }

    private String toStringOrEmpty(Object obj) {
        return Optional.ofNullable(obj).map(Object::toString).orElse("");
    }
}
