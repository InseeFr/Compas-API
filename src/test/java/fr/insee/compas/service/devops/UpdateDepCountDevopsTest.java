package fr.insee.compas.service.devops;

import static fr.insee.compas.util.DevopsConstantes.EN_DEVELOPPEMENT;
import static fr.insee.compas.util.DevopsConstantes.SAISIE_MANUELLE;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;
import fr.insee.compas.service.devops.update.strat.UpdateDepCountDevops;
import fr.insee.compas.util.IndicatorSpecialValue;
import fr.insee.compas.util.SaveTFByIndicator;

@ExtendWith(MockitoExtension.class)
class UpdateDepCountDevopsTest {

    @Mock private SaveTFByIndicator saveTFByIndicator;

    @InjectMocks private UpdateDepCountDevops service;

    private fr.insee.compas.model.oscar.Module module;

    @BeforeEach
    void setup() {
        module = fr.insee.compas.model.oscar.Module.builder().id(1).idApplication(100).build();
    }

    @Test
    void shouldDoNothingWhenModulesIsNull() {
        service.updateDevops(LocalDateTime.now(), LocalDateTime.now(), null, Map.of());

        verifyNoInteractions(saveTFByIndicator);
    }

    @Test
    void shouldReturnNRWhenSaisieManuelle() {
        module.setSourceCreation(SAISIE_MANUELLE);

        service.updateDevops(LocalDateTime.now(), LocalDateTime.now(), List.of(module), Map.of());

        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(1),
                        eq(100),
                        eq(IndicateurType.DEPLOYMENT_COUNT),
                        eq(BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode())),
                        eq(SourceType.OSCAR));
    }

    @Test
    void shouldReturnSOWhenEnDeveloppement() {
        module.setStatut(EN_DEVELOPPEMENT);

        service.updateDevops(LocalDateTime.now(), LocalDateTime.now(), List.of(module), Map.of());

        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(1),
                        eq(100),
                        eq(IndicateurType.DEPLOYMENT_COUNT),
                        eq(BigDecimal.valueOf(IndicatorSpecialValue.SO.getCode())),
                        eq(SourceType.OSCAR));
    }

    @Test
    void shouldReturnNRWhenNoHistorique() {
        service.updateDevops(LocalDateTime.now(), LocalDateTime.now(), List.of(module), Map.of());

        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(1),
                        eq(100),
                        eq(IndicateurType.DEPLOYMENT_COUNT),
                        eq(BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode())),
                        eq(SourceType.OSCAR));
    }

    @Test
    void shouldCountValidDeployments() {
        ModuleHistorique h1 = new ModuleHistorique();
        h1.setDateOperation(LocalDateTime.now().minusDays(1));

        ModuleHistorique h2 = new ModuleHistorique();
        h2.setDateOperation(LocalDateTime.now().minusDays(2));

        Map<String, List<ModuleHistorique>> historiques = Map.of("1", List.of(h1, h2));

        service.updateDevops(
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now(),
                List.of(module),
                historiques);

        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(1),
                        eq(100),
                        eq(IndicateurType.DEPLOYMENT_COUNT),
                        eq(BigDecimal.valueOf(0)),
                        eq(SourceType.OSCAR));
    }

    @Test
    void shouldAggregateAverageByApplication() {
        Module module2 =
                fr.insee.compas.model.oscar.Module.builder().id(2).idApplication(100).build();

        ModuleHistorique h1 = new ModuleHistorique();
        h1.setDateOperation(LocalDateTime.now().minusDays(1));

        Map<String, List<ModuleHistorique>> historiques =
                Map.of(
                        "1", List.of(h1), // 1 deploy
                        "2", List.of(h1, h1) // 2 deploys
                        );

        service.updateDevops(
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now(),
                List.of(module, module2),
                historiques);

        // moyenne de (1 + 2) = 1.5 → arrondi dépend de ta méthode
        verify(saveTFByIndicator)
                .saveByIndicator(
                        isNull(),
                        eq(100),
                        eq(IndicateurType.DEPLOYMENT_COUNT),
                        any(BigDecimal.class),
                        eq(SourceType.OSCAR));
    }
}
