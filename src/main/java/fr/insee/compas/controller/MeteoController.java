package fr.insee.compas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import fr.insee.compas.model.meteo.DemandeCreationMeteo;
import fr.insee.compas.model.meteo.Meteo;
import fr.insee.compas.service.meteo.MeteoAffichageService;
import fr.insee.compas.service.meteo.MeteoCreationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/meteo")
@Tag(name = "API Meteo", description = "API des indicateurs Météo du dev")
@AllArgsConstructor
public class MeteoController {

    private MeteoCreationService meteoCreationService;
    private MeteoAffichageService meteoAffichageService;

    @PostMapping
    @Operation(
            summary =
                    "Création d'une météo pour une application. Renvoie les ids des TableFaits"
                            + " créés. ")
    public ResponseEntity<List<Long>> creerMeteo(
            @RequestBody DemandeCreationMeteo demandeCreationMeteo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meteoCreationService.creerMeteo(demandeCreationMeteo));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les applications et leur éventuelle météo")
    public List<Meteo> listerApplicationsMeteo() {
        return meteoAffichageService.listerApplicationsMeteo();
    }
}
