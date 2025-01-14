package fr.insee.compas.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.compas.mapper.IndicateurApplicationGreenITViewMapper;
import fr.insee.compas.mapper.IndicateurModuleGreenITViewMapper;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.service.GreenItService;
import fr.insee.compas.view.IndicateurApplicationGreenITView;
import fr.insee.compas.view.IndicateurModuleGreenITView;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/kpi_green")
@Tag(name = "API Kpi GreenIt", description = "API des indicateurs GreenIT")
public class GreenItController {

    private final GreenItService greenItService;
    private final IndicateurModuleGreenITViewMapper indicateurModuleGreenITViewMapper;
    private final IndicateurApplicationGreenITViewMapper indicateurApplicationGreenITViewMapper;

    public GreenItController(
            GreenItService greenItService,
            IndicateurModuleGreenITViewMapper indicateurModuleGreenITViewMapper,
            IndicateurApplicationGreenITViewMapper indicateurApplicationGreenITViewMapper) {
        this.greenItService = greenItService;
        this.indicateurModuleGreenITViewMapper = indicateurModuleGreenITViewMapper;
        this.indicateurApplicationGreenITViewMapper = indicateurApplicationGreenITViewMapper;
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<IndicateurApplicationGreenITView> getNombreVirtualMachine(
            @PathVariable("applicationId") Integer applicationId) {
        final IndicateurApplicationGreenIT kpiGreen =
                greenItService.getIndicateursApplicationGreenIT(applicationId);
        final IndicateurApplicationGreenITView view =
                indicateurApplicationGreenITViewMapper.toView(kpiGreen);
        return ResponseEntity.ok(view);
    }

    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<IndicateurModuleGreenITView> getIndicateursGreenIT(
            @PathVariable("moduleId") Integer moduleId) {
        final IndicateurModuleGreenIT kpiGreen =
                greenItService.getIndicateursModuleGreenIT(moduleId);
        final IndicateurModuleGreenITView view = indicateurModuleGreenITViewMapper.toView(kpiGreen);
        return ResponseEntity.ok(view);
    }

    @PostMapping(value = "/modules/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCSV(@RequestParam("file") final MultipartFile file) {
        greenItService.miseAJourIndicateursGreenItFromFile(file);
        return ResponseEntity.ok("Fichier CSV importé avec succès !");
    }
}
