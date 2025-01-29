package fr.insee.compas.service;

import static fr.insee.compas.service.SonarService.getIndicateurSonar;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.Indicateur;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.sonar.CouvertureTest;
import fr.insee.compas.repository.TableFaitsRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QualiteService {

    private final OscarService oscarService;

    private final UtilsService utilsService;

    private final TableFaitsService tableFaitsService;

    private final TableFaitsRepository tableFaitsRepository;

    private final AgregationService agregationService;

    public QualiteService(
            OscarService oscarService,
            UtilsService utilsService,
            TableFaitsService tableFaitsService,
            TableFaitsRepository tableFaitsRepository,
            AgregationService agregationService) {
        this.oscarService = oscarService;
        this.utilsService = utilsService;
        this.tableFaitsService = tableFaitsService;
        this.tableFaitsRepository = tableFaitsRepository;
        this.agregationService = agregationService;
    }

    public void miseAJourLinesTableFaitsEnBaseDeDonnees() throws IOException {

        List<Module> modules = oscarService.getModules();
        LocalDate now = LocalDate.now();
        log.debug("la date est {}", now);
        int compteurModuleOscarWithProjectKey = 0;
        int compteurModuleOscarWithProjectKeySansAnalyse = 0;
        for (Module module : modules) {
            if (!"null".equals(module.getKeySonar())
                    && !module.getKeySonar().equals("Sans objet")) {
                compteurModuleOscarWithProjectKey++;
                CouvertureTest ceLignes =
                        getIndicateurSonar(module.getKeySonar(), "lines_to_cover");
                CouvertureTest ceLignesTestes =
                        getIndicateurSonar(module.getKeySonar(), "uncovered_lines");
                if (!ceLignes.getCouverture().equals("Aucune couverture disponible")) {
                    // Utilisation d'un BigDecimal car en base nous avons un numéric. Risque
                    // d'overflow
                    tableFaitsRepository.save(
                            new TableFaits(
                                    module.getId(),
                                    module.getIdApplication(),
                                    Indicateur.NBR_LIGNE.getValue(),
                                    now,
                                    new BigDecimal(ceLignes.getCouverture()),
                                    0,
                                    null));
                    tableFaitsRepository.save(
                            new TableFaits(
                                    module.getId(),
                                    module.getIdApplication(),
                                    Indicateur.NBR_LIGNE_TEST.getValue(),
                                    now,
                                    new BigDecimal(ceLignesTestes.getCouverture()),
                                    0,
                                    null));
                } else {
                    log.warn("le module {} n'a pas une analyse sonar correcte", module.getId());
                    compteurModuleOscarWithProjectKeySansAnalyse++;
                }
            }
        }
        log.info(
                "Nombre de module Oscar avec un project Key Sonar : {}",
                compteurModuleOscarWithProjectKey);
        log.info(
                "Nombre de module Oscar avec un project Key Sonar mais pas d'analyse valide  : {}",
                compteurModuleOscarWithProjectKeySansAnalyse);
    }
}
