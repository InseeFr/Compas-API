package fr.insee.compas.controller;

import static fr.insee.compas.util.TendanceUtils.buildPeriode;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import fr.insee.compas.model.compas.Periode;
import fr.insee.compas.model.sonar.RecuperationMeasures;
import fr.insee.compas.service.qualite.IndicateurQualiteApplicationService;
import fr.insee.compas.service.qualite.IndicateurQualiteModuleService;
import fr.insee.compas.service.qualite.RecuperationIndicateurSonarService;
import fr.insee.compas.view.IndicateurQualiteView;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/qualite")
public class QualiteController {

    private final RecuperationIndicateurSonarService testUnitaireService;
    private final IndicateurQualiteModuleService moduleService;
    private final IndicateurQualiteApplicationService applicationService;

    public QualiteController(
            RecuperationIndicateurSonarService testUnitaireService,
            IndicateurQualiteModuleService moduleService,
            IndicateurQualiteApplicationService applicationService) {
        this.testUnitaireService = testUnitaireService;
        this.moduleService = moduleService;
        this.applicationService = applicationService;
    }

    @PutMapping("/indicateurs-sonar")
    @Operation(summary = "mise à jour des indicateurs provenant de sonar")
    public void updateIndicateursSonar() {
        log.info("Début de la récupération des indicateurs pour la couverture de test");
        Map<String, RecuperationMeasures> analyseModule =
                testUnitaireService.putIndicateursSonarModule();
        testUnitaireService.putIndicateursSonarApplication(analyseModule);
        log.info("fin de la récupération des indicateurs pour la couverture de test");
    }

    @GetMapping(value = "/modules", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<IndicateurQualiteView> getIndicateurQualiteByModuleByDate(
            @RequestParam(required = false) String origine,
            @RequestParam(required = false) String passee)
            throws ParseException {
        log.info("Début du endpoint  récupération indicateur Qualite par module");
        Periode periode = buildPeriode(origine, passee);

        List<IndicateurQualiteView> result =
                moduleService.getIndicateurNiveauModule(periode.origine(), periode.passee());
        log.info("Fin du endpoint récupération indicateur Qualite par module");
        return result;
    }

    @GetMapping(value = "/applications", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<IndicateurQualiteView> getIndicateurQualiteByApplicationByDate(
            @RequestParam(required = false) String origine,
            @RequestParam(required = false) String passee)
            throws ParseException {
        log.info("Début du endpoint récupération indicateur Qualite par application ");

        Periode periode = buildPeriode(origine, passee);
        List<IndicateurQualiteView> result =
                applicationService.getIndicateurNiveauApplication(
                        periode.origine(), periode.passee());
        log.info("Fin du endpoint récupération indicateur Qualite par application");
        return result;
    }
}
