package fr.insee.compas.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.TableFaitsRepository;

@Service
public class TableFaitsService {

    private final TableFaitsRepository tableFaitsRepository;

    public TableFaitsService(TableFaitsRepository tableFaitsRepository) {
        this.tableFaitsRepository = tableFaitsRepository;
    }

    public Map<Integer, TableFaits> getMapMetricByModule(int indicateur) {
        List<TableFaits> metrics = tableFaitsRepository.findLatestValueByIndicateur(indicateur);
        return metrics.stream()
                .collect(
                        Collectors.toMap(
                                TableFaits::getIdModule, // Clé : idModule
                                tableFaits -> tableFaits // Valeur : l'objet TableFaits
                                ));
    }
}
