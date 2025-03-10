package fr.insee.compas.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.ApplicationOscarView;
import fr.insee.compas.client.view.ModuleOscarView;
import fr.insee.compas.client.view.VmOscarView;
import fr.insee.compas.exception.CompasClientException;
import fr.insee.compas.exception.ErrorVM;
import fr.insee.compas.mapper.MetriqueVmMapper;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.compas.dto.MetriqueApplicationDTO;
import fr.insee.compas.model.compas.dto.MetriqueModuleDTO;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.model.greenit.MetriqueVm;
import fr.insee.compas.model.greenit.MetriqueVmCsvRead;
import fr.insee.compas.repository.TableFaitsRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GreenItService {

    private final OscarClient oscarClient;

    private final MetriqueVmMapper metriqueVmMapper;

    private final TableFaitsRepository tableFaitsRepository;

    public GreenItService(
            OscarClient oscarClient,
            MetriqueVmMapper metriqueVmMapper,
            TableFaitsRepository tableFaitsRepository,
            List<MetriqueVm> metrics) {
        super();
        this.oscarClient = oscarClient;
        this.metriqueVmMapper = metriqueVmMapper;
        this.tableFaitsRepository = tableFaitsRepository;
        this.metrics = metrics;
    }

    private List<MetriqueVm> metrics;

    private static final Logger logger = LoggerFactory.getLogger(GreenItService.class);

    public IndicateurModuleGreenIT getIndicateursModuleGreenIT(Integer moduleId) {
        log.debug(
                "on rentre bien dans la construction dans le service des indicateurs green par"
                        + " module");
        final ResponseEntity<ModuleOscarView> module = oscarClient.getModuleOscar(moduleId);
        final IndicateurModuleGreenIT greenIt = new IndicateurModuleGreenIT();
        final ModuleOscarView moduleOscarView = module.getBody();
        greenIt.setModuleId(moduleId);
        greenIt.setModuleName(moduleOscarView != null ? moduleOscarView.getNom() : "anonyme");
        final List<TableFaits> ramAlloueeLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(
                        IndicateurType.RAM_ALLOUEE.getValue(), moduleId);
        final Optional<TableFaits> indRamAllouee = ramAlloueeLatestValues.stream().findFirst();
        if (indRamAllouee.isPresent()) {
            greenIt.setRamAllocated(indRamAllouee.get().getValeur().intValue());
            greenIt.setRamMaxi(
                    calculateMetricPercentModule(
                            IndicateurType.RAM_MAXI.getValue(),
                            greenIt.getRamAllocated(),
                            moduleId));
        }
        final List<TableFaits> disqueAlloueLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(
                        IndicateurType.DISQUE_ALLOUE.getValue(), moduleId);
        final Optional<TableFaits> indDisqueAlloue = disqueAlloueLatestValues.stream().findFirst();
        if (indDisqueAlloue.isPresent()) {
            greenIt.setDiskAllocated(indDisqueAlloue.get().getValeur().intValue());
            greenIt.setDiskUsed(
                    calculateMetricPercentModule(
                            IndicateurType.DISQUE_CONSOMME.getValue(),
                            greenIt.getDiskAllocated(),
                            moduleId));
        }
        final List<TableFaits> cpuAlloueeLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(
                        IndicateurType.CPU_ALLOUEE.getValue(), moduleId);
        final Optional<TableFaits> indCpuAllouee = cpuAlloueeLatestValues.stream().findFirst();
        if (indCpuAllouee.isPresent()) {
            greenIt.setCpuAllocated(indCpuAllouee.get().getValeur().intValue());
            greenIt.setCpuMaxi(
                    calculateMetricPercentModule(
                            IndicateurType.CPU_MAXI.getValue(),
                            greenIt.getCpuAllocated(),
                            moduleId));
        }

        final List<TableFaits> consoLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(
                        IndicateurType.CONSO_ELEC.getValue(), moduleId);
        final Optional<TableFaits> indConso = consoLatestValues.stream().findFirst();
        indConso.ifPresent(i -> greenIt.setConso(i.getValeur().intValue()));
        final List<TableFaits> nbVmLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(
                        IndicateurType.NBR_VM.getValue(), moduleId);
        final Optional<TableFaits> indNbVm = nbVmLatestValues.stream().findFirst();
        indNbVm.ifPresent(i -> greenIt.setNbVm(i.getValeur().intValue()));
        return greenIt;
    }

    public IndicateurApplicationGreenIT getIndicateursApplicationGreenIT(Integer applicationId) {
        final ResponseEntity<ApplicationOscarView> application =
                oscarClient.getApplicationOscar(applicationId);
        final IndicateurApplicationGreenIT greenIt = new IndicateurApplicationGreenIT();
        final ApplicationOscarView applicationOscarView = application.getBody();
        greenIt.setApplicationId(applicationId);
        greenIt.setApplicationName(
                applicationOscarView != null ? applicationOscarView.getNom() : "anonyme");
        final List<BigDecimal> ramAlloueeLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                        IndicateurType.RAM_ALLOUEE.getValue(), applicationId);
        final Optional<BigDecimal> indRamAllouee =
                ramAlloueeLatestValues.stream().filter(Objects::nonNull).findFirst();
        if (indRamAllouee.isPresent()) {
            greenIt.setRamAllocated(indRamAllouee.get().intValue());
            greenIt.setRamMaxi(
                    calculateMetricPercentApplication(
                            IndicateurType.RAM_MAXI.getValue(),
                            greenIt.getRamAllocated(),
                            applicationId));
        }
        final List<BigDecimal> disqueAlloueLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                        IndicateurType.DISQUE_ALLOUE.getValue(), applicationId);
        final Optional<BigDecimal> indDisqueAlloue =
                disqueAlloueLatestValues.stream().filter(Objects::nonNull).findFirst();
        if (indDisqueAlloue.isPresent()) {
            greenIt.setDiskAllocated(indDisqueAlloue.get().intValue());
            greenIt.setDiskUsed(
                    calculateMetricPercentApplication(
                            IndicateurType.DISQUE_CONSOMME.getValue(),
                            greenIt.getDiskAllocated(),
                            applicationId));
        }
        final List<BigDecimal> cpuAlloueeLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                        IndicateurType.CPU_ALLOUEE.getValue(), applicationId);
        final Optional<BigDecimal> indCpuAllouee =
                cpuAlloueeLatestValues.stream().filter(Objects::nonNull).findFirst();
        if (indCpuAllouee.isPresent()) {
            greenIt.setCpuAllocated(indCpuAllouee.get().intValue());
            greenIt.setCpuMaxi(
                    calculateMetricPercentApplication(
                            IndicateurType.CPU_MAXI.getValue(),
                            greenIt.getCpuAllocated(),
                            applicationId));
        }
        final List<BigDecimal> consoLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                        IndicateurType.CONSO_ELEC.getValue(), applicationId);
        final Optional<BigDecimal> indConso =
                consoLatestValues.stream().filter(Objects::nonNull).findFirst();
        indConso.ifPresent(i -> greenIt.setConso(i.intValue()));
        final List<BigDecimal> nbVmlatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                        IndicateurType.NBR_VM.getValue(), applicationId);
        final Optional<BigDecimal> indNbVm =
                nbVmlatestValues.stream().filter(Objects::nonNull).findFirst();
        indNbVm.ifPresent(i -> greenIt.setNbVm(i.intValue()));
        return greenIt;
    }

    private BigDecimal calculateMetricPercentModule(
            int indicateurType, Integer metricAllocated, Integer moduleId) {
        if (metricAllocated == 0) {
            return null;
        }
        final List<TableFaits> metricLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(indicateurType, moduleId);
        final Optional<TableFaits> indicateur = metricLatestValues.stream().findFirst();
        if (!indicateur.isPresent()) {
            return null;
        }
        return calculatePercent(indicateur.get().getValeur(), new BigDecimal(metricAllocated));
    }

    private BigDecimal calculateMetricPercentApplication(
            int indicateurType, Integer metricAllocated, Integer applicationId) {
        if (metricAllocated == 0) {
            return null;
        }
        final List<BigDecimal> metricLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                        indicateurType, applicationId);
        final Optional<BigDecimal> indicateur =
                metricLatestValues.stream().filter(Objects::nonNull).findFirst();
        if (!indicateur.isPresent()) {
            return null;
        }
        return calculatePercent(indicateur.get(), new BigDecimal(metricAllocated));
    }

    private BigDecimal calculatePercent(BigDecimal numerateur, BigDecimal denominateur) {
        if (denominateur == null || BigDecimal.ZERO.equals(denominateur)) {
            return null;
        }
        return numerateur.divide(denominateur, RoundingMode.UP).multiply(new BigDecimal(100));
    }

    public void miseAJourIndicateursGreenIT() {
        final ResponseEntity<List<VmOscarView>> vmOscars = oscarClient.getAllVmOscar();
        if (vmOscars.getBody() == null) {
            final ErrorVM errorVM = new ErrorVM();
            errorVM.setMessage("Erreur retour body Oscar");
            throw new CompasClientException(500, errorVM);
        } else {
            miseAJourIndicateursApplicationGreenIT(vmOscars.getBody());
            miseAJourIndicateursModuleGreenIT(vmOscars.getBody());
        }
    }

    void miseAJourIndicateursApplicationGreenIT(List<VmOscarView> vmOscars) {
        final Map<Integer, List<VmOscarView>> vmApplis =
                vmOscars.stream()
                        .filter(p -> p.getIdModule() == null)
                        .collect(Collectors.groupingBy(VmOscarView::getIdApplication));
        vmApplis.entrySet()
                .forEach(
                        e -> {
                            final List<String> vms =
                                    e.getValue().stream().map(m -> m.getNom()).toList();
                            logger.info(
                                    String.format(
                                            "taille de la vm applis %d et id %d ",
                                            vms.size(), e.getKey()));
                            peuplerIndicateurs(null, e.getKey(), vms);
                        });
    }

    void miseAJourIndicateursModuleGreenIT(List<VmOscarView> vmOscars) {
        final Map<Integer, List<VmOscarView>> vmModules =
                vmOscars.stream()
                        .filter(p -> p.getIdModule() != null)
                        .collect(Collectors.groupingBy(VmOscarView::getIdModule));
        vmModules
                .entrySet()
                .forEach(
                        e -> {
                            final Optional<VmOscarView> vmFirst =
                                    e.getValue().stream().filter(Objects::nonNull).findFirst();
                            final List<String> vms =
                                    e.getValue().stream().map(m -> m.getNom()).toList();
                            logger.info(
                                    String.format(
                                            "taille de la vm modules %d et moduleId %d ",
                                            +vms.size(), e.getKey()));
                            peuplerIndicateurs(e.getKey(), vmFirst.get().getIdApplication(), vms);
                        });
    }

    public void miseAJourIndicateursGreenItFromFile(MultipartFile file) {
        if (metrics != null && !metrics.isEmpty()) {
            logger.info("le fichier csv est déjà uploadé");
        } else {
            metrics = new ArrayList<>();
            metrics =
                    loadCSVData(file).stream()
                            .map(metriqueVmMapper::toMetriqueVm)
                            .flatMap(Optional::stream)
                            .toList();
            miseAJourIndicateursGreenIT();
        }
    }

    public List<MetriqueVmCsvRead> loadCSVData(MultipartFile file) {
        List<MetriqueVmCsvRead> metriqueVmCsvReads = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            final CsvToBean<MetriqueVmCsvRead> csvToBean =
                    new CsvToBeanBuilder<MetriqueVmCsvRead>(reader)
                            .withType(MetriqueVmCsvRead.class)
                            .withIgnoreLeadingWhiteSpace(true)
                            .build();
            metriqueVmCsvReads = csvToBean.parse();
            logger.debug("ça passe pour le parsing csv en String");
        } catch (final Exception e) {
            final ErrorVM errorVM = new ErrorVM();
            logger.info("Erreur lors de la lecture du csv");
            errorVM.setMessage("Erreur lors de la lecture : " + e.getMessage());
            throw new CompasClientException(500, errorVM);
        }
        return metriqueVmCsvReads;
    }

    private void peuplerIndicateurs(Integer modId, Integer appId, List<String> vms) {
        final TableFaits indRamAllocated = buildGreen(modId, appId);
        indRamAllocated.setIdIndicateur(IndicateurType.RAM_ALLOUEE.getValue());
        indRamAllocated.setValeur(calculAgregatValeur(vms, MetriqueVm::getRamAllocated));
        tableFaitsRepository.save(indRamAllocated);
        final TableFaits indRamMaxi = buildGreen(modId, appId);
        indRamMaxi.setIdIndicateur(IndicateurType.RAM_MAXI.getValue());
        indRamMaxi.setValeur(calculAgregatValeur(vms, MetriqueVm::getRamMaxi));
        tableFaitsRepository.save(indRamMaxi);
        final TableFaits indDiskAllocated = buildGreen(modId, appId);
        indDiskAllocated.setIdIndicateur(IndicateurType.DISQUE_ALLOUE.getValue());
        indDiskAllocated.setValeur(calculAgregatValeur(vms, MetriqueVm::getDiskAllocated));
        tableFaitsRepository.save(indDiskAllocated);
        final TableFaits indDiskUsed = buildGreen(modId, appId);
        indDiskUsed.setIdIndicateur(IndicateurType.DISQUE_CONSOMME.getValue());
        indDiskUsed.setValeur(calculAgregatValeur(vms, MetriqueVm::getDiskUsed));
        tableFaitsRepository.save(indDiskUsed);
        final TableFaits indCpuAllocated = buildGreen(modId, appId);
        indCpuAllocated.setIdIndicateur(IndicateurType.CPU_ALLOUEE.getValue());
        indCpuAllocated.setValeur(calculAgregatValeur(vms, MetriqueVm::getRamAllocated));
        tableFaitsRepository.save(indCpuAllocated);
        final TableFaits indCpuMaxi = buildGreen(modId, appId);
        indCpuMaxi.setIdIndicateur(IndicateurType.CPU_MAXI.getValue());
        indCpuMaxi.setValeur(calculAgregatValeur(vms, MetriqueVm::getRamMaxi));
        tableFaitsRepository.save(indCpuMaxi);
        final TableFaits indConso = buildGreen(modId, appId);
        indConso.setIdIndicateur(IndicateurType.CONSO_ELEC.getValue());
        indConso.setValeur(calculAgregatValeur(vms, m -> calculConsoElectrique(m.getConso())));
        tableFaitsRepository.save(indConso);
        final TableFaits indNbVm = buildGreen(modId, appId);
        indNbVm.setIdIndicateur(IndicateurType.NBR_VM.getValue());
        indNbVm.setValeur(
                BigDecimal.valueOf(metrics.stream().filter(m -> vms.contains(m.getVm())).count()));
        tableFaitsRepository.save(indNbVm);
    }

    private TableFaits buildGreen(Integer modId, Integer appId) {
        return TableFaits.builder()
                .idModule(modId)
                .idApplication(appId)
                .idSource(SourceType.FICHIER_VM.getValue())
                .date(LocalDate.now())
                .build();
    }

    private BigDecimal calculAgregatValeur(
            List<String> vms, Function<MetriqueVm, BigDecimal> valueExtractor) {
        return metrics.stream()
                .filter(m -> vms.contains(m.getVm()))
                .map(valueExtractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculConsoElectrique(BigDecimal consoIn) {
        if (consoIn == null) {
            return new BigDecimal(0);
        }
        return consoIn.multiply(new BigDecimal(30));
    }

    public List<MetriqueVm> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<MetriqueVm> metrics) {
        this.metrics = metrics;
    }

    public List<MetriqueApplicationDTO> getApplicationConsommationElectrique() {
        final List<Object[]> results =
                tableFaitsRepository.findLatestSummedValuesByIndicateurForAllApplications(
                        IndicateurType.CONSO_ELEC.getValue());
        return results.stream()
                .map(
                        result ->
                                new MetriqueApplicationDTO(
                                        (Integer) result[0],
                                        ((java.sql.Date) result[1]).toLocalDate(),
                                        (BigDecimal) result[2]))
                .toList();
    }

    public List<MetriqueModuleDTO> getModuleConsommationElectrique() {
        final List<Object[]> results =
                tableFaitsRepository.findLatestSummedValuesByIndicateurForAllModules(
                        IndicateurType.CONSO_ELEC.getValue());
        return results.stream()
                .map(
                        result ->
                                new MetriqueModuleDTO(
                                        (Integer) result[0],
                                        ((java.sql.Date) result[1]).toLocalDate(),
                                        (BigDecimal) result[2]))
                .toList();
    }
}
