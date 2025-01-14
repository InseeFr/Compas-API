package fr.insee.compas.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.model.compas.ModuleOscar;
import fr.insee.compas.repository.ModuleOscarRepository;
import fr.insee.compas.service.IndicateurOscarService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/module_oscar")
public class ModuleOscarController {

    private final ModuleOscarRepository moduleRepository;

    private final IndicateurOscarService moduleService;

    public ModuleOscarController(
            ModuleOscarRepository moduleRepository, IndicateurOscarService moduleService) {
        this.moduleRepository = moduleRepository;
        this.moduleService = moduleService;
    }

    @GetMapping("/findAll")
    @Operation(summary = "Obtenir tous les modules ")
    public List<ModuleOscar> getAllUsers() {
        return moduleRepository.findAll();
    }

    @GetMapping("/findModuleActif")
    @Operation(summary = "Obtenir tous les modules actifs ")
    public List<ModuleOscar> getModuleActif() {
        return moduleRepository.findByActif(true);
    }

    @PutMapping("/MiseAjour")
    @Operation(summary = "Mettre à jour les modules de Compas à partir d'Oscar")
    public void miseAjourModule() throws IOException {
        moduleService.miseAjourModuleOscarEnBaseDeDonnees();
    }
}
