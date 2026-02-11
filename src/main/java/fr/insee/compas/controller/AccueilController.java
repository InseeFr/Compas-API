package fr.insee.compas.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.service.GitlabService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/accueil")
@AllArgsConstructor
@Slf4j
@Tag(name = "Accueil", description = "Markdowns des dossiers gitlab")
public class AccueilController {
    private GitlabService gitlabService;

    @GetMapping("/indicators")
    @Operation(
            summary = "Récupération des indicateurs en format markdown",
            description =
                    "Retourne une map où la clé est le nom de l'indicateur et la valeur est son"
                            + " contenu en markdown")
    @ApiResponse(
            responseCode = "200",
            description = "Liste des indicateurs récupérés avec succès",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema =
                                    @Schema(
                                            type = "object",
                                            additionalPropertiesSchema = String.class,
                                            example =
                                                    """
                                                    {
                                                      "indicateur-qualité": "## Indicateur qualité\\n\\n**100%**",
                                                      "indicateur-sécurité": "## Indicateur sécurité\\n\\n**100%**"
                                                    }
                                                    """)))
    public ResponseEntity<Map<String, String>> getIndicatorsMarkdowns() {
        log.info("Récupérations des markdowns sur gitlab");
        Map<String, String> response = gitlabService.getMarkdownIndicators();
        log.info("Récupération terminée: {} indicateur(s) récupéré(s)", response.size());
        return ResponseEntity.ok(response);
    }
}
