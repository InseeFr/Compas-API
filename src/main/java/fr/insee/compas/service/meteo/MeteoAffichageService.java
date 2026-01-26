package fr.insee.compas.service.meteo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.meteo.Meteo;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MeteoAffichageService {

    private static final ZoneId TZ_PARIS = ZoneId.of("Europe/Paris");

    private OscarService oscarService;
    private TableFaitsRepository tableFaitsRepository;

    /** Version existante : liste toutes les applis avec leur dernière météo si dispo */
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

                            TableFaits example =
                                    TableFaits.builder()
                                            .idApplication(application.getIdApplication())
                                            .idIndicateur(MeteoCreationService.ID_INDICATEUR_METEO)
                                            .idSource(
                                                    MeteoCreationService.ID_SOURCE_SAISIE_MANUELLE)
                                            .build();

                            tableFaitsRepository
                                    .findBy(
                                            Example.of(example),
                                            q ->
                                                    q.sortBy(Sort.by("date", "id").descending())
                                                            .first())
                                    .ifPresent(
                                            tableFait -> {
                                                meteo.setDate(tableFait.getDate());
                                                meteo.setValeurMeteo(tableFait.getValeur());
                                                meteo.setCommentaire(tableFait.getCommentaire());
                                            });

                            return meteo;
                        })
                .toList();
    }

    /** ne sortir que les applis dont la dernière météo a >= 23 jours */
    public List<Meteo> listerApplicationsMeteoAncienne() {
        return listerApplicationsMeteoAvecAgeMin(23);
    }

    /** Variante paramétrable (au cas où tu veuilles 15, 30 jours, etc.) */
    public List<Meteo> listerApplicationsMeteoAvecAgeMin(int ageMinJours) {
        LocalDate today = LocalDate.now(TZ_PARIS);

        return oscarService.getApplications().stream()
                .map(
                        application -> {
                            TableFaits example =
                                    TableFaits.builder()
                                            .idApplication(application.getIdApplication())
                                            .idIndicateur(MeteoCreationService.ID_INDICATEUR_METEO)
                                            .idSource(
                                                    MeteoCreationService.ID_SOURCE_SAISIE_MANUELLE)
                                            .build();

                            // on récupère la DERNIÈRE météo
                            return tableFaitsRepository
                                    .findBy(
                                            Example.of(example),
                                            q ->
                                                    q.sortBy(Sort.by("date", "id").descending())
                                                            .first())
                                    .map(
                                            tableFait -> {
                                                LocalDate dateMeteo =
                                                        toLocalDate(tableFait.getDate());
                                                if (dateMeteo == null) return null;

                                                long age =
                                                        ChronoUnit.DAYS.between(dateMeteo, today);
                                                if (age >= ageMinJours) {
                                                    // construire l'objet Meteo pour l’appli
                                                    return Meteo.builder()
                                                            .idApplication(
                                                                    application.getIdApplication())
                                                            .appName(application.getAppName())
                                                            .domaineSndi(
                                                                    application.getDomaineSndi())
                                                            .sndi(application.getSndi())
                                                            .date(tableFait.getDate())
                                                            .valeurMeteo(tableFait.getValeur())
                                                            .commentaire(tableFait.getCommentaire())
                                                            .build();
                                                }
                                                return null; // trop récent -> on filtre
                                            })
                                    .orElse(null); // pas de météo -> on filtre aussi
                        })
                .filter(Objects::nonNull)
                .toList();
    }

    /** Récupère les 10 dernières météo pour chaque application (id_indicateur = 401) */
    public List<Meteo> listerDernieresMeteosParApplication(Integer nbMois) {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1).minusMonths(nbMois - 1L);
        Map<Integer, AppMeta> metaById =
                oscarService.getApplications().stream()
                        .collect(
                                Collectors.toMap(
                                        Application::getIdApplication,
                                        a ->
                                                new AppMeta(
                                                        a.getAppName(),
                                                        a.getSndi(),
                                                        a.getDomaineSndi())));

        List<Object[]> rows = tableFaitsRepository.findLast10MeteoPerApp(startDate);

        return rows.stream()
                .map(
                        r -> {
                            Integer idApp = (Integer) r[0];
                            LocalDate localDate =
                                    (r[1] instanceof java.sql.Date date)
                                            ? date.toLocalDate()
                                            : (LocalDate) r[1];
                            LocalDate date = (r[1] == null) ? null : localDate;
                            BigDecimal valeur = (BigDecimal) r[2];
                            String commentaire = (String) r[3];

                            AppMeta meta =
                                    metaById.getOrDefault(idApp, new AppMeta(null, null, null));

                            return Meteo.builder()
                                    .idApplication(idApp)
                                    .appName(meta.appName)
                                    .sndi(meta.sndi)
                                    .domaineSndi(meta.domaine)
                                    .date(date)
                                    .valeurMeteo(valeur)
                                    .commentaire(commentaire)
                                    .build();
                        })
                .toList();
    }

    /** Convertit la date du TableFaits vers LocalDate, quel que soit son type courant. */
    private static LocalDate toLocalDate(Object date) {
        if (date == null) return null;
        if (date instanceof LocalDate ld) return ld;
        if (date instanceof LocalDateTime ldt) return ldt.toLocalDate();
        if (date instanceof java.util.Date d) return d.toInstant().atZone(TZ_PARIS).toLocalDate();

        return null;
    }

    private record AppMeta(String appName, String sndi, String domaine) {}
}
