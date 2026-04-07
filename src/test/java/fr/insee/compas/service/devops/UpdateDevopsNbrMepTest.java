package fr.insee.compas.service.devops;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import fr.insee.compas.service.devops.update.strat.UpdateDevopsNbrMep;
import fr.insee.compas.util.DevopsConstantes;
import fr.insee.compas.util.IndicatorSpecialValue;
import fr.insee.compas.util.SaveTFByIndicator;

@ExtendWith(MockitoExtension.class)
class UpdateDevopsNbrMepTest {

    @Mock private SaveTFByIndicator saveTFByIndicator;

    @InjectMocks private UpdateDevopsNbrMep service;

    private fr.insee.compas.model.oscar.Module module;

    @BeforeEach
    void setup() {
        module = fr.insee.compas.model.oscar.Module.builder().id(1).idApplication(100).build();
    }

    @Test
    void shouldDoNothingWhenModulesNull() {
        service.updateDevops(null, null, null, Map.of());

        verifyNoInteractions(saveTFByIndicator);
    }

    @Test
    void shouldReturnSOWhenSaisieManuelle() {
        module.setSourceCreation(DevopsConstantes.SAISIE_MANUELLE);

        service.updateDevops(null, null, List.of(module), Map.of());

        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(1),
                        eq(100),
                        eq(IndicateurType.NBR_JOUR_MEP),
                        eq(BigDecimal.valueOf(IndicatorSpecialValue.SO.getCode())),
                        eq(SourceType.OSCAR));
    }

    @Test
    void shouldReturnSOWhenEnDeveloppement() {
        module.setStatut(DevopsConstantes.EN_DEVELOPPEMENT);

        service.updateDevops(null, null, List.of(module), Map.of());

        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(1),
                        eq(100),
                        eq(IndicateurType.NBR_JOUR_MEP),
                        eq(BigDecimal.valueOf(IndicatorSpecialValue.SO.getCode())),
                        eq(SourceType.OSCAR));
    }

    @Test
    void shouldReturnNRWhenDateLivraisonNull() {
        module.setDateDerniereLivraisonEnProduction(null);

        service.updateDevops(null, null, List.of(module), Map.of());

        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(1),
                        eq(100),
                        eq(IndicateurType.NBR_JOUR_MEP),
                        eq(BigDecimal.valueOf(IndicatorSpecialValue.NR.getCode())),
                        eq(SourceType.OSCAR));
    }

    @Test
    void shouldCalculateDaysSinceLastProduction() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        module.setDateDerniereLivraisonEnProduction(yesterday);

        service.updateDevops(null, null, List.of(module), Map.of());

        verify(saveTFByIndicator)
                .saveByIndicator(
                        eq(1),
                        eq(100),
                        eq(IndicateurType.NBR_JOUR_MEP),
                        eq(BigDecimal.valueOf(1)),
                        eq(SourceType.OSCAR));
    }

    @Test
    void shouldAggregateAverageByApplication() {
        Module module2 =
                fr.insee.compas.model.oscar.Module.builder().id(2).idApplication(100).build();

        module.setDateDerniereLivraisonEnProduction(LocalDate.now().minusDays(2)); // 2 jours
        module2.setDateDerniereLivraisonEnProduction(LocalDate.now().minusDays(4)); // 4 jours

        service.updateDevops(null, null, List.of(module, module2), Map.of());

        // moyenne = (2 + 4) / 2 = 3
        verify(saveTFByIndicator)
                .saveByIndicator(
                        isNull(),
                        eq(100),
                        eq(IndicateurType.NBR_JOUR_MEP),
                        eq(BigDecimal.valueOf(3)),
                        eq(SourceType.OSCAR));
    }
}
