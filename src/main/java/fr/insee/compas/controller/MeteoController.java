package fr.insee.compas.controller;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import fr.insee.compas.model.meteo.DemandeCreationMeteo;
import fr.insee.compas.model.meteo.Meteo;
import fr.insee.compas.service.meteo.MeteoAffichageService;
import fr.insee.compas.service.meteo.MeteoAlerteService;
import fr.insee.compas.service.meteo.MeteoCreationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/meteo")
@Tag(name = "API Meteo", description = "API des indicateurs Météo du dev")
@AllArgsConstructor
@Validated
public class MeteoController {

    private MeteoCreationService meteoCreationService;
    private MeteoAffichageService meteoAffichageService;
    private final MeteoAlerteService meteoAlerteService;

    @PostMapping
    @Operation(
            summary =
                    "Création d'une météo pour une application. Renvoie les ids des TableFaits"
                            + " créés.")
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

    // 🆕 Nouveau GET : renvoie uniquement les applis dont la dernière météo a au moins 23 jours
    @GetMapping("/anciennes")
    @Operation(summary = "Lister les applications dont la dernière météo date d'au moins 23 jours")
    public List<Meteo> listerApplicationsMeteoAnciennes() {
        return meteoAffichageService.listerApplicationsMeteoAncienne();
    }

    // (optionnel) GET paramétrable : si tu veux choisir le nombre de jours via query param
    @GetMapping("/anciennes/{jours}")
    @Operation(summary = "Lister les applications dont la dernière météo date d'au moins X jours")
    public List<Meteo> listerApplicationsMeteoAnciennesParam(@PathVariable int jours) {
        return meteoAffichageService.listerApplicationsMeteoAvecAgeMin(jours);
    }

    @PostMapping("/alertes")
    public ResponseEntity<Void> envoyerAlertes(
            @RequestParam(defaultValue = "23") int jours,
            @RequestParam(defaultValue = "true") boolean test) {
        meteoAlerteService.envoyerAlertesRga(jours, test);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/history")
    @Operation(summary = "Lister les dernières données météo sur une fenêtre de X mois")
    public List<Meteo> getHistory(
            @Parameter(description = "Nombre de mois à remonter", example = "6")
                    @RequestParam(name = "nbMois", defaultValue = "6")
                    @Min(3)
                    @Max(12)
                    Integer nbMois) {
        return meteoAffichageService.listerDernieresMeteosParApplication(nbMois);
    }
}
