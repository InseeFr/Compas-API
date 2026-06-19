package fr.insee.compas.controller;

import static fr.insee.compas.util.TendanceUtils.buildPeriode;

import java.text.ParseException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import fr.insee.compas.model.compas.Periode;
import fr.insee.compas.service.securite.*;
import fr.insee.compas.view.IndicateurApplicationSecuriteMonthly;
import fr.insee.compas.view.IndicateurSecuriteView;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/securite")
public class SecuriteController {

    private final RecupCveSecuriteService cveSecuriteService;
    private final CveCriticalMonthlyService cveCriticalMonthlyService;
    private final IIndicateurSecuriteService indicateurSecuriteService;
    private final RecupHyperxSecuriteService recupHyperxSecuriteService;

    public SecuriteController(
            RecupCveSecuriteService cveSecuriteService,
            IIndicateurSecuriteService indicateurSecuriteService,
            CveCriticalMonthlyService cveCriticalMonthlyService,
            RecupHyperxSecuriteService recupHyperxSecuriteService) {
        this.cveSecuriteService = cveSecuriteService;
        this.cveCriticalMonthlyService = cveCriticalMonthlyService;
        this.indicateurSecuriteService = indicateurSecuriteService;
        this.recupHyperxSecuriteService = recupHyperxSecuriteService;
    }

    @PutMapping("/indicateurs-cve")
    @Operation(summary = "mise à jour des indicateurs cve en base de donnée")
    public void updateIndicateurCve() {
        log.info("Début de la récupération des indicateurs pour les cve pour les applications");
        cveSecuriteService.recupereCve();
        log.info("Fin de la récupération des indicateurs pour les cve pour les applications");
    }

    @GetMapping(value = "/modules", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<IndicateurSecuriteView> getIndicateurSecuriteByModule(
            @RequestParam(required = false) String dateReference,
            @RequestParam(required = false) String datePassee)
            throws ParseException {
        log.info("Début du endpoint  récupération indicateur Securite par module");
        Periode periode = buildPeriode(dateReference, datePassee);
        List<IndicateurSecuriteView> result =
                indicateurSecuriteService.getIndicateursModuleView(
                        periode.origine(), periode.passee());
        log.info("Fin du endpoint récupération indicateur Securite par module");
        return result;
    }

    @GetMapping(value = "/applications", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<IndicateurSecuriteView> getIndicateurSecuriteByApplication(
            @RequestParam(required = false) String dateReference,
            @RequestParam(required = false) String datePassee)
            throws ParseException {
        log.info("Début du endpoint récupération indicateur Securite par application ");
        Periode periode = buildPeriode(dateReference, datePassee);
        List<IndicateurSecuriteView> result =
                indicateurSecuriteService.getIndicateursApplicationView(
                        periode.origine(), periode.passee());

        log.info("Fin du endpoint récupération indicateur Securite par application");
        return result;
    }

    @GetMapping(
            value = "/applications/cve-critical/monthly",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "CVE critiques par application et par mois (mesures du 1er du mois)")
    public List<IndicateurApplicationSecuriteMonthly> getCveCriticalMonthly() {
        log.info("Début endpoint CVE critiques mensuel (sans filtre)");
        List<IndicateurApplicationSecuriteMonthly> result = cveCriticalMonthlyService.getMonthly();
        log.info("Fin endpoint CVE critiques mensuel : {} lignes", result.size());
        return result;
    }

    @PutMapping("/indicateurs-delaiMajVM")
    @Operation(summary = "mise à jour des indicateurs delaiMajVM en base de donnée")
    public void updateIndicateurDelai() {

        recupHyperxSecuriteService.updateDonneesVmNonMiseAjourDansDelaiParHyperX();
    }
}
