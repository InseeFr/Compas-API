package fr.insee.compas.controller;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.service.qualite.RecupCveService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/gitlab")
@AllArgsConstructor
public class GitlabIndicateursController {

    private RecupCveService recupCveService;

    @PutMapping("/recupCve")
    public void recupCve() {
        recupCveService.getCveInBdd();
    }
}
