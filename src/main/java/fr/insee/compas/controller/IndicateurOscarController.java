package fr.insee.compas.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.model.compas.ModuleGradeDistance;
import fr.insee.compas.service.IndicateurOscarService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/indicators")
public class IndicateurOscarController {

    private final IndicateurOscarService indicateurOscarService;

    @Autowired
    public IndicateurOscarController(IndicateurOscarService indicateurOscarService) {
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

    @GetMapping("/get-distance-grade")
    public Map<Integer, ModuleGradeDistance> getGradeDistanceMEP() {
        log.info("****** Début du endpoint  getGradeDistanceMEP ********");
        Map<Integer, ModuleGradeDistance> result = indicateurOscarService.calculateDistanceGrades();
        log.info("****** fin du endpoint    getGradeDistanceMEP ********");
        return result;
    }
}
