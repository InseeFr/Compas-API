package fr.insee.compas.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.dto.HomologationDto;
import fr.insee.compas.service.homologation.IHomologationService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/homologations")
@AllArgsConstructor
public class HomologationController {
    private final IHomologationService homologationService;

    @GetMapping(value = "application-homologation", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HomologationDto>> getHomologation() {
        return ResponseEntity.ok(homologationService.getAllHomologation());
    }

    @GetMapping("/homologation/applications-absentes")
    public ResponseEntity<List<String>> getApplicationsAbsentesOscar() {
        return ResponseEntity.ok(homologationService.getAppliAbsentesOscar());
    }
}
