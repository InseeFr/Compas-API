package fr.insee.compas.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.compas.mapper.ApplicationConsommationElectriqueViewMapper;
import fr.insee.compas.mapper.IndicateurApplicationGreenITViewMapper;
import fr.insee.compas.mapper.IndicateurModuleGreenITViewMapper;
import fr.insee.compas.mapper.ModuleConsommationElectriqueViewMapper;
import fr.insee.compas.model.compas.dto.MetriqueApplicationDTO;
import fr.insee.compas.model.compas.dto.MetriqueModuleDTO;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.service.GreenItService;
import fr.insee.compas.view.ApplicationConsommationElectriqueView;
import fr.insee.compas.view.IndicateurApplicationGreenITView;
import fr.insee.compas.view.IndicateurModuleGreenITView;
import fr.insee.compas.view.ModuleConsommationElectriqueView;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/kpi-green")
@Tag(name = "API Kpi GreenIt", description = "API des indicateurs GreenIT")
public class GreenItController {

    private final GreenItService greenItService;
    private final IndicateurModuleGreenITViewMapper indicateurModuleGreenITViewMapper;
    private final IndicateurApplicationGreenITViewMapper indicateurApplicationGreenITViewMapper;
    private final ApplicationConsommationElectriqueViewMapper
            applicationConsommationElectriqueViewMapper;
    private final ModuleConsommationElectriqueViewMapper moduleConsommationElectriqueViewMapper;

    public GreenItController(
            GreenItService greenItService,
            IndicateurModuleGreenITViewMapper indicateurModuleGreenITViewMapper,
            IndicateurApplicationGreenITViewMapper indicateurApplicationGreenITViewMapper,
            ApplicationConsommationElectriqueViewMapper applicationConsommationElectriqueViewMapper,
            ModuleConsommationElectriqueViewMapper moduleConsommationElectriqueViewMapper) {
        this.greenItService = greenItService;
        this.indicateurModuleGreenITViewMapper = indicateurModuleGreenITViewMapper;
        this.indicateurApplicationGreenITViewMapper = indicateurApplicationGreenITViewMapper;
        this.applicationConsommationElectriqueViewMapper =
                applicationConsommationElectriqueViewMapper;
        this.moduleConsommationElectriqueViewMapper = moduleConsommationElectriqueViewMapper;
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<IndicateurApplicationGreenITView> getNombreVirtualMachine(
            @PathVariable("applicationId") Integer applicationId) {
        final IndicateurApplicationGreenIT kpiGreen =
                greenItService.getIndicateursApplicationGreenIT(applicationId);
        final Optional<IndicateurApplicationGreenITView> view =
                indicateurApplicationGreenITViewMapper.toView(kpiGreen);
        return view.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<IndicateurModuleGreenITView> getIndicateursGreenIT(
            @PathVariable("moduleId") Integer moduleId) {
        final IndicateurModuleGreenIT kpiGreen =
                greenItService.getIndicateursModuleGreenIT(moduleId);
        final Optional<IndicateurModuleGreenITView> view =
                indicateurModuleGreenITViewMapper.toView(kpiGreen);
        return view.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/applications/consommation-electrique")
    public ResponseEntity<List<ApplicationConsommationElectriqueView>>
            getApplicationConsommationElectrique() {
        final List<MetriqueApplicationDTO> consommationsDTO =
                greenItService.getApplicationConsommationElectrique();
        final List<ApplicationConsommationElectriqueView> consos =
                consommationsDTO.stream()
                        .map(applicationConsommationElectriqueViewMapper::toView)
                        .toList();
        return new ResponseEntity<>(consos, null, HttpStatus.OK);
    }

    @GetMapping("/modules/consommation-electrique")
    public ResponseEntity<List<ModuleConsommationElectriqueView>>
            getConsommationElectriqueApplication() {
        final List<MetriqueModuleDTO> consommationsDTO =
                greenItService.getModuleConsommationElectrique();
        final List<ModuleConsommationElectriqueView> consos =
                consommationsDTO.stream()
                        .map(moduleConsommationElectriqueViewMapper::toView)
                        .toList();
        return new ResponseEntity<>(consos, null, HttpStatus.OK);
    }

    @PostMapping(value = "/modules/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCSV(@RequestParam("file") final MultipartFile file) {
        greenItService.miseAJourIndicateursGreenItFromFile(file);
        return ResponseEntity.ok("Fichier CSV importé avec succès !");
    }
}
