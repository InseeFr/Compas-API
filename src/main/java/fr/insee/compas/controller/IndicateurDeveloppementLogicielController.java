package fr.insee.compas.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.service.DeveloppementLogicielService;
import fr.insee.compas.view.IndicateurApplicationDeveloppementLogicielView;
import fr.insee.compas.view.IndicateurModuleDeveloppementLogicielView;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/developpement-logiciel")
public class IndicateurDeveloppementLogicielController {

    private final DeveloppementLogicielService developpementLogicielService;

    @Autowired
    public IndicateurDeveloppementLogicielController(
            DeveloppementLogicielService developpementLogicielService) {
        this.developpementLogicielService = developpementLogicielService;
    }

    @PutMapping("/update-distance-indicator")
    @Operation(
            summary =
                    "mise à jour de la distance entre le jour de déploiement en production et la"
                            + " date du jour")
    public void miseAjourValeursDistance() throws IOException {
        log.info("****** Début du endpoint  MiseAjourIndicateurDistanceMEP ********");
        developpementLogicielService.miseAJourIndicateurDistanceEnBaseDeDonnees();
        log.info("****** fin du endpoint    MiseAjourIndicateurDistanceMEP ********");
    }

    @PutMapping("/update-deployment-count-indicator")
    @Operation(
            summary =
                    "mise à jour du nombre de déploiement d'un module en production depuis un"
                            + " intervalle de dates")
    public void miseAjourValeursDeploymentCount(
            @RequestParam(value = "startDate", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endDate)
            throws IOException {
        log.info("****** Début du endpoint miseAjourValeursDeploymentCount ********");
        developpementLogicielService.miseAJourIndicateurDeploymentCountEnBaseDeDonnees(
                startDate, endDate);
        log.info("****** Fin du endpoint miseAjourValeursDeploymentCount ********");
    }

    @GetMapping("/get-distance-grade-module")
    public List<IndicateurModuleDeveloppementLogicielView> getGradeDistanceMEPModule() {
        log.info("****** Début du endpoint  getGradeDistanceMEPModule ********");
        List<IndicateurModuleDeveloppementLogicielView> result =
                developpementLogicielService.calculateGradesModule(IndicateurType.NBR_JOUR_MEP);
        log.info("****** fin du endpoint    getGradeDistanceMEPModule********");
        return result;
    }

    @GetMapping("/get-distance-grade-application")
    public List<IndicateurApplicationDeveloppementLogicielView> getGradeDistanceMEPApplication() {
        log.info("****** Début du endpoint  getGradeDistanceMEPApplication ********");
        List<IndicateurApplicationDeveloppementLogicielView> result =
                developpementLogicielService.calculateGradesApplication(
                        IndicateurType.NBR_JOUR_MEP);
        log.info("****** fin du endpoint    getGradeDistanceMEPApplication ********");
        return result;
    }

    @GetMapping("/get-monthly-deployments-grade-module")
    public List<IndicateurModuleDeveloppementLogicielView> getMonthlyDeploymentsGradeModule() {
        log.info("****** Début du endpoint  getMonthlyDeploymentsGradeModule ********");
        List<IndicateurModuleDeveloppementLogicielView> result =
                developpementLogicielService.calculateGradesModule(IndicateurType.DEPLOYMENT_COUNT);
        log.info("****** fin du endpoint    getMonthlyDeploymentsGradeModule********");
        return result;
    }

    @GetMapping("/get-monthly-deployments-grade-application")
    public List<IndicateurApplicationDeveloppementLogicielView>
            getMonthlyDeploymentsGradeApplication() {
        log.info("****** Début du endpoint  getMonthlyDeploymentsGradeApplication ********");
        List<IndicateurApplicationDeveloppementLogicielView> result =
                developpementLogicielService.calculateGradesApplication(
                        IndicateurType.DEPLOYMENT_COUNT);
        log.info("****** fin du endpoint    getMonthlyDeploymentsGradeApplication********");
        return result;
    }
}
