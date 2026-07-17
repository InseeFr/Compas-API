package fr.insee.compas.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.compas.model.compas.Periode;
import fr.insee.compas.service.FichierControlService;
import fr.insee.compas.service.greenit.GreenItService;
import fr.insee.compas.util.TendanceUtils;
import fr.insee.compas.util.greenit.GreenITutils;
import fr.insee.compas.view.green.IndicateurAppGreenBaseView;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/kpi-green")
@Tag(name = "API Kpi GreenIt", description = "API des indicateurs GreenIT")
@RequiredArgsConstructor
@Slf4j
public class GreenItController {

    private final GreenItService greenItService;
    private final TendanceUtils.GreenPeriodeBuilder greenPeriodeBuilder;
    private final FichierControlService fichierControlService;

    @GetMapping(value = "/valid-dates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<LocalDate>> getValidDates() {
        log.info("Récupération des dates valides pour les indicateurs GreenIT ...");
        Set<LocalDate> validDates = greenItService.getValidDates();
        return ResponseEntity.ok(validDates);
    }

    @GetMapping(value = "/applications/{viewMode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<IndicateurAppGreenBaseView>> getApplications(
            @PathVariable GreenITutils.ViewGreen viewMode,
            @RequestParam(required = false) String origine,
            @RequestParam(required = false) String passee)
            throws IllegalAccessException {
        log.info("Récupération de l'indicateur applications de GreenIT ...");
        Periode periode = greenPeriodeBuilder.buildPeriodeGreen(origine, passee);
        List<IndicateurAppGreenBaseView> greenITViews =
                greenItService.getIndicateursApplicationGreenIT(
                        viewMode, periode.origine(), periode.passee());
        log.info("Récupération greenIt terminée");
        return ResponseEntity.ok(greenITViews);
    }

    @PostMapping(value = "/modules/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCSV(@RequestParam("file") final MultipartFile file) {

        final String fileName = file.getOriginalFilename();
        final LocalDate fileDate = fichierControlService.controlVmFileName(fileName);
        greenItService.miseAJourVmMetricsGreenItFromFile(file, fileDate);
        return ResponseEntity.ok("Fichier CSV importé avec succès !");
    }

    @PostMapping(value = "/modules/upload/kube", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadKubeCSV(@RequestParam("file") final MultipartFile file) {
        final String fileName = file.getOriginalFilename();
        final LocalDate fileDate = fichierControlService.controlKubeFileName(fileName);
        greenItService.miseAJourKubeMetricsGreenItFromFile(file, fileDate);
        return ResponseEntity.ok("Fichier CSV importé avec succès !");
    }

    @PostMapping(value = "/applications/applishare")
    public ResponseEntity<String> uploadApplishare() {
        greenItService.miseAJourApplishareMetricsGreenItFromApi();
        return ResponseEntity.ok("Chargement via Hyperx effectué avec succès !");
    }
}
