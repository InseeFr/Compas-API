package fr.insee.compas.schedule;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.insee.compas.model.sonar.RecuperationMeasures;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.devops.UpdateIndicatorDevopsService;
import fr.insee.compas.service.qualite.RecuperationIndicateurSonarService;
import fr.insee.compas.service.securite.RecupCveSecuriteService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApiScheduler {
    private final OscarService oscarService;
    private final RecuperationIndicateurSonarService indicateurSonar;
    private final RecupCveSecuriteService cveService;
    private final UpdateIndicatorDevopsService updateIndicatorDevopsService;

    public ApiScheduler(
            OscarService oscarService,
            RecuperationIndicateurSonarService testUnitaireService,
            RecupCveSecuriteService cveService,
            UpdateIndicatorDevopsService updateIndicatorDevopsService) {
        this.oscarService = oscarService;
        this.indicateurSonar = testUnitaireService;
        this.cveService = cveService;
        this.updateIndicatorDevopsService = updateIndicatorDevopsService;
    }

    @Scheduled(cron = "0 30 6 * * *")
    public void callApi() {
        log.info("mise à jour des modules oscar à la date de {}", LocalDate.now());
        oscarService.miseAjourModuleOscarEnBaseDeDonnees();
        log.info("mise à jour des indicateurs sonar");
        try {
            Map<String, RecuperationMeasures> analyseModule =
                    indicateurSonar.putIndicateursSonarModule();
            indicateurSonar.putIndicateursSonarApplication(analyseModule);
        } catch (IOException e) {
            log.error("pb lors de la mise à jour des indicateur sonar{}", e.getMessage());
        }
        log.info("mise à jour des indicateurs cve");
        cveService.recupereCve();
        log.info("fin des mises à jour des cve");
        log.info("mise à jour des indicateurs devops");
        updateIndicatorDevopsService.miseAJourIndicateursDevopsEnBaseDeDonnes(null, null);
        log.info("fin des mises à jour des devops");
        log.info("fin des mises à jour ");
    }
}
