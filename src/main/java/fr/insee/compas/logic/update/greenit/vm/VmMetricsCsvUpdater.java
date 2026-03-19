package fr.insee.compas.logic.update.greenit.vm;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.VmOscarView;
import fr.insee.compas.exception.CompasClientException;
import fr.insee.compas.exception.CompasException;
import fr.insee.compas.exception.CompasUploadException;
import fr.insee.compas.exception.ErrorVM;
import fr.insee.compas.logic.update.greenit.GreenItMetricsUpdater;
import fr.insee.compas.mapper.MetriqueVmMapper;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.greenit.MetriqueVm;
import fr.insee.compas.model.greenit.MetriqueVmCsvRead;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.util.greenit.ScoreUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VmMetricsCsvUpdater implements GreenItMetricsUpdater {

    private final OscarClient oscarClient;
    private final TableFaitsRepository tableFaitsRepository;
    private final MetriqueVmMapper metriqueVmMapper;
    private List<MetriqueVm> metriqueVms;

    public static final BigDecimal MULTIPLE_POUR_CONSO_HORAIRE = new BigDecimal(12);
    private static final Map<IndicateurType, IndicateurType> MAP_IND_GLOBAUX_VS_PD =
            Map.of(
                    IndicateurType.RAM_ALLOUEE, IndicateurType.RAM_ALLOUEE_PD,
                    IndicateurType.RAM_MAXI, IndicateurType.RAM_MAXI_PD,
                    IndicateurType.DISQUE_ALLOUE, IndicateurType.DISQUE_ALLOUE_PD,
                    IndicateurType.DISQUE_CONSOMME, IndicateurType.DISQUE_CONSOMME_PD,
                    IndicateurType.CPU_ALLOUEE, IndicateurType.CPU_ALLOUEE_PD,
                    IndicateurType.CPU_MAXI, IndicateurType.CPU_MAXI_PD,
                    IndicateurType.CONSO_ELEC, IndicateurType.CONSO_ELEC_PD,
                    IndicateurType.NBR_VM, IndicateurType.NBR_VM_PD,
                    IndicateurType.CPU_CONSOMMEE, IndicateurType.CPU_CONSOMMEE_PD,
                    IndicateurType.RAM_CONSOMMEE, IndicateurType.RAM_CONSOMMEE_PD);

    public VmMetricsCsvUpdater(
            OscarClient oscarClient,
            TableFaitsRepository tableFaitsRepository,
            MetriqueVmMapper metriqueVmMapper,
            List<MetriqueVm> metriqueVms) {
        this.oscarClient = oscarClient;
        this.tableFaitsRepository = tableFaitsRepository;
        this.metriqueVmMapper = metriqueVmMapper;
        this.metriqueVms = metriqueVms;
    }

    public void miseAJourIndicateursGreenItFromFile(MultipartFile file, LocalDate fileDate) {
        metriqueVms = new ArrayList<>();
        metriqueVms =
                loadVmCSVData(file).stream()
                        .map(metriqueVmMapper::toMetriqueVm)
                        .flatMap(Optional::stream)
                        .toList();
        miseAJourIndicateursGreenIT(fileDate);
    }

    public List<MetriqueVmCsvRead> loadVmCSVData(MultipartFile file) throws CompasException {
        List<MetriqueVmCsvRead> metriqueVmCsvReads = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            final CsvToBean<MetriqueVmCsvRead> csvToBean =
                    new CsvToBeanBuilder<MetriqueVmCsvRead>(reader)
                            .withType(MetriqueVmCsvRead.class)
                            .withIgnoreLeadingWhiteSpace(true)
                            .build();
            metriqueVmCsvReads = csvToBean.parse();
            log.debug("ça passe pour le parsing csv en String");
        } catch (final Exception e) {
            final ErrorVM errorVM = new ErrorVM();
            log.info("Erreur lors de la lecture du csv");
            errorVM.setMessage("Erreur lors de la lecture : " + e.getMessage());
            throw new CompasUploadException(500, errorVM);
        }
        return metriqueVmCsvReads;
    }

    public void miseAJourIndicateursGreenIT(LocalDate fileDate) {
        final ResponseEntity<List<VmOscarView>> vmOscars = oscarClient.getAllVmOscar();
        if (vmOscars.getBody() == null) {
            final ErrorVM errorVM = new ErrorVM();
            errorVM.setMessage("Erreur retour body Oscar");
            throw new CompasClientException(500, errorVM);
        } else {
            miseAJourIndicateursApplicationGreenIT(vmOscars.getBody(), fileDate);
            miseAJourIndicateursModuleGreenIT(vmOscars.getBody(), fileDate);
        }
    }

    public void miseAJourIndicateursApplicationGreenIT(
            List<VmOscarView> vmOscars, LocalDate fileDate) {
        final Map<Integer, List<VmOscarView>> vmApplis =
                vmOscars.stream()
                        .filter(p -> p.getIdModule() == null)
                        .collect(Collectors.groupingBy(VmOscarView::getIdApplication));
        vmApplis.entrySet()
                .forEach(
                        e -> {
                            final List<String> vmsPd =
                                    e.getValue().stream()
                                            .filter(
                                                    p ->
                                                            ScoreUtils.isPlateformeProd(
                                                                    p.getPlateforme()))
                                            .map(VmOscarView::getNom)
                                            .toList();
                            final List<String> vms =
                                    e.getValue().stream().map(m -> m.getNom()).toList();
                            log.debug(
                                    String.format(
                                            "taille de la vmPd applis %d et id %d ",
                                            vmsPd.size(), e.getKey()));
                            peuplerIndicateurs(null, e.getKey(), vmsPd, fileDate, true);
                            peuplerIndicateurs(null, e.getKey(), vms, fileDate, false);
                        });
    }

    public void miseAJourIndicateursModuleGreenIT(List<VmOscarView> vmOscars, LocalDate fileDate) {
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
                            final List<String> vmsPd =
                                    e.getValue().stream()
                                            .filter(
                                                    p ->
                                                            ScoreUtils.isPlateformeProd(
                                                                    p.getPlateforme()))
                                            .map(VmOscarView::getNom)
                                            .toList();
                            final List<String> vms =
                                    e.getValue().stream().map(m -> m.getNom()).toList();
                            log.debug(
                                    String.format(
                                            "taille de la vmPd modules %d et moduleId %d ",
                                            +vmsPd.size(), e.getKey()));
                            peuplerIndicateurs(
                                    e.getKey(),
                                    vmFirst.get().getIdApplication(),
                                    vmsPd,
                                    fileDate,
                                    true);
                            peuplerIndicateurs(
                                    e.getKey(),
                                    vmFirst.get().getIdApplication(),
                                    vms,
                                    fileDate,
                                    false);
                        });
    }

    private void peuplerIndicateurs(
            Integer modId,
            Integer appId,
            List<String> vms,
            LocalDate fileDate,
            boolean isPlateformeProd) {
        final TableFaits indRamAllocated =
                TableFaits.builder()
                        .idModule(modId)
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_VM.getValue())
                        .idIndicateur(
                                indicateurGlobalOuProd(IndicateurType.RAM_ALLOUEE, isPlateformeProd)
                                        .getValue())
                        .valeur(calculAgregatValeur(vms, MetriqueVm::getRamAllocated))
                        .build();
        final TableFaits indDiskAllocated =
                TableFaits.builder()
                        .idModule(modId)
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_VM.getValue())
                        .idIndicateur(
                                indicateurGlobalOuProd(
                                                IndicateurType.DISQUE_ALLOUE, isPlateformeProd)
                                        .getValue())
                        .valeur(calculAgregatValeur(vms, MetriqueVm::getDiskAllocated))
                        .build();
        final TableFaits indDiskUsed =
                TableFaits.builder()
                        .idModule(modId)
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_VM.getValue())
                        .idIndicateur(
                                indicateurGlobalOuProd(
                                                IndicateurType.DISQUE_CONSOMME, isPlateformeProd)
                                        .getValue())
                        .valeur(calculAgregatValeur(vms, MetriqueVm::getDiskUsed))
                        .build();
        final TableFaits indCpuAllocated =
                TableFaits.builder()
                        .idModule(modId)
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_VM.getValue())
                        .idIndicateur(
                                indicateurGlobalOuProd(IndicateurType.CPU_ALLOUEE, isPlateformeProd)
                                        .getValue())
                        .valeur(calculAgregatValeur(vms, MetriqueVm::getCpuAllocated))
                        .build();
        final TableFaits indConso =
                TableFaits.builder()
                        .idModule(modId)
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_VM.getValue())
                        .idIndicateur(
                                indicateurGlobalOuProd(IndicateurType.CONSO_ELEC, isPlateformeProd)
                                        .getValue())
                        .valeur(calculAgregatValeur(vms, m -> calculConsoElectrique(m.getConso())))
                        .build();
        final TableFaits indNbVm =
                TableFaits.builder()
                        .idModule(modId)
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_VM.getValue())
                        .idIndicateur(
                                indicateurGlobalOuProd(IndicateurType.NBR_VM, isPlateformeProd)
                                        .getValue())
                        .valeur(
                                BigDecimal.valueOf(
                                        metriqueVms.stream()
                                                .filter(m -> vms.contains(m.getVm()))
                                                .count()))
                        .build();
        List<TableFaits> tableFaits =
                List.of(
                        indRamAllocated,
                        indDiskAllocated,
                        indDiskUsed,
                        indCpuAllocated,
                        indConso,
                        indNbVm);
        tableFaitsRepository.saveAll(tableFaits);
    }

    private BigDecimal calculAgregatValeur(
            List<String> vms, Function<MetriqueVm, BigDecimal> valueExtractor) {
        return metriqueVms.stream()
                .filter(m -> vms.contains(m.getVm()))
                .map(valueExtractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculConsoElectrique(BigDecimal consoIn) {
        if (consoIn == null) {
            return new BigDecimal(0);
        }
        return consoIn.multiply(MULTIPLE_POUR_CONSO_HORAIRE);
    }

    protected static IndicateurType indicateurGlobalOuProd(
            IndicateurType base, boolean isPlateformeProd) {
        return isPlateformeProd ? MAP_IND_GLOBAUX_VS_PD.getOrDefault(base, base) : base;
    }
}
