package fr.insee.compas.controller;

import java.time.LocalDate;
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

import fr.insee.compas.mapper.IndicateurApplicationGreenITViewMapper;
import fr.insee.compas.mapper.IndicateurModuleGreenITViewMapper;
import fr.insee.compas.model.compas.dto.MetriqueApplicationDTO;
import fr.insee.compas.model.compas.dto.MetriqueModuleDTO;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.service.FichierControlService;
import fr.insee.compas.service.GreenItService;
import fr.insee.compas.view.IndicateurApplicationGreenITView;
import fr.insee.compas.view.IndicateurModuleGreenITView;

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
    private final FichierControlService fichierControlService;
    private final IndicateurModuleGreenITViewMapper indicateurModuleGreenITViewMapper;
    private final IndicateurApplicationGreenITViewMapper indicateurApplicationGreenITViewMapper;

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

    @GetMapping("/applications")
    public ResponseEntity<List<IndicateurApplicationGreenITView>> getApplications() {
        final List<MetriqueApplicationDTO> consommationsDTO =
                greenItService.getApplicationMetriques();
        log.info("consommationsDTO : {}", consommationsDTO.size());
        final List<IndicateurApplicationGreenIT> kpisGreen =
                consommationsDTO.stream()
                        .map(MetriqueApplicationDTO::getIdApplication)
                        .distinct()
                        .map(greenItService::getIndicateursApplicationGreenIT)
                        .toList();
        final List<IndicateurApplicationGreenITView> views =
                kpisGreen.stream()
                        .map(indicateurApplicationGreenITViewMapper::toView)
                        .flatMap(Optional::stream)
                        .toList();
        return new ResponseEntity<>(views, null, HttpStatus.OK);
    }

    @GetMapping("/modules")
    public ResponseEntity<List<IndicateurModuleGreenITView>> getModules() {
        final List<MetriqueModuleDTO> consommationsDTO = greenItService.getModuleMetriques();
        final List<IndicateurModuleGreenIT> kpisGreen =
                consommationsDTO.stream()
                        .map(
                                dto ->
                                        greenItService.getIndicateursModuleGreenIT(
                                                dto.getIdModule(), dto.getDate()))
                        .toList();
        final List<IndicateurModuleGreenITView> views =
                kpisGreen.stream()
                        .map(indicateurModuleGreenITViewMapper::toView)
                        .flatMap(Optional::stream)
                        .toList();
        return new ResponseEntity<>(views, null, HttpStatus.OK);
    }

    @PostMapping(value = "/modules/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCSV(@RequestParam("file") final MultipartFile file) {

        final String fileName = file.getOriginalFilename();
        final LocalDate fileDate = fichierControlService.controlFileName(fileName);
        greenItService.miseAJourIndicateursGreenItFromFile(file, fileDate);
        return ResponseEntity.ok("Fichier CSV importé avec succès !");
    }
}
