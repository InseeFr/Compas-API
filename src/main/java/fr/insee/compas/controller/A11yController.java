package fr.insee.compas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import fr.insee.compas.model.a11y.IndicateursModuleA11Y;
import fr.insee.compas.model.a11y.InfosSaisiesA11yToSaveDTO;
import fr.insee.compas.service.a11y.A11yAffichageService;
import fr.insee.compas.service.a11y.A11yMajService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/a11y")
@Tag(name = "API a11y", description = "API des indicateurs sur Accessibilité du dev")
@AllArgsConstructor
public class A11yController {
    private A11yAffichageService a11yAffichageService;
    private A11yMajService a11yMajService;

    @GetMapping("/modules")
    @Operation(summary = "Lister tous les modules et informations accessibilité")
    public List<IndicateursModuleA11Y> listerModulesA11y() {
        return a11yAffichageService.listerModulesA11y();
    }

    @GetMapping("/applications")
    @Operation(summary = "Lister tous les modules et informations accessibilité")
    public List<IndicateursModuleA11Y> listerApplicationA11y() {
        return a11yAffichageService.listerApplicationsA11y();
    }

    @PostMapping
    @Operation(
            summary =
                    "Création / update des informations saisie pour un module. Renvoie l'id des"
                            + " infosSaisies ")
    public ResponseEntity<Long> majInfosSaisiesA11Y(
            @RequestBody InfosSaisiesA11yToSaveDTO infosSaisiesA11yToSaveDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(a11yMajService.majInfosSaisiesA11y(infosSaisiesA11yToSaveDTO));
    }

    @PutMapping("/issues-accessibility")
    @Operation(summary = "Insertion des AA11y de sonar dans la bdd" + " infosSaisies ")
    public void majNbIssueSonarAccessibitlite() {
        a11yMajService.getNbIssueSonarAccessibility();
    }
}
