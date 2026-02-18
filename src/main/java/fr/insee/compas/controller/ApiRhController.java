package fr.insee.compas.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.client.configuration.oauth.ApiRhAuthentification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@Slf4j
public class ApiRhController {

    private final ApiRhAuthentification apiRhAuthentification;

    /**
     * Endpoint pour récupérer les couples idep -> email à partir de l'API RH Exemple : GET
     * http://localhost:8080/api/agents
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getAgentsIdepEmail() {
        log.info("Appel du controller /api/agents");
        Map<String, String> result = apiRhAuthentification.recupererIdepEtEmails();
        return ResponseEntity.ok(result);
    }
}
