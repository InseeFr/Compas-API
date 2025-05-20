package fr.insee.compas.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.model.sonar.RecuperationMeasures;
import fr.insee.compas.service.qualite.IndicateurQualiteApplicationService;
import fr.insee.compas.service.qualite.IndicateurQualiteModuleService;
import fr.insee.compas.service.qualite.RecupCveService;
import fr.insee.compas.service.qualite.RecuperationIndicateurSonarService;
import fr.insee.compas.view.IndicateurQualiteView;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/qualite")
public class QualiteController {

    private final RecuperationIndicateurSonarService testUnitaireService;
    private final RecupCveService cveService;
    private final IndicateurQualiteModuleService moduleService;
    private final IndicateurQualiteApplicationService applicationService;

    public QualiteController(
            RecuperationIndicateurSonarService testUnitaireService,
            RecupCveService cveService,
            IndicateurQualiteModuleService moduleService,
            IndicateurQualiteApplicationService applicationService) {
        this.testUnitaireService = testUnitaireService;
        this.cveService = cveService;
        this.moduleService = moduleService;
        this.applicationService = applicationService;
    }

    @PutMapping("/indicateurs-sonar")
    @Operation(summary = "mise à jour des indicateurs provenant de sonar")
    public void updateIndicateursSonar() throws IOException {
        log.info("Début de la récupération des indicateurs pour la couverture de test");
        Map<String, RecuperationMeasures> analyseModule =
                testUnitaireService.putIndicateursSonarModule();
        testUnitaireService.putIndicateursSonarApplication(analyseModule);
        log.info("fin de la récupération des indicateurs pour la couverture de test");
    }

    @PutMapping("/indicateurs-cve")
    @Operation(summary = "mise à jour des indicateurs cve en base de donnée")
    public void updateIndicateurCve() {
        log.info("Début de la récupération des indicateurs pour les cve pour les applications");
        cveService.recupereCve();
        log.info("Fin de la récupération des indicateurs pour les cve pour les applications");
    }

    @GetMapping("/modules")
    public List<IndicateurQualiteView> getIndicateurQualiteByModule() throws IOException {
        log.info("Début du endpoint  récupération indicateur Qualite par module");
        List<IndicateurQualiteView> result = moduleService.getIndicateurNiveauModule();
        log.info("Fin du endpoint récupération indicateur Qualite par module");
        return result;
    }

    @GetMapping("/applications")
    public List<IndicateurQualiteView> getIndicateurQualiteByApplication() throws IOException {
        log.info("Début du endpoint récupération indicateur Qualite par application ");
        List<IndicateurQualiteView> result = applicationService.getIndicateurNiveauApplication();
        log.info("Fin du endpoint récupération indicateur Qualite par application");
        return result;
    }
}
