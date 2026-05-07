package fr.insee.compas.logic.update.greenit.kube;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.bean.CsvToBeanBuilder;

import fr.insee.compas.client.OscarClient;
import fr.insee.compas.client.view.KubeOscarView;
import fr.insee.compas.exception.CompasClientException;
import fr.insee.compas.exception.CompasException;
import fr.insee.compas.exception.CompasUploadException;
import fr.insee.compas.exception.ErrorVM;
import fr.insee.compas.logic.update.greenit.GreenItMetricsUpdater;
import fr.insee.compas.mapper.MetriqueKubeMapper;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.greenit.MetriqueKube;
import fr.insee.compas.model.greenit.MetriqueKubeCsvRead;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.util.greenit.ScoreUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KubeMetricsCsvUpdater implements GreenItMetricsUpdater {
    private final MetriqueKubeMapper metriqueKubeMapper;
    private List<MetriqueKube> metriqueKubes;
    private final OscarClient oscarClient;
    private final TableFaitsRepository tableFaitsRepository;
    private static final BigDecimal NB_SEMAINES = new BigDecimal(5);

    public KubeMetricsCsvUpdater(
            OscarClient oscarClient,
            TableFaitsRepository tableFaitsRepository,
            MetriqueKubeMapper metriqueKubeMapper,
            List<MetriqueKube> kubeMetrics) {
        this.oscarClient = oscarClient;
        this.tableFaitsRepository = tableFaitsRepository;
        this.metriqueKubeMapper = metriqueKubeMapper;
        this.metriqueKubes = kubeMetrics;
    }

    public void miseAJourIndicateursGreenItFromFile(MultipartFile file, LocalDate fileDate) {
        metriqueKubes =
                loadKubeCSVData(file).stream()
                        .map(metriqueKubeMapper::toMetriqueKube)
                        .flatMap(Optional::stream)
                        .toList();
        miseAJourIndicateursGreenIT(fileDate);
    }

    public List<MetriqueKubeCsvRead> loadKubeCSVData(MultipartFile file) throws CompasException {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            return new CsvToBeanBuilder<MetriqueKubeCsvRead>(reader)
                    .withType(MetriqueKubeCsvRead.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();
        } catch (final Exception e) {
            final ErrorVM errorVM = new ErrorVM();
            log.info("Erreur lors de la lecture du csv");
            errorVM.setMessage("Erreur lors de la lecture : " + e.getMessage());
            throw new CompasUploadException(500, errorVM);
        }
    }

    public void miseAJourIndicateursGreenIT(LocalDate fileDate) {
        final ResponseEntity<List<KubeOscarView>> kubeOscars = oscarClient.getAllNamespacesOscar();
        if (kubeOscars.getBody() == null) {
            final ErrorVM errorVM = new ErrorVM();
            errorVM.setMessage("Erreur retour body Oscar");
            throw new CompasClientException(500, errorVM);
        } else {
            log.info("nombre d'applications sur Kube : " + kubeOscars.getBody().size());
            miseAJourIndicateursApplicationKubeGreenIT(kubeOscars.getBody(), fileDate);
        }
    }

    void miseAJourIndicateursApplicationKubeGreenIT(
            List<KubeOscarView> kubeOscars, LocalDate fileDate) {
        final Map<Integer, List<KubeOscarView>> kubeApplis =
                kubeOscars.stream().collect(Collectors.groupingBy(KubeOscarView::getIdApplication));
        kubeApplis
                .entrySet()
                .forEach(
                        e -> {
                            final List<String> namespaces =
                                    e.getValue().stream().map(KubeOscarView::getNamespace).toList();
                            log.info(
                                    String.format(
                                            "taille de la kube applis %d et id %d et namespace %s",
                                            namespaces.size(), e.getKey(), e.getValue()));
                            peuplerIndicateurs(e.getKey(), namespaces, fileDate);
                        });
    }

    private void peuplerIndicateurs(Integer appId, List<String> kubes, LocalDate fileDate) {
        final TableFaits indRamUsed =
                TableFaits.builder()
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_KUBE.getValue())
                        .idIndicateur(IndicateurType.RAM_CONSOMMEE.getValue())
                        .valeur(
                                calculKubeAllAgregatValeur(kubes, MetriqueKube::getRamUsed)
                                        .divide(NB_SEMAINES, RoundingMode.UP))
                        .build();
        final TableFaits indCpuUsed =
                TableFaits.builder()
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_KUBE.getValue())
                        .idIndicateur(IndicateurType.CPU_CONSOMMEE.getValue())
                        .valeur(calculKubeAllAgregatValeur(kubes, MetriqueKube::getCpuUsed))
                        .build();
        final TableFaits indS3Used =
                TableFaits.builder()
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_KUBE.getValue())
                        .idIndicateur(IndicateurType.S3_CONSOMME.getValue())
                        .valeur(
                                calculKubeAllAgregatValeur(kubes, MetriqueKube::getS3Used)
                                        .divide(NB_SEMAINES, RoundingMode.UP))
                        .build();
        final TableFaits indPvcUsed =
                TableFaits.builder()
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_KUBE.getValue())
                        .idIndicateur(IndicateurType.PVC_CONSOMME.getValue())
                        .valeur(
                                calculKubeAllAgregatValeur(kubes, MetriqueKube::getPvcUsed)
                                        .divide(NB_SEMAINES, RoundingMode.UP))
                        .build();
        final TableFaits indNbPodMaxi =
                TableFaits.builder()
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_KUBE.getValue())
                        .idIndicateur(IndicateurType.NB_POD_MAXI.getValue())
                        .valeur(
                                calculKubeAllAgregatValeur(kubes, MetriqueKube::getNbPodMaxi)
                                        .divide(NB_SEMAINES, RoundingMode.UP))
                        .build();
        final TableFaits indRamUsedProd =
                TableFaits.builder()
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_KUBE.getValue())
                        .idIndicateur(IndicateurType.RAM_CONSOMMEE_PD.getValue())
                        .valeur(
                                calculKubeProdAgregatValeur(kubes, MetriqueKube::getRamUsed)
                                        .divide(NB_SEMAINES, RoundingMode.UP))
                        .build();
        final TableFaits indCpuUsedProd =
                TableFaits.builder()
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_KUBE.getValue())
                        .idIndicateur(IndicateurType.CPU_CONSOMMEE_PD.getValue())
                        .valeur(calculKubeProdAgregatValeur(kubes, MetriqueKube::getCpuUsed))
                        .build();
        final TableFaits indS3UsedProd =
                TableFaits.builder()
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_KUBE.getValue())
                        .idIndicateur(IndicateurType.S3_CONSOMME_PD.getValue())
                        .valeur(
                                calculKubeProdAgregatValeur(kubes, MetriqueKube::getS3Used)
                                        .divide(NB_SEMAINES, RoundingMode.UP))
                        .build();
        final TableFaits indPvcUsedProd =
                TableFaits.builder()
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_KUBE.getValue())
                        .idIndicateur(IndicateurType.PVC_CONSOMME_PD.getValue())
                        .valeur(
                                calculKubeProdAgregatValeur(kubes, MetriqueKube::getPvcUsed)
                                        .divide(NB_SEMAINES, RoundingMode.UP))
                        .build();
        final TableFaits indNbPodMaxiProd =
                TableFaits.builder()
                        .idApplication(appId)
                        .date(fileDate)
                        .idSource(SourceType.FICHIER_KUBE.getValue())
                        .idIndicateur(IndicateurType.NB_POD_MAXI_PD.getValue())
                        .valeur(
                                calculKubeProdAgregatValeur(kubes, MetriqueKube::getNbPodMaxi)
                                        .divide(NB_SEMAINES, RoundingMode.UP))
                        .build();
        List<TableFaits> tableFaits =
                List.of(
                        indRamUsed,
                        indCpuUsed,
                        indS3Used,
                        indPvcUsed,
                        indNbPodMaxi,
                        indRamUsedProd,
                        indCpuUsedProd,
                        indS3UsedProd,
                        indPvcUsedProd,
                        indNbPodMaxiProd);
        tableFaitsRepository.saveAll(tableFaits);
    }

    private BigDecimal calculKubeAllAgregatValeur(
            List<String> kubes, Function<MetriqueKube, BigDecimal> valueExtractor) {
        return metriqueKubes.stream()
                .filter(m -> kubes.contains(m.getNamespace()))
                .map(valueExtractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculKubeProdAgregatValeur(
            List<String> kubes, Function<MetriqueKube, BigDecimal> valueExtractor) {
        return metriqueKubes.stream()
                .filter(m -> kubes.contains(m.getNamespace()))
                .filter(m -> ScoreUtils.isPlateformeProd(m.getEnvironnement()))
                .map(valueExtractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
