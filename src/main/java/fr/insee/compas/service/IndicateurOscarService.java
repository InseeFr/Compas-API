package fr.insee.compas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.compas.dto.AggregatedSumResultDto;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.ModuleOscarRepository;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.view.IndicateurApplicationDeveloppementLogicielView;
import fr.insee.compas.view.IndicateurModuleDeveloppementLogicielView;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IndicateurOscarService {

    private final OscarService oscarService;

    private final TableFaitsRepository tableFaitsRepository;

    private final TableFaitsService tableFaitsService;

    private final ModuleOscarRepository moduleOscarRepo;

    @Autowired
    public IndicateurOscarService(
            TableFaitsRepository tableFaitsRepository,
            OscarService oscarService,
            TableFaitsService tableFaitsService,
            ModuleOscarRepository moduleOscarRepo) {
        this.tableFaitsRepository = tableFaitsRepository;
        this.oscarService = oscarService;
        this.tableFaitsService = tableFaitsService;
        this.moduleOscarRepo = moduleOscarRepo;
    }

    public List<IndicateurModuleDeveloppementLogicielView> calculateDistanceGradesModule() {

        List<TableFaits> latestValues =
                tableFaitsRepository.findLatestValueByIndicateurByModule(
                        IndicateurType.NBR_JOUR_MEP.getValue());
        List<Module> modules = oscarService.getModules();
        Map<Integer, List<TableFaits>> groupedByModule =
                latestValues.stream().collect(Collectors.groupingBy(TableFaits::getIdModule));

        List<IndicateurModuleDeveloppementLogicielView> result = new ArrayList<>();

        for (Module module : modules) {
            Integer moduleId = module.getId();
            List<TableFaits> moduleValues = groupedByModule.get(moduleId);
            if (moduleValues != null) {

                int distance =
                        moduleValues.stream()
                                .findFirst()
                                .map(tableFaits -> tableFaits.getValeur().intValue())
                                .orElse(0);

                String grade = getGradeFromDistance(distance);
                result.add(
                        IndicateurModuleDeveloppementLogicielView.builder()
                                .moduleId(module.getId())
                                .noteDistance(grade)
                                .valueDistance(distance)
                                .build());

            } else {
                result.add(
                        IndicateurModuleDeveloppementLogicielView.builder()
                                .moduleId(module.getId())
                                .noteDistance(Notation.NR.getGrade())
                                .build());
            }
        }

        return result;
    }

    public List<IndicateurApplicationDeveloppementLogicielView>
            calculateDistanceGradesApplication() {

        Map<Integer, AggregatedSumResultDto> latestValues =
                tableFaitsService.findAgregationAvgByIndicateurAndApplication(
                        IndicateurType.NBR_JOUR_MEP.getValue());
        List<Application> applications = oscarService.getApplications();

        List<IndicateurApplicationDeveloppementLogicielView> result = new ArrayList<>();

        for (Application application : applications) {
            Integer applicationId = application.getIdApplication();

            if (latestValues.get(applicationId) != null) {
                Integer distance = latestValues.get(applicationId).getSumValeur().intValue();
                String grade = getGradeFromDistance(distance);
                result.add(
                        IndicateurApplicationDeveloppementLogicielView.builder()
                                .applicationId(application.getIdApplication())
                                .noteDistance(grade)
                                .valueDistance(distance)
                                .build());

            } else {
                result.add(
                        IndicateurApplicationDeveloppementLogicielView.builder()
                                .applicationId(application.getIdApplication())
                                .noteDistance(Notation.NR.getGrade())
                                .build());
            }
        }

        return result;
    }

    public void miseAJourLinesTableFaitsEnBaseDeDonnees() {
        LocalDate now = LocalDate.now();
        List<Module> modules = oscarService.getModules();
        log.debug("la date est {}", now);
        for (Module module : modules) {
            if (module.getDateDerniereLivraison() != null) {
                tableFaitsRepository.save(
                        TableFaits.builder()
                                .idModule(module.getId())
                                .idApplication(module.getIdApplication())
                                .idIndicateur(IndicateurType.NBR_JOUR_MEP.getValue())
                                .date(now)
                                .valeur(
                                        BigDecimal.valueOf(
                                                ChronoUnit.DAYS.between(
                                                        module.getDateDerniereLivraison(), now)))
                                .idSource(SourceType.OSCAR.getValue())
                                .build());
            } else {
                log.warn(
                        "Le champ dateDerniereLivraison du module {} n'existe pas", module.getId());
                tableFaitsRepository.save(
                        TableFaits.builder()
                                .idModule(module.getId())
                                .idApplication(module.getIdApplication())
                                .idIndicateur(IndicateurType.NBR_JOUR_MEP.getValue())
                                .date(now)
                                .valeur(BigDecimal.valueOf(-1))
                                .idSource(SourceType.OSCAR.getValue())
                                .build());
            }
        }
    }

    private String getGradeFromDistance(Integer distance) {
        if (distance == null) {
            return null;
        }

        if (distance == -1) {
            return Notation.X.getGrade();
        }

        return switch (distance / 30) {
            case 0 -> // 0 <= distance <= 30
                    Notation.A.getGrade();
            case 1 -> // 31 <= distance <= 90
                    Notation.B.getGrade();
            case 2 -> // 91 <= distance <= 180
                    Notation.C.getGrade();
            case 3 -> // 181 <= distance <= 360
                    Notation.D.getGrade();
            default -> // distance > 360
                    Notation.E.getGrade();
        };
    }

    @Transactional
    public void miseAjourModuleOscarEnBaseDeDonnees() {
        List<Module> modules = oscarService.getModules();
        // Mise à false les module de compas déjà récupéré d'oscar
        moduleOscarRepo.desactivateAllModules();
        // Mise à jour des modules present dans oscar (Ajout si non présent dans compas,
        // reactivation
        // si déjà présent dans oscar)
        for (Module module : modules) {
            log.debug("Mise à jour du module :{}", module.getId());
            moduleOscarRepo.upsertProduct(module.getId());
        }
    }
}
