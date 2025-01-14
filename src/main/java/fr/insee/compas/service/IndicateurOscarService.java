package fr.insee.compas.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.Grade;
import fr.insee.compas.model.Indicateur;
import fr.insee.compas.model.Source;
import fr.insee.compas.model.compas.ModuleGradeDistance;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.ModuleOscarRepository;
import fr.insee.compas.repository.TableFaitsRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IndicateurOscarService {

    private final OscarService oscarService;

    private final TableFaitsRepository tableFaitsRepository;

    private final ModuleOscarRepository moduleOscarRepo;

    @Autowired
    public IndicateurOscarService(
            TableFaitsRepository tableFaitsRepository,
            OscarService oscarService,
            ModuleOscarRepository moduleOscarRepo) {
        this.tableFaitsRepository = tableFaitsRepository;
        this.oscarService = oscarService;
        this.moduleOscarRepo = moduleOscarRepo;
    }

    public Map<Integer, ModuleGradeDistance> calculateDistanceGrades() {

        List<TableFaits> latestValues =
                tableFaitsRepository.findLatestValueByIndicateur(
                        Indicateur.NBR_JOUR_MEP.getValue());
        List<Module> modules = oscarService.getModules();
        Map<Integer, List<TableFaits>> groupedByModule =
                latestValues.stream().collect(Collectors.groupingBy(TableFaits::getIdModule));

        Map<Integer, ModuleGradeDistance> result = new HashMap<>();

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

                result.put(
                        moduleId,
                        new ModuleGradeDistance(
                                module.getModName(),
                                module.getAppName(),
                                module.getSndi(),
                                module.getDomaineFonctionnel(),
                                grade));
            } else {
                result.put(
                        moduleId,
                        new ModuleGradeDistance(
                                module.getModName(),
                                module.getAppName(),
                                module.getSndi(),
                                module.getDomaineFonctionnel(),
                                "Non renseigné"));
            }
        }

        return result;
    }

    public void miseAJourLinesTableFaitsEnBaseDeDonnees() throws IOException {
        LocalDate now = LocalDate.now();
        List<Module> modules = oscarService.getModules();
        log.debug("la date est {}", now);
        for (Module module : modules) {
            if (module.getDateDerniereLivraison() != null) {
                tableFaitsRepository.save(
                        TableFaits.builder()
                                .idModule(module.getId())
                                .idApplication(module.getIdApplication())
                                .idIndicateur(Indicateur.NBR_JOUR_MEP.getValue())
                                .date(now)
                                .valeur(
                                        BigDecimal.valueOf(
                                                ChronoUnit.DAYS.between(
                                                        module.getDateDerniereLivraison(), now)))
                                .idSource(Source.OSCAR.getValue())
                                .build());
            } else {
                log.warn(
                        "Le champ dateDerniereLivraison du module {} n'existe pas", module.getId());
                tableFaitsRepository.save(
                        TableFaits.builder()
                                .idModule(module.getId())
                                .idApplication(module.getIdApplication())
                                .idIndicateur(Indicateur.NBR_JOUR_MEP.getValue())
                                .date(now)
                                .valeur(BigDecimal.valueOf(-1))
                                .idSource(Source.OSCAR.getValue())
                                .build());
            }
        }
    }

    private String getGradeFromDistance(Integer distance) {
        if (distance == null) {
            return null;
        }

        if (distance == -1) {
            return Grade.X.getGrade();
        }

        return switch (distance / 30) {
            case 0 -> // 0 <= distance <= 30
                    Grade.A.getGrade();
            case 1 -> // 31 <= distance <= 90
                    Grade.B.getGrade();
            case 2 -> // 91 <= distance <= 180
                    Grade.C.getGrade();
            case 3 -> // 181 <= distance <= 360
                    Grade.D.getGrade();
            default -> // distance > 360
                    Grade.E.getGrade();
        };
    }

    @Transactional
    public void miseAjourModuleOscarEnBaseDeDonnees() throws IOException {
        List<Module> modules = oscarService.getModules();
        // Mise à false les module de compas déjà récupéré d'oscar
        moduleOscarRepo.desactivateAllModules();
        // Mise à jour des module present dans oscar (Ajout si non présent dans compas, reactivation
        // si déjà présent dans oscar)
        for (Module module : modules) {
            moduleOscarRepo.upsertProduct(module.getId());
        }
    }
}
