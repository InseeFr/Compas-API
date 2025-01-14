package fr.insee.compas.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.ApplicationOscarView;
import fr.insee.compas.client.view.ApplicationTechnique;
import fr.insee.compas.client.view.ModuleOscarView;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.greenit.IndicateurApplicationGreenIT;
import fr.insee.compas.model.greenit.IndicateurModuleGreenIT;
import fr.insee.compas.model.greenit.MetriqueVm;
import fr.insee.compas.repository.TableFaitsRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GreenItService {

    @Autowired private OscarClient oscarClient;

    private List<MetriqueVm> metrics;

    private static final Logger logger = LoggerFactory.getLogger(GreenItService.class);
    @Autowired private TableFaitsRepository tableFaitsRepository;

    public IndicateurModuleGreenIT getIndicateursModuleGreenIT(Integer moduleId) {
        log.debug(
                "on rentre bien dans la construction dans le service des indicateurs green par"
                        + " module");
        final ResponseEntity<ModuleOscarView> module = oscarClient.getModuleOscar(moduleId);
        final IndicateurModuleGreenIT greenIt = new IndicateurModuleGreenIT();
        final ModuleOscarView moduleOscarView = module.getBody();
        greenIt.setModuleId(moduleId);
        greenIt.setModuleName(moduleOscarView != null ? moduleOscarView.getNom() : "anonyme");
        final List<TableFaits> ramAlloueeLlatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(101, moduleId);
        final Optional<TableFaits> indRamAllouee = ramAlloueeLlatestValues.stream().findFirst();
        indRamAllouee.ifPresent(i -> greenIt.setRamAllocated(i.getValeur().intValue()));
        final List<TableFaits> disqueAllouelatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(102, moduleId);
        final Optional<TableFaits> indDisqueAlloue = disqueAllouelatestValues.stream().findFirst();
        indDisqueAlloue.ifPresent(i -> greenIt.setDiskAllocated(i.getValeur().intValue()));
        final List<TableFaits> cpuAlloueelatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(103, moduleId);
        final Optional<TableFaits> indCpuAllouee = cpuAlloueelatestValues.stream().findFirst();
        indCpuAllouee.ifPresent(i -> greenIt.setCpuAllocated(i.getValeur().intValue()));
        final List<TableFaits> disqueUsedlatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(104, moduleId);
        final Optional<TableFaits> indDisqueUsed = disqueUsedlatestValues.stream().findFirst();
        indDisqueUsed.ifPresent(i -> greenIt.setDiskUsed(i.getValeur().intValue()));
        final List<TableFaits> nbVmlatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(105, moduleId);
        final Optional<TableFaits> indNbVm = nbVmlatestValues.stream().findFirst();
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
        final List<Integer> ramAlloueeLatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(101, applicationId);
        greenIt.setRamAllocated(ramAlloueeLatestValues.stream().reduce(0, (a, b) -> a + b));
        final List<Integer> disqueAllouelatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(102, applicationId);
        greenIt.setDiskAllocated(disqueAllouelatestValues.stream().reduce(0, (a, b) -> a + b));
        final List<Integer> cpuAlloueelatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(103, applicationId);
        greenIt.setCpuAllocated(cpuAlloueelatestValues.stream().reduce(0, (a, b) -> a + b));
        final List<Integer> disqueUsedlatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(104, applicationId);
        greenIt.setDiskUsed(disqueUsedlatestValues.stream().reduce(0, (a, b) -> a + b));
        final List<Integer> nbVmlatestValues =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(105, applicationId);
        greenIt.setNbVm(nbVmlatestValues.stream().reduce(0, (a, b) -> a + b));
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
        applicationsOscarView.forEach(
                app -> {
                    final TableFaits indRamUsed = new TableFaits();
                    indRamUsed.setIdModule(null);
                    indRamUsed.setIdApplication(app.getId());
                    indRamUsed.setDate(LocalDate.now());
                    indRamUsed.setIdIndicateur(3);
                    indRamUsed.setIdSource(101);
                    indRamUsed.setValeur(
                            BigDecimal.valueOf(
                                    metrics.stream()
                                            .filter(m -> vms.contains(m.getVm()))
                                            .mapToInt(m -> m.getRamAllocated())
                                            .sum()));
                    tableFaitsRepository.save(indRamUsed);
                    final TableFaits indCpuUsed = new TableFaits();
                    indCpuUsed.setIdModule(null);
                    indCpuUsed.setIdApplication(app.getId());
                    indCpuUsed.setDate(LocalDate.now());
                    indCpuUsed.setIdIndicateur(103);
                    indCpuUsed.setIdSource(101);
                    indCpuUsed.setValeur(
                            BigDecimal.valueOf(
                                    metrics.stream()
                                            .filter(m -> vms.contains(m.getVm()))
                                            .mapToInt(m -> m.getCpuAllocated())
                                            .sum()));
                    tableFaitsRepository.save(indCpuUsed);
                    final TableFaits indDiskAllocated = new TableFaits();
                    indDiskAllocated.setIdModule(null);
                    indDiskAllocated.setIdApplication(app.getId());
                    indDiskAllocated.setDate(LocalDate.now());
                    indDiskAllocated.setIdIndicateur(102);
                    indDiskAllocated.setIdSource(101);
                    indDiskAllocated.setValeur(
                            BigDecimal.valueOf(
                                    metrics.stream()
                                            .filter(m -> vms.contains(m.getVm()))
                                            .mapToInt(m -> m.getDiskAllocated())
                                            .sum()));
                    tableFaitsRepository.save(indDiskAllocated);
                    final TableFaits indDiskUsed = new TableFaits();
                    indDiskUsed.setIdModule(null);
                    indDiskUsed.setIdApplication(app.getId());
                    indDiskUsed.setDate(LocalDate.now());
                    indDiskUsed.setIdIndicateur(104);
                    indDiskUsed.setIdSource(101);
                    indDiskUsed.setValeur(
                            BigDecimal.valueOf(
                                            metrics.stream()
                                                    .filter(m -> vms.contains(m.getVm()))
                                                    .mapToInt(m -> m.getDiskUsed())
                                                    .sum())
                                    .divide(indDiskAllocated.getValeur(), RoundingMode.UP));
                    tableFaitsRepository.save(indDiskUsed);
                    final TableFaits indNbVm = new TableFaits();
                    indNbVm.setIdModule(null);
                    indNbVm.setIdApplication(app.getId());
                    indNbVm.setDate(LocalDate.now());
                    indNbVm.setIdIndicateur(105);
                    indNbVm.setIdSource(101);
                    indNbVm.setValeur(
                            BigDecimal.valueOf(
                                    metrics.stream().filter(m -> vms.contains(m.getVm())).count()));
                    tableFaitsRepository.save(indNbVm);
                });
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
                mod -> {
                    final TableFaits indRamUsed = new TableFaits();
                    indRamUsed.setIdModule(mod.getId());
                    indRamUsed.setIdApplication(mod.getApplicationTechnique().getId());
                    indRamUsed.setDate(LocalDate.now());
                    indRamUsed.setIdIndicateur(3);
                    indRamUsed.setIdSource(101);
                    indRamUsed.setValeur(
                            BigDecimal.valueOf(
                                    metrics.stream()
                                            .filter(m -> vms.contains(m.getVm()))
                                            .mapToInt(m -> m.getRamAllocated())
                                            .sum()));
                    tableFaitsRepository.save(indRamUsed);
                    final TableFaits indCpuUsed = new TableFaits();
                    indCpuUsed.setIdModule(mod.getId());
                    indCpuUsed.setIdApplication(mod.getApplicationTechnique().getId());
                    indCpuUsed.setDate(LocalDate.now());
                    indCpuUsed.setIdIndicateur(103);
                    indCpuUsed.setIdSource(101);
                    indCpuUsed.setValeur(
                            BigDecimal.valueOf(
                                    metrics.stream()
                                            .filter(m -> vms.contains(m.getVm()))
                                            .mapToInt(m -> m.getCpuAllocated())
                                            .sum()));
                    tableFaitsRepository.save(indCpuUsed);
                    final TableFaits indDiskAllocated = new TableFaits();
                    indDiskAllocated.setIdModule(mod.getId());
                    indDiskAllocated.setIdApplication(mod.getApplicationTechnique().getId());
                    indDiskAllocated.setDate(LocalDate.now());
                    indDiskAllocated.setIdIndicateur(102);
                    indDiskAllocated.setIdSource(101);
                    indDiskAllocated.setValeur(
                            BigDecimal.valueOf(
                                    metrics.stream()
                                            .filter(m -> vms.contains(m.getVm()))
                                            .mapToInt(m -> m.getDiskAllocated())
                                            .sum()));
                    tableFaitsRepository.save(indDiskAllocated);
                    final TableFaits indDiskUsed = new TableFaits();
                    indDiskUsed.setIdModule(mod.getId());
                    indDiskUsed.setIdApplication(mod.getApplicationTechnique().getId());
                    indDiskUsed.setDate(LocalDate.now());
                    indDiskUsed.setIdIndicateur(104);
                    indDiskUsed.setIdSource(101);
                    indDiskUsed.setValeur(
                            BigDecimal.valueOf(
                                            metrics.stream()
                                                    .filter(m -> vms.contains(m.getVm()))
                                                    .mapToInt(m -> m.getDiskUsed())
                                                    .sum())
                                    .divide(indDiskAllocated.getValeur(), RoundingMode.UP));
                    tableFaitsRepository.save(indDiskUsed);
                    final TableFaits indNbVm = new TableFaits();
                    indNbVm.setIdModule(mod.getId());
                    indNbVm.setIdApplication(mod.getApplicationTechnique().getId());
                    indNbVm.setDate(LocalDate.now());
                    indNbVm.setIdIndicateur(105);
                    indNbVm.setIdSource(101);
                    indNbVm.setValeur(
                            BigDecimal.valueOf(
                                    metrics.stream().filter(m -> vms.contains(m.getVm())).count()));
                    tableFaitsRepository.save(indNbVm);
                });
    }

    public void miseAJourIndicateursGreenItFromFile(MultipartFile file) {
        if (metrics != null) {
            logger.info("le fichier csv est déjà uploadé");
        } else {
            saveCSVData(file);
            miseAJourIndicateursModuleGreenIT();
            miseAJourIndicateursApplicationGreenIT();
        }
    }

    public List<MetriqueVm> saveCSVData(MultipartFile file) {
        metrics = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            final CsvToBean<MetriqueVm> csvToBean =
                    new CsvToBeanBuilder<MetriqueVm>(reader)
                            .withType(MetriqueVm.class)
                            .withIgnoreLeadingWhiteSpace(true)
                            .build();
            metrics = csvToBean.parse();

        } catch (final Exception e) {
            throw new RuntimeException("Erreur lors de la lecture : " + e.getMessage());
        }
        return metrics;
    }

    public List<MetriqueVm> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<MetriqueVm> metrics) {
        this.metrics = metrics;
    }
}
