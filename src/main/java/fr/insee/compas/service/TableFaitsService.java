package fr.insee.compas.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.compas.dto.AggregatedSumResultDto;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.TableFaitsRepository;

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

    public Map<Integer, AggregatedSumResultDto> findAgregationSumByIndicateurAndApplication(
            int indicateur) {
        List<Object[]> results = tableFaitsRepository.findAggregatedSumResults(indicateur);
        return results.stream()
                .collect(
                        Collectors.toMap(
                                obj -> ((Number) obj[0]).intValue(), // Clé : ID Application
                                obj ->
                                        new AggregatedSumResultDto(
                                                new BigDecimal(((Number) obj[1]).toString()),
                                                ((Number) obj[0]).intValue())));
    }

    public Map<Integer, AggregatedSumResultDto> findAgregationAvgByIndicateurAndApplication(
            int indicateur) {
        List<Object[]> results = tableFaitsRepository.findAggregatedAvgResults(indicateur);
        return results.stream()
                .collect(
                        Collectors.toMap(
                                obj -> ((Number) obj[0]).intValue(), // Clé : ID Application
                                obj ->
                                        new AggregatedSumResultDto(
                                                new BigDecimal(((Number) obj[1]).toString()),
                                                ((Number) obj[0]).intValue())));
    }
}
