package fr.insee.compas.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.model.compas.ModuleGrade;
import fr.insee.compas.service.QualiteService;
import fr.insee.compas.service.qualite.TestUnitService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/indicateurs")
public class QualiteController {

    private final QualiteService qualiteService;

    private final TestUnitService tuService;

    public QualiteController(QualiteService qualiteService, TestUnitService tuService) {
        this.qualiteService = qualiteService;
        this.tuService = tuService;
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
        Map<Integer, ModuleGrade> result = tuService.calculateTestGradesNiveauModule();
        log.info("****** fin du endpoint    PourcentageTest ********");
        return result;
    }

    @GetMapping("/PourcentageTestApplication")
    public Map<Integer, ModuleGrade> getTestPercentagesByApplication() throws IOException {
        log.info("****** Début du endpoint  PourcentageTestApplication ********");
        Map<Integer, ModuleGrade> result = tuService.calculateTestGradesNiveauApplication();
        log.info("****** fin du endpoint    PourcentageTestApplication ********");
        return result;
    }
}
