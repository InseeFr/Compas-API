package fr.insee.compas.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.model.compas.ModuleGrade;
import fr.insee.compas.service.QualiteService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/indicateurs")
public class QualiteController {

    private final QualiteService qualiteService;

    public QualiteController(QualiteService qualiteService) {
        this.qualiteService = qualiteService;
    }

    @PutMapping("/MiseAjourIndicateurCouvertureTest")
    @Operation(
            summary =
                    "mise à jour du nombre de lignes de code et du nombre de lignes non testées du"
                            + " module ")
    public void miseAjourValeurs() throws IOException {
        log.info("****** Début du endpoint  MiseAjourIndicateurCouvertureTest ********");
        qualiteService.miseAJourLinesTableFaitsEnBaseDeDonnees();
        log.info("****** fin du endpoint    MiseAjourIndicateurCouvertureTest ********");
    }

    @GetMapping("/PourcentageTest")
    public Map<Integer, ModuleGrade> getTestPercentagesByModule() throws IOException {
        log.info("****** Début du endpoint  PourcentageTest ********");
        Map<Integer, ModuleGrade> result = qualiteService.calculateTestGradesNiveauModule();
        log.info("****** fin du endpoint    PourcentageTest ********");
        return result;
    }

    @GetMapping("/PourcentageTestApplication")
    public Map<Integer, ModuleGrade> getTestPercentagesByApplication() throws IOException {
        log.info("****** Début du endpoint  PourcentageTestApplication ********");
        Map<Integer, ModuleGrade> result = qualiteService.calculateTestGradesNiveauApplication();
        log.info("****** fin du endpoint    PourcentageTestApplication ********");
        return result;
    }
}
