package fr.insee.compas.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.ModuleOscarView;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.OscarService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class OscarController {

    private final OscarClient oscarClient;

    private final OscarService oscarService;

    public OscarController(OscarClient oscarclient, OscarService oscarService) {
        this.oscarClient = oscarclient;
        this.oscarService = oscarService;
    }

    @GetMapping("/get-all-modules")
    @Operation(summary = "liste des modules d'oscar ")
    public List<ModuleOscarView> getAllModules() {
        return oscarClient.getAllModuleOscar().getBody();
    }

    @GetMapping("/get-modules")
    @Operation(summary = "liste des modules d'oscar sans feign ")
    public List<Module> getModules() {
        return oscarService.getModules();
    }

    @GetMapping("/get-applications")
    @Operation(summary = "liste des applications d'oscar sans feign ")
    public List<Application> getApplications() {
        return oscarService.getApplications();
    }
}
