package fr.insee.compas.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.compas.model.compas.ApplicationTip;
import fr.insee.compas.repository.ApplicationTipsRepository;
import fr.insee.compas.repository.MaturiteCloudRepository;
import fr.insee.compas.service.maturitecloud.ApplicationTipsService;
import fr.insee.compas.service.maturitecloud.MaturiteCloudCsvService;
import fr.insee.compas.view.IndicateurApplicationMaturiteCloud;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/cloud")
public class MaturiteCloudController {

    private final MaturiteCloudRepository repo;
    private final MaturiteCloudCsvService service;
    private final ApplicationTipsRepository tipsRepo;
    private final ApplicationTipsService tipsCsvService;

    public MaturiteCloudController(
            MaturiteCloudRepository repo,
            MaturiteCloudCsvService service,
            ApplicationTipsRepository tipsRepo,
            ApplicationTipsService tipsCsvService) {
        this.repo = repo;
        this.service = service;
        this.tipsRepo = tipsRepo;
        this.tipsCsvService = tipsCsvService;
    }

    @GetMapping("/applications")
    public List<IndicateurApplicationMaturiteCloud> getMaturiteCloud() {
        List<Object[]> rows = repo.findAllLatestMatRobAndScores();
        List<IndicateurApplicationMaturiteCloud> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Integer idApp = (Integer) r[0];
            String maturite = (String) r[1];

            BigDecimal rob = (BigDecimal) r[2];
            BigDecimal benef = (BigDecimal) r[3];
            BigDecimal orga = (BigDecimal) r[4];
            BigDecimal complex = (BigDecimal) r[5];
            BigDecimal tech = (BigDecimal) r[6];

            BigDecimal progDep = (BigDecimal) r[7];
            BigDecimal progTechs = (BigDecimal) r[8];
            BigDecimal progArchi = (BigDecimal) r[9];
            BigDecimal progMatEq = (BigDecimal) r[10];
            BigDecimal progDev = (BigDecimal) r[11];
            BigDecimal progCloud = (BigDecimal) r[12];

            String robustesse = (rob == null) ? null : Integer.toString(rob.intValue()); // "x"
            String scoreBenefice = to2d(benef);
            String scoreOrga = to2d(orga);
            String scoreComplexite = to2d(complex);
            String scoreTechnique = to2d(tech);

            String progressionDeploy = to2d(progDep);
            String progressionTechnos = to2d(progTechs);
            String progressionArchi = to2d(progArchi);
            String progressionMateqip = to2d(progMatEq);
            String progressionDevops = to2d(progDev);
            String progressionCloud = to2d(progCloud);

            out.add(
                    IndicateurApplicationMaturiteCloud.builder()
                            .applicationId(idApp)
                            .maturite(maturite)
                            .robustesse(robustesse)
                            .scoreBenefice(scoreBenefice)
                            .scoreOrga(scoreOrga)
                            .scoreComplexite(scoreComplexite)
                            .scoreTechnique(scoreTechnique)
                            .progressionDeploy(progressionDeploy)
                            .progressionTechnos(progressionTechnos)
                            .progressionArchi(progressionArchi)
                            .progressionMateqip(progressionMateqip)
                            .progressionDevops(progressionDevops)
                            .progressionCloud(progressionCloud)
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

    @GetMapping("/conseils")
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
}
