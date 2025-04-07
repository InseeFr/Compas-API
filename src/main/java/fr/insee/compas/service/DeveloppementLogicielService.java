package fr.insee.compas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.compas.dto.AggregatedResultDto;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.util.DeveloppementLogicielConstantes;
import fr.insee.compas.view.IndicateurApplicationDeveloppementLogicielView;
import fr.insee.compas.view.IndicateurModuleDeveloppementLogicielView;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeveloppementLogicielService {

    private final OscarService oscarService;

    private final TableFaitsRepository tableFaitsRepository;

    private final TableFaitsService tableFaitsService;

    @Autowired
    public DeveloppementLogicielService(
            TableFaitsRepository tableFaitsRepository,
            OscarService oscarService,
            TableFaitsService tableFaitsService) {
        this.tableFaitsRepository = tableFaitsRepository;
        this.oscarService = oscarService;
        this.tableFaitsService = tableFaitsService;
    }

    /**
     * Calculate grades for modules based on the provided indicator type.
     *
     * @param indicateurType The type of the indicator to calculate grades for.
     * @return A list of views representing grades for each module.
     */
    public List<IndicateurModuleDeveloppementLogicielView> calculateGradesModule(
            IndicateurType indicateurType) {

        List<TableFaits> latestValues =
                tableFaitsRepository.findLatestValueByIndicateurByModule(indicateurType.getValue());
        List<Module> modules = oscarService.getModules();
        Map<Integer, List<TableFaits>> groupedByModule =
                latestValues.stream().collect(Collectors.groupingBy(TableFaits::getIdModule));

        List<IndicateurModuleDeveloppementLogicielView> result = new ArrayList<>();

        for (Module module : modules) {
            Integer moduleId = module.getId();
            List<TableFaits> moduleValues = groupedByModule.get(moduleId);
            if (moduleValues != null) {
                int value =
                        moduleValues.stream()
                                .findFirst()
                                .map(tableFaits -> tableFaits.getValeur().intValue())
                                .orElse(0);

                String grade;
                switch (indicateurType) {
                    case IndicateurType.DEPLOYMENT_COUNT ->
                            grade = getGradeFromDeploymentCount(value);
                    case IndicateurType.NBR_JOUR_MEP -> grade = getGradeFromDistance(value);
                    default -> grade = Notation.X.getGrade();
                }

                result.add(
                        IndicateurModuleDeveloppementLogicielView.builder()
                                .moduleId(module.getId())
                                .note(grade)
                                .value(value)
                                .build());

            } else {
                result.add(
                        IndicateurModuleDeveloppementLogicielView.builder()
                                .moduleId(module.getId())
                                .note(Notation.X.getGrade())
                                .build());
            }
        }

        return result;
    }

    /**
     * Calculate grades for applications based on the provided indicator type.
     *
     * @param indicateurType The type of the indicator to calculate grades for.
     * @return A list of views representing grades for each application.
     */
    public List<IndicateurApplicationDeveloppementLogicielView> calculateGradesApplication(
            IndicateurType indicateurType) {

        Map<Integer, AggregatedResultDto> latestValues =
                tableFaitsService.findAgregationAvgByIndicateurAndApplication(
                        indicateurType.getValue());

        List<Application> applications = oscarService.getApplications();

        List<IndicateurApplicationDeveloppementLogicielView> result = new ArrayList<>();

        for (Application application : applications) {
            Integer applicationId = application.getIdApplication();

            if (latestValues.get(applicationId) != null) {
                Integer value = latestValues.get(applicationId).getSumValeur().intValue();

                String grade;

                switch (indicateurType) {
                    case IndicateurType.DEPLOYMENT_COUNT ->
                            grade = getGradeFromDeploymentCount(value);
                    case IndicateurType.NBR_JOUR_MEP -> grade = getGradeFromDistance(value);
                    default -> grade = Notation.X.getGrade();
                }
                result.add(
                        IndicateurApplicationDeveloppementLogicielView.builder()
                                .applicationId(applicationId)
                                .note(grade)
                                .value(value)
                                .build());

            } else {
                result.add(
                        IndicateurApplicationDeveloppementLogicielView.builder()
                                .applicationId(applicationId)
                                .note(Notation.X.getGrade())
                                .build());
            }
        }

        return result;
    }

    /**
     * Update the indicator for the number of days since the last production release in the
     * database.
     */
    public void miseAJourIndicateurDistanceEnBaseDeDonnees() {
        List<Module> modules = oscarService.getModules();
        log.debug("La date est {}", LocalDate.now());

        for (Module module : modules) {
            BigDecimal valeur = BigDecimal.valueOf(-1);

            if (module.getStatut().equals(DeveloppementLogicielConstantes.EN_PRODUCTION)) {
                valeur = BigDecimal.valueOf(-2);

                LocalDate dateDerniereLivraison = module.getDateDerniereLivraisonEnProduction();
                if (dateDerniereLivraison != null) {
                    valeur =
                            BigDecimal.valueOf(
                                    ChronoUnit.DAYS.between(
                                            dateDerniereLivraison, LocalDate.now()));
                }
            }

            saveIndicator(module, IndicateurType.NBR_JOUR_MEP, valeur);
        }
    }

    /**
     * Update the indicator for the deployment count in the database using the provided start and
     * end date.
     */
    public void miseAJourIndicateurDeploymentCountEnBaseDeDonnees(
            LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
            endDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La startDate ne peut pas être après la endDate.");
        }

        List<Module> modules = oscarService.getModules();
        log.debug("La date de début est {}, la date de fin est {}", startDate, endDate);

        Map<String, List<ModuleHistorique>> allHistoriqueMap = oscarService.getModulesHistorique();

        for (Module module : modules) {
            List<ModuleHistorique> allHistorique =
                    allHistoriqueMap.get(String.valueOf(module.getId()));

            if (allHistorique != null) {
                LocalDateTime finalStartDate = startDate;
                LocalDateTime finalEndDate = endDate;
                long historiquesCount =
                        allHistorique.stream()
                                .filter(
                                        historique ->
                                                DeveloppementLogicielConstantes
                                                                .SERVICE_ACCOUNT_OSCAR4_SERVICE
                                                                .equals(
                                                                        historique
                                                                                .getAuteurOperation())
                                                        && (historique
                                                                        .getDateOperation()
                                                                        .isAfter(finalStartDate)
                                                                || historique
                                                                        .getDateOperation()
                                                                        .isEqual(finalStartDate))
                                                        && (historique
                                                                        .getDateOperation()
                                                                        .isBefore(finalEndDate)
                                                                || historique
                                                                        .getDateOperation()
                                                                        .isEqual(finalEndDate))
                                                        && DeveloppementLogicielConstantes
                                                                .MODIFICATION
                                                                .equals(historique.getOperation()))
                                .count();

                boolean isInProduction =
                        DeveloppementLogicielConstantes.EN_PRODUCTION.equals(module.getStatut());

                BigDecimal valeur =
                        isInProduction
                                ? BigDecimal.valueOf(historiquesCount)
                                : BigDecimal.valueOf(-1);

                saveIndicator(module, IndicateurType.DEPLOYMENT_COUNT, valeur);
            }
        }
    }

    /**
     * Generic method to save an indicator in the database.
     *
     * @param module The module to which the indicator is linked.
     * @param indicateur The type of indicator to be saved.
     * @param valeur The value of the indicator to be saved.
     */
    private void saveIndicator(Module module, IndicateurType indicateur, BigDecimal valeur) {
        tableFaitsRepository.save(
                TableFaits.builder()
                        .idModule(module.getId())
                        .idApplication(module.getIdApplication())
                        .idIndicateur(indicateur.getValue())
                        .date(LocalDate.now())
                        .valeur(valeur)
                        .idSource(SourceType.OSCAR.getValue())
                        .build());
    }

    /**
     * Returns the grade for the given distance value.
     *
     * @param distance The distance in days since the last delivery.
     * @return The grade corresponding to the distance value.
     */
    private String getGradeFromDistance(Integer distance) {
        if (distance == null) return null;

        if (distance == -1) return Notation.SO.getGrade();

        if (distance == -2) return Notation.NR.getGrade();

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

    /**
     * Returns the grade for the given deployment count.
     *
     * @param count The number of deployments.
     * @return The grade corresponding to the deployment count.
     */
    private String getGradeFromDeploymentCount(Integer count) {
        if (count == null) return null;

        if (count == -1) return Notation.SO.getGrade();
        if (count == -2) return Notation.NR.getGrade();

        return switch ((count > 20) ? 5 : (count - 1) / 5) {
            case 0 -> Notation.E.getGrade(); // ]0,5]
            case 1 -> Notation.D.getGrade(); // ]5,10]
            case 2 -> Notation.C.getGrade(); // ]10,15]
            case 3 -> Notation.B.getGrade(); // ]15,20]
            default -> Notation.A.getGrade(); // >20
        };
    }
}
