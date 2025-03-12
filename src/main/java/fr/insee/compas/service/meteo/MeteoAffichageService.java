package fr.insee.compas.service.meteo;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.meteo.Meteo;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MeteoAffichageService {

    private OscarService oscarService;
    private TableFaitsRepository tableFaitsRepository;

    public List<Meteo> listerApplicationsMeteo() {
        return oscarService.getApplications().stream()
                .map(
                        application -> {
                            Meteo meteo =
                                    Meteo.builder()
                                            .idApplication(application.getIdApplication())
                                            .appName(application.getAppName())
                                            .domaineSndi(application.getDomaineSndi())
                                            .sndi(application.getSndi())
                                            .build();
                            TableFaits tableFaitsExample =
                                    TableFaits.builder()
                                            .idApplication(application.getIdApplication())
                                            .idIndicateur(MeteoCreationService.ID_INDICATEUR_METEO)
                                            .idSource(
                                                    MeteoCreationService.ID_SOURCE_SAISIE_MANUELLE)
                                            .build();
                            tableFaitsRepository
                                    .findBy(
                                            Example.of(tableFaitsExample),
                                            q ->
                                                    q.sortBy(Sort.by("date", "id").descending())
                                                            .first())
                                    .ifPresent(
                                            tableFait -> {
                                                meteo.setDate(tableFait.getDate());
                                                meteo.setValeurMeteo(tableFait.getValeur());
                                            });
                            return meteo;
                        })
                .toList();
    }
}
