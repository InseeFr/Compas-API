package fr.insee.compas.service.greenit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.ApplicationOscarView;
import fr.insee.compas.client.view.ModuleOscarView;
import fr.insee.compas.logic.update.greenit.kube.KubeMetricsCsvUpdater;
import fr.insee.compas.logic.update.greenit.vm.VmMetricsCsvUpdater;
import fr.insee.compas.mapper.MetriqueVmMapper;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.compas.dto.MetriqueApplicationDTO;
import fr.insee.compas.model.compas.dto.MetriqueModuleDTO;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.repository.projection.MetriqueModuleProjection;
import fr.insee.compas.repository.projection.MetriqueSumIndicateurProjection;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GreenItService {

    protected final OscarClient oscarClient;

    protected final MetriqueVmMapper metriqueVmMapper;

    protected final TableFaitsRepository tableFaitsRepository;

    private final KubeMetricsCsvUpdater kubeMetricsCsvUpdater;

    private final VmMetricsCsvUpdater vmMetricsCsvUpdater;

    private static final List<IndicateurType> GREEN_IT_INDICATORS =
            List.of(
                    IndicateurType.RAM_ALLOUEE,
                    IndicateurType.DISQUE_ALLOUE,
                    IndicateurType.DISQUE_CONSOMME,
                    IndicateurType.CPU_ALLOUEE,
                    IndicateurType.CONSO_ELEC,
                    IndicateurType.NBR_VM,
                    IndicateurType.RAM_ALLOUEE_PD,
                    IndicateurType.DISQUE_ALLOUE_PD,
                    IndicateurType.DISQUE_CONSOMME_PD,
                    IndicateurType.CPU_ALLOUEE_PD,
                    IndicateurType.CONSO_ELEC_PD,
                    IndicateurType.NBR_VM_PD,
                    IndicateurType.RAM_CONSOMMEE,
                    IndicateurType.RAM_CONSOMMEE_PD,
                    IndicateurType.CPU_CONSOMMEE,
                    IndicateurType.CPU_CONSOMMEE_PD,
                    IndicateurType.S3_CONSOMME,
                    IndicateurType.S3_CONSOMME_PD,
                    IndicateurType.PVC_CONSOMME,
                    IndicateurType.NB_POD_MAXI,
                    IndicateurType.NB_POD_MAXI_PD);

    private static final List<Integer> GREEN_IT_INDICATOR_IDS =
            GREEN_IT_INDICATORS.stream().map(IndicateurType::getValue).toList();

    public GreenItService(
            OscarClient oscarClient,
            MetriqueVmMapper metriqueVmMapper,
            TableFaitsRepository tableFaitsRepository,
            KubeMetricsCsvUpdater kubeMetricsCsvUpdater,
            VmMetricsCsvUpdater vmMetricsCsvUpdater) {
        super();
        this.oscarClient = oscarClient;
        this.metriqueVmMapper = metriqueVmMapper;
        this.tableFaitsRepository = tableFaitsRepository;
        this.vmMetricsCsvUpdater = vmMetricsCsvUpdater;
        this.kubeMetricsCsvUpdater = kubeMetricsCsvUpdater;
    }

    public IndicateurModuleGreenIT getIndicateursModuleGreenIT(Integer moduleId) {
        log.debug(
                "on rentre bien dans la construction dans le service des indicateurs green par"
                        + " module");
        final LocalDate lastDay =
                tableFaitsRepository.findLastDateIndicateur(IndicateurType.CONSO_ELEC.getValue());
        return getIndicateursModuleGreenIT(moduleId, lastDay);
    }

    public IndicateurModuleGreenIT getIndicateursModuleGreenIT(
            Integer moduleId, LocalDate lastDay) {
        log.debug(
                "on rentre bien dans la construction dans le service des indicateurs green par"
                        + " module");
        final ResponseEntity<ModuleOscarView> module = oscarClient.getModuleOscar(moduleId);
        final IndicateurModuleGreenIT greenIt = new IndicateurModuleGreenIT();
        final ModuleOscarView moduleOscarView = module.getBody();
        greenIt.setModuleId(moduleId);
        greenIt.setDateMaj(lastDay);
        greenIt.setModuleName(moduleOscarView != null ? moduleOscarView.getNom() : "anonyme");
        final List<TableFaits> ramAlloueeLatestValues =
                tableFaitsRepository.findByDateAndIdIndicateurAndIdModule(
                        lastDay, IndicateurType.RAM_ALLOUEE.getValue(), moduleId);
        final Optional<TableFaits> indRamAllouee = ramAlloueeLatestValues.stream().findFirst();
        if (indRamAllouee.isPresent()) {
            greenIt.setRamAllocated(indRamAllouee.get().getValeur().intValue());
            greenIt.setRamMaxi(
                    calculateMetricPercentModule(
                            IndicateurType.RAM_MAXI.getValue(),
                            greenIt.getRamAllocated(),
                            moduleId,
                            lastDay));
        }
        final List<TableFaits> disqueAlloueLatestValues =
                tableFaitsRepository.findByDateAndIdIndicateurAndIdModule(
                        lastDay, IndicateurType.DISQUE_ALLOUE.getValue(), moduleId);
        final Optional<TableFaits> indDisqueAlloue = disqueAlloueLatestValues.stream().findFirst();
        if (indDisqueAlloue.isPresent()) {
            greenIt.setDiskAllocated(indDisqueAlloue.get().getValeur().intValue());
            greenIt.setDiskUsed(null);
        }
        final List<TableFaits> cpuAlloueeLatestValues =
                tableFaitsRepository.findByDateAndIdIndicateurAndIdModule(
                        lastDay, IndicateurType.CPU_ALLOUEE.getValue(), moduleId);
        final Optional<TableFaits> indCpuAllouee = cpuAlloueeLatestValues.stream().findFirst();
        if (indCpuAllouee.isPresent()) {
            greenIt.setCpuAllocated(indCpuAllouee.get().getValeur().intValue());
            greenIt.setCpuMaxi(
                    calculateMetricPercentModule(
                            IndicateurType.CPU_MAXI.getValue(),
                            greenIt.getCpuAllocated(),
                            moduleId,
                            lastDay));
        }

        final List<TableFaits> consoLatestValues =
                tableFaitsRepository.findByDateAndIdIndicateurAndIdModule(
                        lastDay, IndicateurType.CONSO_ELEC.getValue(), moduleId);
        final Optional<TableFaits> indConso = consoLatestValues.stream().findFirst();
        indConso.ifPresent(i -> greenIt.setConso(i.getValeur().intValue()));
        final List<TableFaits> nbVmLatestValues =
                tableFaitsRepository.findByDateAndIdIndicateurAndIdModule(
                        lastDay, IndicateurType.NBR_VM.getValue(), moduleId);
        final Optional<TableFaits> indNbVm = nbVmLatestValues.stream().findFirst();
        indNbVm.ifPresent(i -> greenIt.setNbVm(i.getValeur().intValue()));
        return greenIt;
    }

    public IndicateurApplicationGreenIT getIndicateursApplicationGreenIT(Integer applicationId) {
        final LocalDate lastDay =
                tableFaitsRepository.findLastDateIndicateur(IndicateurType.CONSO_ELEC.getValue());
        return getIndicateursApplicationGreenIT(applicationId, lastDay);
    }

    public IndicateurApplicationGreenIT getIndicateursApplicationGreenIT(
            Integer applicationId, LocalDate lastDay) {
        final ResponseEntity<ApplicationOscarView> application =
                oscarClient.getApplicationOscar(applicationId);
        final IndicateurApplicationGreenIT greenIt = new IndicateurApplicationGreenIT();
        greenIt.setDateMaj(lastDay);
        final ApplicationOscarView applicationOscarView = application.getBody();
        greenIt.setApplicationId(applicationId);
        greenIt.setApplicationName(
                applicationOscarView != null ? applicationOscarView.getNom() : "anonyme");

        Map<Integer, BigDecimal> indicateurs = loadIndicators(applicationId, lastDay);
        greenIt.setRamAllocated(getValue(indicateurs, IndicateurType.RAM_ALLOUEE).intValue());
        greenIt.setRamAllocatedProd(
                getValue(indicateurs, IndicateurType.RAM_ALLOUEE_PD).intValue());
        greenIt.setDiskAllocated(getValue(indicateurs, IndicateurType.DISQUE_ALLOUE).intValue());
        greenIt.setDiskUsed(getValue(indicateurs, IndicateurType.DISQUE_CONSOMME).intValue());
        greenIt.setDiskAllocatedProd(
                getValue(indicateurs, IndicateurType.DISQUE_ALLOUE_PD).intValue());
        greenIt.setDiskUsedProd(
                getValue(indicateurs, IndicateurType.DISQUE_CONSOMME_PD).intValue());
        greenIt.setCpuAllocated(getValue(indicateurs, IndicateurType.CPU_ALLOUEE).intValue());
        greenIt.setCpuAllocatedProd(
                getValue(indicateurs, IndicateurType.CPU_ALLOUEE_PD).intValue());
        greenIt.setConso(getValue(indicateurs, IndicateurType.CONSO_ELEC).intValue());
        greenIt.setConsoProd(getValue(indicateurs, IndicateurType.CONSO_ELEC_PD).intValue());
        greenIt.setNbVm(getValue(indicateurs, IndicateurType.NBR_VM).intValue());
        greenIt.setNbVmProd(getValue(indicateurs, IndicateurType.NBR_VM_PD).intValue());
        greenIt.setCpuUsed(getValue(indicateurs, IndicateurType.CPU_CONSOMMEE).intValue());
        greenIt.setCpuUsedProd(getValue(indicateurs, IndicateurType.CPU_CONSOMMEE_PD).intValue());
        greenIt.setRamUsed(getValue(indicateurs, IndicateurType.RAM_CONSOMMEE).longValue());
        greenIt.setRamUsedProd(getValue(indicateurs, IndicateurType.RAM_CONSOMMEE_PD).longValue());
        greenIt.setS3Used(getValue(indicateurs, IndicateurType.S3_CONSOMME).longValue());
        greenIt.setS3UsedProd(getValue(indicateurs, IndicateurType.S3_CONSOMME_PD).longValue());
        greenIt.setPvcUsed(getValue(indicateurs, IndicateurType.PVC_CONSOMME).longValue());
        greenIt.setPvcUsedProd(getValue(indicateurs, IndicateurType.PVC_CONSOMME_PD).longValue());
        greenIt.setNbPodMaxi(getValue(indicateurs, IndicateurType.NB_POD_MAXI).intValue());
        greenIt.setNbPodMaxiProd(getValue(indicateurs, IndicateurType.NB_POD_MAXI_PD).intValue());
        return greenIt;
    }

    private Map<Integer, BigDecimal> loadIndicators(Integer idApplication, LocalDate date) {
        return tableFaitsRepository
                .findSumByDateAndListIndicateurIdsAndIdApplication(
                        date, GREEN_IT_INDICATOR_IDS, idApplication)
                .stream()
                .filter(Objects::nonNull)
                .collect(
                        Collectors.toMap(
                                MetriqueSumIndicateurProjection::getIdIndicateur,
                                p ->
                                        Optional.ofNullable(p.getTotalValeur())
                                                .orElse(BigDecimal.ZERO)));
    }

    private BigDecimal getValue(Map<Integer, BigDecimal> map, IndicateurType type) {
        return map.getOrDefault(type.getValue(), BigDecimal.valueOf(0));
    }

    private BigDecimal calculateMetricPercentModule(
            int indicateurType, Integer metricAllocated, Integer moduleId, LocalDate lastDay) {
        if (metricAllocated == 0) {
            return null;
        }
        final List<TableFaits> metricLatestValues =
                tableFaitsRepository.findByDateAndIdIndicateurAndIdModule(
                        lastDay, indicateurType, moduleId);
        final Optional<TableFaits> indicateur = metricLatestValues.stream().findFirst();
        if (!indicateur.isPresent()) {
            return null;
        }
        return calculatePercent(indicateur.get().getValeur(), new BigDecimal(metricAllocated));
    }

    private BigDecimal calculatePercent(BigDecimal numerateur, BigDecimal denominateur) {
        if (numerateur == null
                || denominateur == null
                || denominateur.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerateur
                .divide(denominateur, 10, RoundingMode.UP)
                .multiply(new BigDecimal(100))
                .setScale(0, RoundingMode.UP);
    }

    public void miseAJourVmMetricsGreenItFromFile(MultipartFile file, LocalDate fileDate) {
        vmMetricsCsvUpdater.miseAJourIndicateursGreenItFromFile(file, fileDate);
    }

    public void miseAJourKubeMetricsGreenItFromFile(MultipartFile file, LocalDate fileDate) {
        kubeMetricsCsvUpdater.miseAJourIndicateursGreenItFromFile(file, fileDate);
    }

    public List<MetriqueApplicationDTO> getApplicationMetriques() {
        return Stream.of(IndicateurType.CONSO_ELEC, IndicateurType.NB_POD_MAXI)
                .flatMap(
                        indicateur ->
                                tableFaitsRepository
                                        .findLatestSummedValuesByIndicateurForAllApplications(
                                                indicateur.getValue())
                                        .stream()
                                        .map(
                                                result ->
                                                        new MetriqueApplicationDTO(
                                                                result.getIdApplication(),
                                                                result.getDate(),
                                                                result.getTotalValeur())))
                .toList();
    }

    public List<MetriqueModuleDTO> getModuleMetriques() {
        final List<MetriqueModuleProjection> results =
                tableFaitsRepository.findLatestSummedValuesByIndicateurForAllModules(
                        IndicateurType.CONSO_ELEC.getValue());
        return results.stream()
                .map(
                        result ->
                                new MetriqueModuleDTO(
                                        result.getIdModule(),
                                        result.getDate(),
                                        result.getTotalValeur()))
                .toList();
    }
}
