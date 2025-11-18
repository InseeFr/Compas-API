package fr.insee.compas.service.securite;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import fr.insee.compas.client.configuration.oauth.AnalyzerAuthentification;
import fr.insee.compas.exception.AnalyzerApiException;
import fr.insee.compas.model.analyzer.ApplicationAnalyzer;
import fr.insee.compas.model.analyzer.ModuleAnalyzer;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.TableFaitsRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RecupCveSecuriteService {

    private final TableFaitsRepository tableFaitsRepository;
    private final UtilsCveService utilService;
    private final RestTemplate restTemplate;

    @Value("${api.analyzer.base-url:#{'https://api-analyzer.developpement.insee.fr'}}")
    private String apiBaseUrl;

    private final AnalyzerAuthentification analyzerAuthentification;

    public RecupCveSecuriteService(
            TableFaitsRepository tableFaitsRepository,
            UtilsCveService utilCveService,
            RestTemplate restTemplate,
            AnalyzerAuthentification analyzerAuthentification) {
        this.tableFaitsRepository = tableFaitsRepository;
        this.utilService = utilCveService;
        this.restTemplate = restTemplate;
        this.analyzerAuthentification = analyzerAuthentification;
    }

    /** Méthode principale pour récupérer et sauvegarder les CVE depuis l'API */
    public void recupereCve() {
        try {
            final List<ApplicationAnalyzer> applications = getApplicationsFromApi();

            for (final ApplicationAnalyzer application : applications) {
                final List<ModuleAnalyzer> modules = application.getModules();

                // Filtrer uniquement les modules avec une date de scan valide
                final List<ModuleAnalyzer> modulesWithScanDate =
                        modules == null
                                ? List.of()
                                : modules.stream()
                                        .filter(m -> m.getDateDernierScanCve() != null)
                                        .toList();

                // Sauvegarder l'application seulement s'il existe au moins un module avec une date
                if (!modulesWithScanDate.isEmpty()) {
                    saveCveApplication(application);

                    // Puis sauvegarder uniquement les modules valides
                    for (final ModuleAnalyzer module : modulesWithScanDate) {
                        saveCveModule(module, application.getId());
                    }
                } else {
                    log.info(
                            "Application ignorée (aucun module scanné) : {}", application.getNom());
                }
            }

            log.info(
                    "Récupération des CVE terminée avec succès pour {} applications",
                    applications.size());

        } catch (final Exception e) {
            log.error(
                    "Erreur lors de la récupération des CVE depuis l'API : {}", e.getMessage(), e);
        }
    }

    /** Récupère la liste des applications depuis l'API */
    private List<ApplicationAnalyzer> getApplicationsFromApi() {
        try {
            final String url = apiBaseUrl + "/applications";

            final HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(analyzerAuthentification.execute());

            final HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            final ResponseEntity<ApplicationAnalyzer[]> response =
                    restTemplate.exchange(
                            url, HttpMethod.GET, requestEntity, ApplicationAnalyzer[].class);

            final ApplicationAnalyzer[] applicationsArray = response.getBody();

            if (applicationsArray == null) {
                log.warn("Aucune application récupérée depuis l'API");
                return List.of();
            }

            log.info("Récupération de {} applications depuis l'API", applicationsArray.length);
            return List.of(applicationsArray);

        } catch (final RestClientException e) {
            log.error("Erreur lors de l'appel à l'API applications : {}", e.getMessage());
            throw new AnalyzerApiException(
                    "Impossible de récupérer les applications depuis l'API", e);
        }
    }

    /** Sauvegarde les CVE */
    private void saveCve(
            Map<String, Integer> cveData,
            Integer applicationId,
            Integer moduleId,
            boolean isApplication) {
        cveData.forEach(
                (type, count) -> {
                    final TableFaits fait =
                            TableFaits.builder()
                                    .idModule(moduleId)
                                    .idApplication(applicationId)
                                    .idIndicateur(
                                            isApplication
                                                    ? utilService.getIndicateurApplication(type)
                                                    : utilService.getIndicateurModule(type))
                                    .valeur(BigDecimal.valueOf(count))
                                    .idSource(SourceType.ANALYZER.getValue())
                                    .commentaire("")
                                    .date(LocalDate.now())
                                    .build();

                    tableFaitsRepository.save(fait);
                    log.debug(
                            "CVE {} sauvegardée - App: {}, Module: {}, Type: {}, Nombre: {}",
                            isApplication ? "application" : "module",
                            applicationId,
                            moduleId,
                            type,
                            count);
                });
    }

    private void saveCveApplication(ApplicationAnalyzer app) {
        saveCve(extractCveDataFromApplication(app), app.getId(), null, true);
    }

    private void saveCveModule(ModuleAnalyzer module, Integer applicationId) {
        saveCve(extractCveDataFromModule(module), applicationId, module.getId(), false);
    }

    /** Extrait les données CVE d'une application */
    private Map<String, Integer> extractCveDataFromApplication(ApplicationAnalyzer application) {
        Map<String, Integer> cveData = new HashMap<>();

        if (application.getCveActives() != null) {
            cveData.put("CRITICAL", application.getCveActives().getNombreCveCritique());
            cveData.put("HIGH", application.getCveActives().getNombreCveMajeur());
            cveData.put("MEDIUM", application.getCveActives().getNombreCveMoyenne());
            cveData.put("LOW", application.getCveActives().getNombreCveFaible());
        }

        return cveData;
    }

    /** Extrait les données CVE d'un module */
    private Map<String, Integer> extractCveDataFromModule(ModuleAnalyzer module) {
        Map<String, Integer> cveData = new HashMap<>();

        if (module.getCveActives() != null) {
            cveData.put("CRITICAL", module.getCveActives().getNombreCveCritique());
            cveData.put("HIGH", module.getCveActives().getNombreCveMajeur());
            cveData.put("MEDIUM", module.getCveActives().getNombreCveMoyenne());
            cveData.put("LOW", module.getCveActives().getNombreCveFaible());
        }

        return cveData;
    }
}
