package fr.insee.compas.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.compas.dto.maturite.DemandeCreationStrategieCloud;
import fr.insee.compas.dto.maturite.DerniereStrategieCloudDto;
import fr.insee.compas.model.compas.ApplicationTip;
import fr.insee.compas.repository.ApplicationTipsRepository;
import fr.insee.compas.repository.MaturiteCloudRepository;
import fr.insee.compas.repository.projection.maturite.MaturiteCloudProjection;
import fr.insee.compas.service.maturitecloud.ApplicationTipsService;
import fr.insee.compas.service.maturitecloud.MaturiteCloudCsvService;
import fr.insee.compas.service.maturitecloud.indicateur.*;
import fr.insee.compas.service.maturitecloud.indicateur.CloudCreationService;
import fr.insee.compas.service.maturitecloud.indicateur.ICloudCreation;
import fr.insee.compas.service.maturitecloud.indicateur.IMaturiteIndicateur;
import fr.insee.compas.service.maturitecloud.indicateur.MaturiteIndicateurService;
import fr.insee.compas.view.IndicateurApplicationMaturiteCloud;
import fr.insee.compas.view.IndicateurMaturiteView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/cloud")
public class MaturiteCloudController {

    private final MaturiteCloudRepository repo;
    private final MaturiteCloudCsvService service;
    private final IMaturiteIndicateur maturiteIndicateurService;
    private final ApplicationTipsRepository tipsRepo;
    private final ApplicationTipsService tipsCsvService;
    private final ICloudCreation cloudCreationService;

    public MaturiteCloudController(
            MaturiteCloudRepository repo,
            MaturiteCloudCsvService service,
            ApplicationTipsRepository tipsRepo,
            ApplicationTipsService tipsCsvService,
            MaturiteIndicateurService maturiteIndicateurService,
            CloudCreationService cloudCreationService) {
        this.repo = repo;
        this.service = service;
        this.tipsRepo = tipsRepo;
        this.maturiteIndicateurService = maturiteIndicateurService;
        this.tipsCsvService = tipsCsvService;
        this.cloudCreationService = cloudCreationService;
    }

    @GetMapping(value = "/indicateur", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Récupération des indicateurs de maturité cloud")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Indicateurs récupérés avec succès",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        array =
                                                @ArraySchema(
                                                        schema =
                                                                @Schema(
                                                                        implementation =
                                                                                IndicateurMaturiteView
                                                                                        .class)))),
                @ApiResponse(
                        responseCode = "204",
                        description = "Aucun indicateur trouvé",
                        content = @Content),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erreur interne du serveur",
                        content = @Content)
            })
    public ResponseEntity<List<IndicateurMaturiteView>> getIndicateur() {
        List<IndicateurMaturiteView> result = maturiteIndicateurService.getIndicateurMaturite();
        if (result.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/applications", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<IndicateurApplicationMaturiteCloud> getMaturiteCloud() {
        List<MaturiteCloudProjection> rows = repo.findAllLatestMatRobAndScores();
        List<IndicateurApplicationMaturiteCloud> out = new ArrayList<>(rows.size());
        for (MaturiteCloudProjection r : rows) {
            String robustesse =
                    (r.getRobustesse() == null)
                            ? null
                            : Integer.toString(r.getRobustesse().intValue());
            String scoreMaturite =
                    (r.getScoreMaturite() == null)
                            ? null
                            : r.getScoreMaturite().setScale(2, RoundingMode.HALF_UP).toPlainString();
            String deltaMaturite =
                    (r.getDeltaMaturite() == null)
                            ? null
                            : r.getDeltaMaturite().setScale(2, RoundingMode.HALF_UP).toPlainString();
            String deltaOpportunite =
                    (r.getDeltaOpportunite() == null)
                            ? null
                            : r.getDeltaOpportunite().setScale(2, RoundingMode.HALF_UP).toPlainString();

            out.add(
                    IndicateurApplicationMaturiteCloud.builder()
                            .applicationId(r.getIdApplication())
                            .maturite(r.getMaturite())
                            .scoreMaturite(scoreMaturite)
                            .deltaMaturite(deltaMaturite)
                            .deltaOpportunite(deltaOpportunite)
                            .robustesse(robustesse)
                            .scoreBenefice(to2d(r.getScoreBenefice()))
                            .scoreOrga(to2d(r.getScoreOrga()))
                            .scoreComplexite(to2d(r.getScoreComplexite()))
                            .scoreTechnique(to2d(r.getScoreTechnique()))
                            .scoreOpportunite(to2d(r.getScoreOpportunite()))
                            .progressionDeploy(to2d(r.getProgressionDeploy()))
                            .progressionTechnos(to2d(r.getProgressionTechnos()))
                            .progressionArchi(to2d(r.getProgressionArchi()))
                            .progressionMateqip(to2d(r.getProgressionMateqip()))
                            .progressionDevops(to2d(r.getProgressionDevops()))
                            .progressionCloud(to2d(r.getProgressionCloud()))
                            .build());
        }
        return out;
    }

    private static String to2d(BigDecimal v) {
        return (v == null) ? null : v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    @PostMapping(path = "/maturite-cloud/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Integer>> importFaitsMaturiteCloud(
            @RequestPart("file") MultipartFile file) throws Exception {
        int inserted = service.importCsv(file);
        return ResponseEntity.ok(Map.of("insertedCount", inserted));
    }

    @GetMapping(value = "/conseils", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ApplicationTip> getApplicationConseils(@RequestParam("nom_oscar") String nomOscar) {
        // ignore la casse côté requête
        return tipsRepo.findAllByNomOscarIgnoreCaseOrderByDateDescIdDesc(nomOscar);
    }

    @PostMapping(path = "/conseils/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importApplicationTips(
            @RequestPart("file") MultipartFile file,
            @RequestParam int sourceId // 1 = technique, 2 = orga
            ) throws Exception {
        int inserted = tipsCsvService.importCsv(file, sourceId);
        return ResponseEntity.ok(
                Map.of(
                        "insertedCount", inserted,
                        "sourceId", sourceId));
    }

    @PostMapping(
            value = "/strategie",
            consumes = "application/json",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Saisie de la maturité cloud")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Saisie de la maturité cloud avec succès "),
                @ApiResponse(
                        responseCode = "500",
                        description = "Erreur interne du serveur",
                        content = @Content)
            })
    public ResponseEntity<List<Long>> saisirStrategieCloud(
            @Valid @RequestBody DemandeCreationStrategieCloud demande) {

        List<Long> idsFaits = cloudCreationService.creerStrategieCloud(demande);

        return ResponseEntity.ok(idsFaits);
    }

    @GetMapping(value = "/strategie/{idModule}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Récupération de la dernière stratégie cloud par id module ")
    public ResponseEntity<DerniereStrategieCloudDto> getDerniereStrategie(
            @PathVariable Integer idModule) {
        log.info("Récupération de la dernière stratégie cloud pour le module {}", idModule);
        DerniereStrategieCloudDto dto =
                maturiteIndicateurService.getDerniereStrategieCloud(idModule);

        if (dto == null) {
            dto =
                    DerniereStrategieCloudDto.builder()
                            .idMod(idModule)
                            .strategieCloud(null)
                            .envCibleProd(null)
                            .commentaire(null)
                            .build();
        }
        return ResponseEntity.ok(dto);
    }
}
