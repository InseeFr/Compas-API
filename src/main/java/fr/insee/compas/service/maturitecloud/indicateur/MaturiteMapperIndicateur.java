package fr.insee.compas.service.maturitecloud.indicateur;

import static fr.insee.compas.util.MaturiteConstantes.*;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fr.insee.compas.dto.MaturiteIndicateurDto;
import fr.insee.compas.model.maturite.MaturiteIndicateurTableProjection;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.util.MaturiteConstantes;
import fr.insee.compas.view.IndicateurMaturiteView;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@AllArgsConstructor
public class MaturiteMapperIndicateur {

    private MaturiteCalculatorService maturiteCalculatorService;

    public Set<Integer> verifyIdAppsOscar(List<Module> modules) {
        if (modules == null || modules.isEmpty()) return Collections.emptySet();
        return modules.stream().map(Module::getIdApplication).collect(Collectors.toSet());
    }

    public Map<Integer, MaturiteConstantes.ModuleInfo> getModulesMapped(List<Module> modules) {
        log.info("Mapping de {} modules", modules.size());
        Map<Integer, MaturiteConstantes.ModuleInfo> result =
                modules.stream()
                        .collect(
                                Collectors.toMap(
                                        Module::getId,
                                        m ->
                                                new MaturiteConstantes.ModuleInfo(
                                                        m.getModName(),
                                                        m.getAppName(),
                                                        m.getSourceCreation(), // envActuelProd
                                                        m.getZoneProduction(), // zoneProduction
                                                        m.getSndi(), // domaineSndi
                                                        m.getDomaineSndi(), // domaineDev
                                                        m
                                                                .getDomaineFonctionnel() // domaineFonctionnel
                                                        )));
        log.info("Mapping terminé : {} modules mappés", result.size());
        return result;
    }

    public Map<Integer, List<MaturiteIndicateurDto>> resultatMaturiteIndicateurToMapByApp(
            List<MaturiteIndicateurTableProjection> result,
            Map<Integer, MaturiteConstantes.ModuleInfo> moduleMapped) {
        log.info("Début du traitement des résultats SQL afin de les mapper par application");

        Map<Integer, List<MaturiteIndicateurDto>> maturiteIndicateurModuleByApp =
                new LinkedHashMap<>();
        Map<Integer, MaturiteIndicateurDto> moduleDtoMap = new LinkedHashMap<>();

        Integer idAppCurrent = result.getFirst().getIdApplication();
        for (MaturiteIndicateurTableProjection value : result) {
            log.debug("Valeur de l'idApp {}", idAppCurrent);
            if (!idAppCurrent.equals(value.getIdApplication())) {
                maturiteIndicateurModuleByApp.put(
                        idAppCurrent, new ArrayList<>(moduleDtoMap.values()));
                moduleDtoMap.clear();
                idAppCurrent = value.getIdApplication();
            }
            processProjection(value, moduleMapped, moduleDtoMap);
        }
        if (!moduleDtoMap.isEmpty()) {
            maturiteIndicateurModuleByApp.put(idAppCurrent, new ArrayList<>(moduleDtoMap.values()));
        }

        log.info(
                "Fin du traitement : {} applications parcourues",
                maturiteIndicateurModuleByApp.size());
        return maturiteIndicateurModuleByApp;
    }

    public List<IndicateurMaturiteView> maturiteMapToListIndicateurMaturiteView(
            Map<Integer, String> maturiteByApp,
            Map<Integer, List<MaturiteIndicateurDto>> maturiteIndicateurModuleByApp,
            Set<Integer> idAppsOscar) {
        log.info(
                "Début du mapping des views pour {} applications",
                maturiteIndicateurModuleByApp.size());
        List<IndicateurMaturiteView> indicateurMaturiteViewList = new ArrayList<>();
        maturiteIndicateurModuleByApp.forEach(
                (key, value) -> {
                    if (value.isEmpty()) {
                        return;
                    }
                    if (!idAppsOscar.contains(key)) {
                        log.warn("Application {} ignorée car absente du référentiel Oscar", key);
                        return;
                    }
                    String allCommentaires =
                            maturiteCalculatorService.getAllCommentaires(
                                    value.stream()
                                            .map(MaturiteIndicateurDto::getCommentaire)
                                            .toList());
                    String ecartCible =
                            maturiteCalculatorService.hasOneEcart(
                                            value.stream()
                                                    .map(MaturiteIndicateurDto::getEcartCible)
                                                    .toList())
                                    ? "oui"
                                    : "non";
                    String stratCloud =
                            maturiteCalculatorService.getStratCloud(
                                    value.stream()
                                            .map(MaturiteIndicateurDto::getStrategieCloud)
                                            .toList());
                    String tauxCloudProd =
                            maturiteCalculatorService.calculateTauxCloudProd(
                                    value.stream()
                                            .map(MaturiteIndicateurDto::getEnvActuelProd)
                                            .toList());

                    String maturite = maturiteByApp.getOrDefault(key, SANS_OBJET);

                    String envCibleProd =
                            maturiteCalculatorService.getEnvApp(
                                    value.stream()
                                            .map(MaturiteIndicateurDto::getEnvCibleProd)
                                            .toList());

                    String envActuelProd =
                            maturiteCalculatorService.getEnvApp(
                                    value.stream()
                                            .map(MaturiteIndicateurDto::getEnvActuelProd)
                                            .toList());

                    MaturiteIndicateurDto maturiteApp =
                            MaturiteIndicateurDto.builder()
                                    .idMod(null)
                                    .idApp(key)
                                    .nameMod(null)
                                    .nameApp(value.getFirst().getNameApp())
                                    .domaineSndi(value.getFirst().getDomaineSndi())
                                    .domaineDev(value.getFirst().getDomaineDev())
                                    .domaineFonctionnel(value.getFirst().getDomaineFonctionnel())
                                    .tauxCloudProd(tauxCloudProd)
                                    .envActuelProd(envActuelProd)
                                    .envCibleProd(envCibleProd)
                                    .ecartCible(ecartCible)
                                    .strategieCloud(stratCloud)
                                    .commentaire(allCommentaires)
                                    .maturite(maturite)
                                    .build();

                    indicateurMaturiteViewList.add(getAppView(maturiteApp));
                    indicateurMaturiteViewList.addAll(getIndicateurMaturiteViewListForMod(value));
                });
        log.info("Fin du mapping : {} views générées", indicateurMaturiteViewList.size());
        return indicateurMaturiteViewList;
    }

    private List<IndicateurMaturiteView> getIndicateurMaturiteViewListForMod(
            List<MaturiteIndicateurDto> modules) {
        log.debug("Mapping des dtos en views {}", modules);
        return modules.stream()
                .map(
                        m ->
                                IndicateurMaturiteView.builder()
                                        .idModule(m.getIdMod())
                                        .idApp(m.getIdApp())
                                        .appName(m.getNameApp())
                                        .moduleName(m.getNameMod())
                                        .maturiteCloud(m.getMaturite())
                                        .isModule(true)
                                        .commentaire(m.getCommentaire())
                                        .tauxCloud(m.getTauxCloudProd())
                                        .ecartCible(m.getEcartCible())
                                        .stratCloud(m.getStrategieCloud())
                                        .serviceName(m.getDomaineSndi())
                                        .domaineDev(m.getDomaineDev())
                                        .domaineFonctionnel(m.getDomaineFonctionnel())
                                        .envActuelProd(m.getEnvActuelProd())
                                        .envCibleProd(m.getEnvCibleProd())
                                        .build())
                .toList();
    }

    private IndicateurMaturiteView getAppView(MaturiteIndicateurDto maturiteApp) {
        return IndicateurMaturiteView.builder()
                .idApp(maturiteApp.getIdApp())
                .appName(maturiteApp.getNameApp())
                .maturiteCloud(maturiteApp.getMaturite())
                .isModule(false)
                .commentaire(maturiteApp.getCommentaire())
                .tauxCloud(maturiteApp.getTauxCloudProd())
                .ecartCible(maturiteApp.getEcartCible())
                .stratCloud(maturiteApp.getStrategieCloud())
                .serviceName(maturiteApp.getDomaineSndi())
                .domaineDev(maturiteApp.getDomaineDev())
                .domaineFonctionnel(maturiteApp.getDomaineFonctionnel())
                .envActuelProd(maturiteApp.getEnvActuelProd())
                .envCibleProd(maturiteApp.getEnvCibleProd())
                .build();
    }

    private void processProjection(
            MaturiteIndicateurTableProjection value,
            Map<Integer, MaturiteConstantes.ModuleInfo> moduleMapped,
            Map<Integer, MaturiteIndicateurDto> moduleDtoMap) {
        MaturiteConstantes.ModuleInfo informationsModule = moduleMapped.get(value.getIdModule());
        if (informationsModule == null) {
            log.debug(
                    "Module {} de l'application {} introuvable dans Oscar — ignoré",
                    value.getIdModule(),
                    value.getIdApplication());
            return;
        }

        String envActuelProd =
                maturiteCalculatorService.getEnvActuelProd(
                        informationsModule.envActuelProd(), informationsModule.zoneProduction());
        MaturiteIndicateurDto moduleDto =
                moduleDtoMap.computeIfAbsent(
                        value.getIdModule(),
                        idModule -> {
                            log.debug(
                                    "Création du moduleDto pour l'id module : {} et l'id app : {}",
                                    value.getIdModule(),
                                    value.getIdApplication());
                            return MaturiteIndicateurDto.builder()
                                    .idApp(value.getIdApplication())
                                    .idMod(idModule)
                                    .domaineDev(
                                            informationsModule.domaineDev() == null
                                                    ? SANS_OBJET
                                                    : informationsModule.domaineDev())
                                    .domaineSndi(informationsModule.domaineSndi())
                                    .domaineFonctionnel(informationsModule.domaineFonctionnel())
                                    .nameApp(informationsModule.appName())
                                    .nameMod(informationsModule.nameMod())
                                    .tauxCloudProd(
                                            maturiteCalculatorService.calculateTauxCloudProdModule(
                                                    envActuelProd))
                                    .envActuelProd(envActuelProd)
                                    .ecartCible(SANS_OBJET)
                                    .commentaire(null)
                                    .strategieCloud(SANS_OBJET)
                                    .envCibleProd(SANS_OBJET)
                                    .build();
                        });

        applyIndicateur(value, moduleDto, informationsModule);
    }

    private void applyIndicateur(
            MaturiteIndicateurTableProjection value,
            MaturiteIndicateurDto moduleDto,
            MaturiteConstantes.ModuleInfo informationsModule) {

        if (ID_INDICATEUR_ENV_CIBLE.equals(value.getIdIndicateur())) {
            String envCible = maturiteCalculatorService.calculateEnvCible(value.getValeur());
            moduleDto.setEnvCibleProd(envCible);
            moduleDto.setEcartCible(
                    envCible.equals(
                                    maturiteCalculatorService.getEnvActuelProd(
                                            informationsModule.envActuelProd(),
                                            informationsModule.zoneProduction()))
                            ? "non"
                            : "oui");
        }

        if (ID_INDICATEUR_STRAT_CLOUD.equals(value.getIdIndicateur())) {
            moduleDto.setStrategieCloud(
                    maturiteCalculatorService.calculateStrategieCLoud(value.getValeur()));
            moduleDto.setCommentaire(value.getCommentaire());
        }
    }
}
