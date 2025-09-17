package fr.insee.compas.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import fr.insee.compas.service.securite.CveCriticalMonthlyService;
import fr.insee.compas.service.securite.IndicateurSecuriteService;
import fr.insee.compas.service.securite.RecupCveSecuriteService;
import fr.insee.compas.view.IndicateurApplicationSecuriteMonthly;
import fr.insee.compas.view.IndicateurSecuriteApplicationView;
import fr.insee.compas.view.IndicateurSecuriteModuleView;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/securite")
public class SecuriteController {

    private final RecupCveSecuriteService cveSecuriteService;
    private final CveCriticalMonthlyService cveCriticalMonthlyService;
    private final IndicateurSecuriteService indicateurSecuriteService;

    public SecuriteController(
            RecupCveSecuriteService cveSecuriteService,
            IndicateurSecuriteService indicateurSecuriteService,
            CveCriticalMonthlyService cveCriticalMonthlyService) {
        this.cveSecuriteService = cveSecuriteService;
        this.cveCriticalMonthlyService = cveCriticalMonthlyService;
        this.indicateurSecuriteService = indicateurSecuriteService;
    }

    @PutMapping("/indicateurs-cve")
    @Operation(summary = "mise à jour des indicateurs cve en base de donnée")
    public void updateIndicateurCve() {
        log.info("Début de la récupération des indicateurs pour les cve pour les applications");
        cveSecuriteService.recupereCve();
        log.info("Fin de la récupération des indicateurs pour les cve pour les applications");
    }

    @GetMapping("/modules")
    public List<IndicateurSecuriteModuleView> getIndicateurQualiteByModule() throws IOException {
        log.info("Début du endpoint  récupération indicateur Qualite par module");
        List<IndicateurSecuriteModuleView> result =
                indicateurSecuriteService.getIndicateursModuleView();
        log.info("Fin du endpoint récupération indicateur Qualite par module");
        return result;
    }

    @GetMapping("/applications")
    public List<IndicateurSecuriteApplicationView> getIndicateurQualiteByApplication()
            throws IOException {
        log.info("Début du endpoint récupération indicateur Qualite par application ");
        List<IndicateurSecuriteApplicationView> result =
                indicateurSecuriteService.getIndicateursApplicationView();

        log.info("Fin du endpoint récupération indicateur Qualite par application");
        return result;
    }

    @GetMapping("/applications/cve-critical/monthly")
    @Operation(summary = "CVE critiques par application et par mois (mesures du 1er du mois)")
    public List<IndicateurApplicationSecuriteMonthly> getCveCriticalMonthly() {
        log.info("Début endpoint CVE critiques mensuel (sans filtre)");
        List<IndicateurApplicationSecuriteMonthly> result = cveCriticalMonthlyService.getMonthly();
        log.info("Fin endpoint CVE critiques mensuel : {} lignes", result.size());
        return result;
    }
}
