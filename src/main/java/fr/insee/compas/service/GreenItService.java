package fr.insee.compas.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.ApplicationOscarView;
import fr.insee.compas.client.view.ApplicationTechnique;
import fr.insee.compas.client.view.ModuleOscarView;
import fr.insee.compas.mapper.MetriqueVmMapper;
import fr.insee.compas.model.Indicateur;
import fr.insee.compas.model.Source;
import fr.insee.compas.model.compas.TableFaits;
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
                        Indicateur.RAM_ALLOUEE.getValue(), moduleId);
        final Optional<TableFaits> indRamAllouee = ramAlloueeLatestValues.stream().findFirst();
        indRamAllouee.ifPresent(i -> greenIt.setRamAllocated(i.getValeur().intValue()));
        final List<TableFaits> ramMaxiLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(
                        Indicateur.RAM_MAXI.getValue(), moduleId);
        final Optional<TableFaits> indRamMaxi = ramMaxiLatestValues.stream().findFirst();
        indRamMaxi.ifPresent(
                i ->
                        greenIt.setRamMaxi(
                                i.getValeur()
                                        .divide(
                                                new BigDecimal(greenIt.getRamAllocated()),
                                                RoundingMode.UP)
                                        .multiply(new BigDecimal(100))));
        final List<TableFaits> disqueAlloueLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(
                        Indicateur.DISQUE_ALLOUE.getValue(), moduleId);
        final Optional<TableFaits> indDisqueAlloue = disqueAlloueLatestValues.stream().findFirst();
        indDisqueAlloue.ifPresent(i -> greenIt.setDiskAllocated(i.getValeur().intValue()));
        final List<TableFaits> disqueUsedLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(
                        Indicateur.DISQUE_CONSOMME.getValue(), moduleId);
        final Optional<TableFaits> indDisqueUsed = disqueUsedLatestValues.stream().findFirst();
        indDisqueUsed.ifPresent(
                i ->
                        greenIt.setDiskUsed(
                                i.getValeur()
                                        .divide(
                                                new BigDecimal(greenIt.getDiskAllocated()),
                                                RoundingMode.UP)
                                        .multiply(new BigDecimal(100))));
        final List<TableFaits> cpuAlloueeLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(
                        Indicateur.CPU_ALLOUEE.getValue(), moduleId);
        final Optional<TableFaits> indCpuAllouee = cpuAlloueeLatestValues.stream().findFirst();
        indCpuAllouee.ifPresent(i -> greenIt.setCpuAllocated(i.getValeur().intValue()));
        final List<TableFaits> cpuMaxiLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(
                        Indicateur.CPU_MAXI.getValue(), moduleId);
        final Optional<TableFaits> indCpuMaxi = cpuMaxiLatestValues.stream().findFirst();
        indCpuMaxi.ifPresent(
                i ->
                        greenIt.setCpuMaxi(
                                i.getValeur()
                                        .divide(
                                                new BigDecimal(greenIt.getCpuAllocated()),
                                                RoundingMode.UP)
                                        .multiply(new BigDecimal(100))));
        final List<TableFaits> consoLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(
                        Indicateur.CONSO_ELEC.getValue(), moduleId);
        final Optional<TableFaits> indConso = consoLatestValues.stream().findFirst();
        indConso.ifPresent(i -> greenIt.setConso(i.getValeur().intValue()));
        final List<TableFaits> nbVmLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(
                        Indicateur.NBR_VM.getValue(), moduleId);
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
                        Indicateur.RAM_ALLOUEE.getValue(), applicationId);
        final Optional<BigDecimal> indRamAllouee =
                ramAlloueeLatestValues.stream().filter(Objects::nonNull).findFirst();
        indRamAllouee.ifPresent(i -> greenIt.setRamAllocated(i.intValue()));
        final List<BigDecimal> ramMaxiLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                        Indicateur.RAM_MAXI.getValue(), applicationId);
        final Optional<BigDecimal> indRamMaxi =
                ramMaxiLatestValues.stream().filter(Objects::nonNull).findFirst();
        indRamMaxi.ifPresent(
                i ->
                        greenIt.setRamMaxi(
                                i.divide(new BigDecimal(greenIt.getRamAllocated()), RoundingMode.UP)
                                        .multiply(new BigDecimal(100))));
        final List<BigDecimal> disqueAlloueLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                        Indicateur.DISQUE_ALLOUE.getValue(), applicationId);
        final Optional<BigDecimal> indDisqueAlloue =
                disqueAlloueLatestValues.stream().filter(Objects::nonNull).findFirst();
        indDisqueAlloue.ifPresent(i -> greenIt.setDiskAllocated(i.intValue()));
        final List<BigDecimal> disqueUsedLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                        Indicateur.DISQUE_CONSOMME.getValue(), applicationId);
        final Optional<BigDecimal> indDisqueUsed =
                disqueUsedLatestValues.stream().filter(Objects::nonNull).findFirst();
        indDisqueUsed.ifPresent(
                i ->
                        greenIt.setDiskUsed(
                                i.divide(
                                                new BigDecimal(greenIt.getDiskAllocated()),
                                                RoundingMode.UP)
                                        .multiply(new BigDecimal(100))));
        final List<BigDecimal> cpuAlloueeLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                        Indicateur.CPU_ALLOUEE.getValue(), applicationId);
        final Optional<BigDecimal> indCpuAllouee =
                cpuAlloueeLatestValues.stream().filter(Objects::nonNull).findFirst();
        indCpuAllouee.ifPresent(i -> greenIt.setCpuAllocated(i.intValue()));
        final List<BigDecimal> cpuMaxiLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                        Indicateur.CPU_MAXI.getValue(), applicationId);
        final Optional<BigDecimal> indCpuMaxi =
                cpuMaxiLatestValues.stream().filter(Objects::nonNull).findFirst();
        indCpuMaxi.ifPresent(
                i ->
                        greenIt.setCpuMaxi(
                                i.divide(new BigDecimal(greenIt.getCpuAllocated()), RoundingMode.UP)
                                        .multiply(new BigDecimal(100))));
        final List<BigDecimal> consoLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                        Indicateur.CONSO_ELEC.getValue(), applicationId);
        final Optional<BigDecimal> indConso =
                consoLatestValues.stream().filter(Objects::nonNull).findFirst();
        indConso.ifPresent(i -> greenIt.setConso(i.intValue()));
        final List<BigDecimal> nbVmlatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(
                        Indicateur.NBR_VM.getValue(), applicationId);
        final Optional<BigDecimal> indNbVm =
                nbVmlatestValues.stream().filter(Objects::nonNull).findFirst();
        indNbVm.ifPresent(i -> greenIt.setNbVm(i.intValue()));
        return greenIt;
    }

    public void miseAJourIndicateursApplicationGreenIT() {
        final List<ApplicationOscarView> applicationsOscarView = new ArrayList<>();
        final ApplicationOscarView applicationOscarView = new ApplicationOscarView();
        applicationOscarView.setId(130);
        applicationOscarView.setNom("sirene4");
        final List<String> vms = new ArrayList<>();
        vms.add("pdsir4esli001");
        vms.add("pdsir4esli002");
        vms.add("pdsir4esli003");
        vms.add("pdsirene4lg001");
        applicationsOscarView.add(applicationOscarView);
        applicationsOscarView.forEach(app -> peuplerIndicateurs(null, app.getId(), vms));
    }

    public void miseAJourIndicateursModuleGreenIT() {
        // final ResponseEntity<ApplicationOscarView> application =
        // oscarClient.getApplicationOscar(applicationId);
        // final ApplicationOscarView applicationOscarView = application.getBody();
        final List<ModuleOscarView> modulesOscarView = new ArrayList<>();
        final ModuleOscarView moduleOscarView = new ModuleOscarView();
        moduleOscarView.setId(238);
        final ApplicationTechnique applicationTechnique = new ApplicationTechnique();
        applicationTechnique.setId(130);
        moduleOscarView.setApplicationTechnique(applicationTechnique);
        moduleOscarView.setNom("sirene4 - repertoire");
        final List<String> vms = new ArrayList<>();
        vms.add("pdsir4replm001");
        vms.add("pdsir4replm002");
        vms.add("pdsir4replm003");
        modulesOscarView.add(moduleOscarView);
        modulesOscarView.forEach(
                mod -> peuplerIndicateurs(mod.getId(), mod.getApplicationTechnique().getId(), vms));
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
            miseAJourIndicateursModuleGreenIT();
            miseAJourIndicateursApplicationGreenIT();
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
            throw new RuntimeException("Erreur lors de la lecture : " + e.getMessage());
        }
        return metriqueVmCsvReads;
    }

    private void peuplerIndicateurs(Integer modId, Integer appId, List<String> vms) {
        final TableFaits indRamAllocated = buildGreen(modId, appId);
        indRamAllocated.setIdIndicateur(Indicateur.RAM_ALLOUEE.getValue());
        indRamAllocated.setValeur(calculAgregatValeur(vms, MetriqueVm::getRamAllocated));
        tableFaitsRepository.save(indRamAllocated);
        final TableFaits indRamMaxi = buildGreen(modId, appId);
        indRamMaxi.setIdIndicateur(Indicateur.RAM_MAXI.getValue());
        indRamMaxi.setValeur(calculAgregatValeur(vms, MetriqueVm::getRamMaxi));
        tableFaitsRepository.save(indRamMaxi);
        final TableFaits indDiskAllocated = buildGreen(modId, appId);
        indDiskAllocated.setIdIndicateur(Indicateur.DISQUE_ALLOUE.getValue());
        indDiskAllocated.setValeur(calculAgregatValeur(vms, MetriqueVm::getDiskAllocated));
        tableFaitsRepository.save(indDiskAllocated);
        final TableFaits indDiskUsed = buildGreen(modId, appId);
        indDiskUsed.setIdIndicateur(Indicateur.DISQUE_CONSOMME.getValue());
        indDiskUsed.setValeur(calculAgregatValeur(vms, MetriqueVm::getDiskUsed));
        tableFaitsRepository.save(indDiskUsed);
        final TableFaits indCpuAllocated = buildGreen(modId, appId);
        indCpuAllocated.setIdIndicateur(Indicateur.CPU_ALLOUEE.getValue());
        indCpuAllocated.setValeur(calculAgregatValeur(vms, MetriqueVm::getRamAllocated));
        tableFaitsRepository.save(indCpuAllocated);
        final TableFaits indCpuMaxi = buildGreen(modId, appId);
        indCpuMaxi.setIdIndicateur(Indicateur.CPU_MAXI.getValue());
        indCpuMaxi.setValeur(calculAgregatValeur(vms, MetriqueVm::getRamMaxi));
        tableFaitsRepository.save(indCpuMaxi);
        final TableFaits indConso = buildGreen(modId, appId);
        indConso.setIdIndicateur(Indicateur.CONSO_ELEC.getValue());
        indConso.setValeur(calculAgregatValeur(vms, MetriqueVm::getConso));
        tableFaitsRepository.save(indConso);
        final TableFaits indNbVm = buildGreen(modId, appId);
        indNbVm.setIdIndicateur(Indicateur.NBR_VM.getValue());
        indNbVm.setValeur(
                BigDecimal.valueOf(metrics.stream().filter(m -> vms.contains(m.getVm())).count()));
        tableFaitsRepository.save(indNbVm);
    }

    private TableFaits buildGreen(Integer modId, Integer appId) {
        return TableFaits.builder()
                .idModule(modId)
                .idApplication(appId)
                .idSource(Source.FICHIER_VM.getValue())
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

    public List<MetriqueVm> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<MetriqueVm> metrics) {
        this.metrics = metrics;
    }
}
