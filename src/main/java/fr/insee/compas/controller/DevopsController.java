package fr.insee.compas.controller;

import static fr.insee.compas.util.TendanceUtils.buildPeriode;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.model.compas.Periode;
import fr.insee.compas.service.devops.IndicatorDevopsApplicationService;
import fr.insee.compas.service.devops.IndicatorDevopsModuleService;
import fr.insee.compas.service.devops.update.UpdateIndicatorDevopsService;
import fr.insee.compas.view.IndicateurDevopsView;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/devops")
public class DevopsController {

    private final UpdateIndicatorDevopsService updateIndicatorDevopsService;
    private final IndicatorDevopsModuleService indicatorDevopsModuleService;
    private final IndicatorDevopsApplicationService indicatorDevopsApplicationService;

    @PutMapping("/indicateurs-devops")
    @Operation(summary = "mise à jour des indicateurs devops en base de donnée")
    public void updateIndicateursDevops(
            @RequestParam(value = "startDate", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endDate) {
        log.info("****** Début de la récupération des indicateurs devops ********");
        updateIndicatorDevopsService.miseAJourIndicateursDevopsEnBaseDeDonnes(startDate, endDate);
        log.info("****** Fin de la récupération des indicateurs devops ********");
    }

    @GetMapping(value = "/applications", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<IndicateurDevopsView> getApplications(
            @RequestParam(required = false) String dateReference,
            @RequestParam(required = false) String datePassee,
            @RequestParam(name = "isSynthetique", required = false, defaultValue = "false")
                    boolean isSynthetique) {
        log.info(
                "****** Début du endpoint getApplications (isSynthetique={}) ********",
                isSynthetique);
        Periode periode = buildPeriode(dateReference, datePassee);
        List<IndicateurDevopsView> result =
                indicatorDevopsApplicationService.getIndicateurNiveauApplication(
                        periode.origine(), periode.passee(), isSynthetique);
        log.info("****** fin du endpoint getApplications ********");
        return result;
    }

    @GetMapping(value = "/modules", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<IndicateurDevopsView> getModules(
            @RequestParam(required = false) String dateReference,
            @RequestParam(required = false) String datePassee,
            @RequestParam(name = "isSynthetique", required = false, defaultValue = "false")
                    boolean isSynthetique) {
        log.info("****** Début du endpoint getModules (isSynthetique={}) ********", isSynthetique);
        Periode periode = buildPeriode(dateReference, datePassee);
        List<IndicateurDevopsView> result =
                indicatorDevopsModuleService.getIndicateurNiveauModule(
                        periode.origine(), periode.passee(), isSynthetique);
        log.info("****** fin du endpoint getModules********");
        return result;
    }
}
