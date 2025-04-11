package fr.insee.compas.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.compas.dto.AggregatedResultDto;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.TableFaitsRepository;
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
        List<TableFaits> metrics =
                tableFaitsRepository.findLatestValueByIndicateurByModule(indicateur);
        return metrics.stream()
                .collect(
                        Collectors.toMap(
                                TableFaits::getIdModule, // Clé : idModule
                                tableFaits -> tableFaits // Valeur : l'objet TableFaits
                                ));
    }

    public Map<Integer, TableFaits> getMapMetricByApplication(int indicateur) {
        List<TableFaits> metrics =
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
        List<Object[]> results = tableFaitsRepository.findAggregatedSumResults(indicateur);
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
        List<Object[]> results = tableFaitsRepository.findAggregatedMaxResults(indicateur);
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
        List<Object[]> results = tableFaitsRepository.findAggregatedAvgResults(indicateur);
        return results.stream()
                .collect(
                        Collectors.toMap(
                                obj -> ((Number) obj[0]).intValue(), // Clé : ID Application
                                obj ->
                                        new AggregatedResultDto(
                                                new BigDecimal(((Number) obj[1]).toString()),
                                                ((Number) obj[0]).intValue())));
    }

    public Map<Integer, IndicateurQualiteView> getIndicateurQualite() {
        List<Object[]> faits = tableFaitsRepository.findValueIndicateurQualiteBrute();

        return faits.stream()
                .collect(
                        Collectors.toMap(
                                obj -> ((Number) obj[0]).intValue(),
                                obj ->
                                        IndicateurQualiteView.builder()
                                                .moduleId(((Number) obj[0]).intValue())
                                                .nbLigneCode(toStringOrEmpty(obj[1]))
                                                .nbLigneCodeNonTeste(toStringOrEmpty(obj[2]))
                                                .nbCveCritical(toStringOrEmpty(obj[3]))
                                                .nbCveHigh(toStringOrEmpty(obj[4]))
                                                .nbCveMedium(toStringOrEmpty(obj[5]))
                                                .nbCveLow(toStringOrEmpty(obj[6]))
                                                .detteTechnique(toStringOrEmpty(obj[7]))
                                                .fiabilite(toStringOrEmpty(obj[8]))
                                                .build()));
    }

    private String toStringOrEmpty(Object obj) {
        return Optional.ofNullable(obj).map(Object::toString).orElse("");
    }
}
