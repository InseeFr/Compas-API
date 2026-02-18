package fr.insee.compas.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.model.compas.ModuleOscar;
import fr.insee.compas.repository.ModuleOscarRepository;
import fr.insee.compas.service.OscarService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/module-oscar")
public class ModuleOscarController {

    private final ModuleOscarRepository moduleRepository;

    private final OscarService oscarService;

    public ModuleOscarController(
            ModuleOscarRepository moduleRepository, OscarService oscarService) {
        this.moduleRepository = moduleRepository;
        this.oscarService = oscarService;
    }

    @GetMapping(value = "/find-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtenir tous les modules ")
    public List<ModuleOscar> getAllUsers() {
        return moduleRepository.findAll();
    }

    @GetMapping(value = "/find-module-actif", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtenir tous les modules actifs ")
    public List<ModuleOscar> getModuleActif() {
        return moduleRepository.findByActif(true);
    }

    @PutMapping("/mise-a-jour")
    @Operation(summary = "Mettre à jour les modules de Compas à partir d'Oscar")
    public void miseAjourModule() {
        oscarService.miseAjourModuleOscarEnBaseDeDonnees();
    }
}
