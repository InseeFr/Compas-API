package fr.insee.compas.service.maturitecloud.indicateur;

import static fr.insee.compas.util.MaturiteConstantes.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.dto.MaturiteIndicateurDto;
import fr.insee.compas.model.maturite.MaturiteIndicateurTableProjection;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.util.MaturiteConstantes;
import fr.insee.compas.view.IndicateurMaturiteView;

@ExtendWith(MockitoExtension.class)
class MaturiteMapperIndicateurTest {

    @InjectMocks private MaturiteMapperIndicateur maturiteMapperIndicateur;

    @Mock private MaturiteCalculatorService maturiteCalculatorService;

    // getModulesMapped
    @Test
    void getModulesMapped_shouldReturnMappedModules_whenModulesProvided() {
        Module module = mock(fr.insee.compas.model.oscar.Module.class);
        when(module.getId()).thenReturn(1);
        when(module.getModName()).thenReturn("ModTest");
        when(module.getAppName()).thenReturn("AppTest");
        when(module.getDomaineSndi()).thenReturn("DomaineTest");
        when(module.getSourceCreation()).thenReturn("sourceTest");
        when(module.getZoneProduction()).thenReturn("cloud");

        Map<Integer, MaturiteConstantes.ModuleInfo> result =
                maturiteMapperIndicateur.getModulesMapped(List.of(module));

        assertThat(result).hasSize(1);
        assertThat(result.get(1).nameMod()).isEqualTo("ModTest");
        assertThat(result.get(1).appName()).isEqualTo("AppTest");
        assertThat(result.get(1).zoneProduction()).isEqualTo("cloud");
    }

    @Test
    void getModulesMapped_shouldReturnEmptyMap_whenNoModules() {
        Map<Integer, MaturiteConstantes.ModuleInfo> result =
                maturiteMapperIndicateur.getModulesMapped(Collections.emptyList());
        assertThat(result).isEmpty();
    }

    // resultatMaturiteIndicateurToMapByApp
    @Test
    void resultatMaturiteIndicateurToMapByApp_shouldMapModulesByApp() {
        MaturiteIndicateurTableProjection projection =
                mock(MaturiteIndicateurTableProjection.class);
        when(projection.getIdApplication()).thenReturn(1);
        when(projection.getIdModule()).thenReturn(10);
        when(projection.getIdIndicateur()).thenReturn(ID_INDICATEUR_STRAT_CLOUD);
        when(projection.getCommentaire()).thenReturn("commentaire test");
        when(projection.getValeur()).thenReturn(1);

        MaturiteConstantes.ModuleInfo moduleInfo =
                new MaturiteConstantes.ModuleInfo(
                        "ModTest", // nameMod
                        "AppTest", // appName
                        "Kube", // zoneProduction
                        "cloud",
                        "DomaineSndiTest", // domaineSndi
                        null, // domaineDev
                        null // domaineFonctionnel
                        );
        Map<Integer, MaturiteConstantes.ModuleInfo> moduleMapped = Map.of(10, moduleInfo);

        when(maturiteCalculatorService.getEnvActuelProd("Kube", "cloud")).thenReturn("Kube");
        when(maturiteCalculatorService.calculateStrategieCLoud(1)).thenReturn("Validée");

        Map<Integer, List<MaturiteIndicateurDto>> result =
                maturiteMapperIndicateur.resultatMaturiteIndicateurToMapByApp(
                        List.of(projection), moduleMapped);

        assertThat(result).hasSize(1);
        assertThat(result.get(1)).hasSize(1);
        assertThat(result.get(1).getFirst().getNameApp()).isEqualTo("AppTest");
        assertThat(result.get(1).getFirst().getStrategieCloud()).isEqualTo("Validée");
        assertThat(result.get(1).getFirst().getCommentaire()).isEqualTo("commentaire test");
    }

    @Test
    void resultatMaturiteIndicateurToMapByApp_shouldSkipModule_whenNotFoundInOscar() {
        MaturiteIndicateurTableProjection projection =
                mock(MaturiteIndicateurTableProjection.class);
        when(projection.getIdApplication()).thenReturn(1);
        when(projection.getIdModule()).thenReturn(99);

        Map<Integer, MaturiteConstantes.ModuleInfo> moduleMapped = Collections.emptyMap();

        Map<Integer, List<MaturiteIndicateurDto>> result =
                maturiteMapperIndicateur.resultatMaturiteIndicateurToMapByApp(
                        List.of(projection), moduleMapped);

        assertThat(result).isEmpty();
    }

    @Test
    void resultatMaturiteIndicateurToMapByApp_shouldSetEnvCible_whenIndicateurEnvCible() {
        MaturiteIndicateurTableProjection projection =
                mock(MaturiteIndicateurTableProjection.class);
        when(projection.getIdApplication()).thenReturn(1);
        when(projection.getIdModule()).thenReturn(10);
        when(projection.getIdIndicateur()).thenReturn(ID_INDICATEUR_ENV_CIBLE);
        when(projection.getValeur()).thenReturn(2);

        MaturiteConstantes.ModuleInfo moduleInfo =
                new MaturiteConstantes.ModuleInfo(
                        "ModTest", // nameMod
                        "AppTest", // appName
                        "Kube", // zoneProduction
                        "cloud",
                        "DomaineSndiTest", // domaineSndi
                        null, // domaineDev
                        null // domaineFonctionnel
                        );
        Map<Integer, MaturiteConstantes.ModuleInfo> moduleMapped = Map.of(10, moduleInfo);

        when(maturiteCalculatorService.getEnvActuelProd("Kube", "cloud")).thenReturn("Kube");
        when(maturiteCalculatorService.calculateEnvCible(2)).thenReturn("Cloud Externe");

        Map<Integer, List<MaturiteIndicateurDto>> result =
                maturiteMapperIndicateur.resultatMaturiteIndicateurToMapByApp(
                        List.of(projection), moduleMapped);

        assertThat(result.get(1).getFirst().getEnvCibleProd()).isEqualTo("Cloud Externe");
        assertThat(result.get(1).getFirst().getEcartCible()).isEqualTo("oui");
    }

    @Test
    void resultatMaturiteIndicateurToMapByApp_shouldSetEcartCibleNon_whenEnvCibleEqualsEnvActuel() {
        MaturiteIndicateurTableProjection projection =
                mock(MaturiteIndicateurTableProjection.class);
        when(projection.getIdApplication()).thenReturn(1);
        when(projection.getIdModule()).thenReturn(10);
        when(projection.getIdIndicateur()).thenReturn(ID_INDICATEUR_ENV_CIBLE);
        when(projection.getValeur()).thenReturn(1);

        MaturiteConstantes.ModuleInfo moduleInfo =
                new MaturiteConstantes.ModuleInfo(
                        "ModTest", // nameMod
                        "AppTest", // appName
                        "Kube", // zoneProduction
                        "cloud",
                        "DomaineSndiTest", // domaineSndi
                        null, // domaineDev
                        null // domaineFonctionnel
                        );
        Map<Integer, MaturiteConstantes.ModuleInfo> moduleMapped = Map.of(10, moduleInfo);

        when(maturiteCalculatorService.getEnvActuelProd("Kube", "cloud")).thenReturn("Kube");
        when(maturiteCalculatorService.calculateEnvCible(1)).thenReturn("Kube");

        Map<Integer, List<MaturiteIndicateurDto>> result =
                maturiteMapperIndicateur.resultatMaturiteIndicateurToMapByApp(
                        List.of(projection), moduleMapped);

        assertThat(result.get(1).getFirst().getEcartCible()).isEqualTo("non");
    }

    // maturiteMapToListIndicateurMaturiteView
    @Test
    void maturiteMapToListIndicateurMaturiteView_shouldReturnAppAndModuleViews() {
        MaturiteIndicateurDto moduleDto =
                MaturiteIndicateurDto.builder()
                        .idMod(10)
                        .idApp(1)
                        .nameApp("AppTest")
                        .nameMod("ModTest")
                        .domaineSndi("DomaineTest")
                        .envActuelProd("Kube")
                        .envCibleProd("Cloud Externe")
                        .ecartCible("oui")
                        .strategieCloud("Validée")
                        .commentaire("commentaire")
                        .tauxCloudProd(SANS_OBJET)
                        .maturite(SANS_OBJET)
                        .build();

        Map<Integer, String> maturiteByApp = Map.of(1, "Niveau 3");
        Map<Integer, List<MaturiteIndicateurDto>> maturiteByAppMap = new LinkedHashMap<>();
        maturiteByAppMap.put(1, List.of(moduleDto));

        when(maturiteCalculatorService.getAllCommentaires(any())).thenReturn("commentaire");
        when(maturiteCalculatorService.hasOneEcart(any())).thenReturn(true);
        when(maturiteCalculatorService.getStratCloud(any())).thenReturn("Validée");
        when(maturiteCalculatorService.calculateTauxCloudProd(any())).thenReturn("100%");

        List<IndicateurMaturiteView> result =
                maturiteMapperIndicateur.maturiteMapToListIndicateurMaturiteView(
                        maturiteByApp, maturiteByAppMap);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getIsModule()).isFalse();
        assertThat(result.getFirst().getAppName()).isEqualTo("AppTest");
        assertThat(result.getFirst().getMaturiteCloud()).isEqualTo("Niveau 3");
        assertThat(result.get(1).getIsModule()).isTrue();
        assertThat(result.get(1).getModuleName()).isEqualTo("ModTest");
    }

    @Test
    void maturiteMapToListIndicateurMaturiteView_shouldUseSansObjet_whenAppNotInMaturiteMap() {
        MaturiteIndicateurDto moduleDto =
                MaturiteIndicateurDto.builder()
                        .idMod(10)
                        .idApp(1)
                        .nameApp("AppTest")
                        .nameMod("ModTest")
                        .domaineSndi("DomaineTest")
                        .envActuelProd(SANS_OBJET)
                        .envCibleProd(SANS_OBJET)
                        .ecartCible(SANS_OBJET)
                        .strategieCloud(SANS_OBJET)
                        .commentaire(null)
                        .tauxCloudProd(SANS_OBJET)
                        .maturite(SANS_OBJET)
                        .build();

        Map<Integer, String> maturiteByApp = Collections.emptyMap();
        Map<Integer, List<MaturiteIndicateurDto>> maturiteByAppMap = new LinkedHashMap<>();
        maturiteByAppMap.put(1, List.of(moduleDto));

        when(maturiteCalculatorService.getAllCommentaires(any())).thenReturn("");
        when(maturiteCalculatorService.hasOneEcart(any())).thenReturn(false);
        when(maturiteCalculatorService.getStratCloud(any())).thenReturn("A instruire");
        when(maturiteCalculatorService.calculateTauxCloudProd(any())).thenReturn("0%");

        List<IndicateurMaturiteView> result =
                maturiteMapperIndicateur.maturiteMapToListIndicateurMaturiteView(
                        maturiteByApp, maturiteByAppMap);

        assertThat(result.getFirst().getMaturiteCloud()).isEqualTo(SANS_OBJET);
    }
}
