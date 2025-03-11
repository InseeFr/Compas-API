package fr.insee.compas.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.service.IndicateurOscarService;
import fr.insee.compas.view.IndicateurApplicationDeveloppementLogicielView;
import fr.insee.compas.view.IndicateurModuleDeveloppementLogicielView;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/developpement-logiciel")
public class IndicateurDeveloppementLogicielController {

    private final IndicateurOscarService indicateurOscarService;

    @Autowired
    public IndicateurDeveloppementLogicielController(
            IndicateurOscarService indicateurOscarService) {
        this.indicateurOscarService = indicateurOscarService;
    }

    @PutMapping("/update-distance-indicator")
    @Operation(
            summary =
                    "mise à jour de la distance entre le jour de déploiement en production et la"
                            + " date du jour")
    public void miseAjourValeurs() throws IOException {
        log.info("****** Début du endpoint  MiseAjourIndicateurDistanceMEP ********");
        indicateurOscarService.miseAJourLinesTableFaitsEnBaseDeDonnees();
        log.info("****** fin du endpoint    MiseAjourIndicateurDistanceMEP ********");
    }

    @GetMapping("/get-distance-grade-module")
    public List<IndicateurModuleDeveloppementLogicielView> getGradeDistanceMEPModule() {
        log.info("****** Début du endpoint  getGradeDistanceMEPModule ********");
        List<IndicateurModuleDeveloppementLogicielView> result =
                indicateurOscarService.calculateDistanceGradesModule();
        log.info("****** fin du endpoint    getGradeDistanceMEPModule********");
        return result;
    }

    @GetMapping("/get-distance-grade-application")
    public List<IndicateurApplicationDeveloppementLogicielView> getGradeDistanceMEPApplication() {
        log.info("****** Début du endpoint  getGradeDistanceMEPApplication ********");
        List<IndicateurApplicationDeveloppementLogicielView> result =
                indicateurOscarService.calculateDistanceGradesApplication();
        log.info("****** fin du endpoint    getGradeDistanceMEPApplication ********");
        return result;
    }
}
