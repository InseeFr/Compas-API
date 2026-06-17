package fr.insee.compas.service.qualite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.mapper.IndicateurQualiteViewMapper;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.UtilsService;
import fr.insee.compas.service.conversion.ConversionService;
import fr.insee.compas.view.IndicateurQualiteView;

@ExtendWith(MockitoExtension.class)
class CouvertureCalculateurServiceTest {

    private IndicateurQualiteViewMapper mapper = new IndicateurQualiteViewMapper();

    @Mock private UtilsService utilsService;

    @Mock private ConversionService conversionService;

    @InjectMocks private CouvertureCalculateurService service;

    private IndicateurQualiteView view;
    private IndicateurQualiteView histo;
    private Module module;

    @BeforeEach
    void setUp() {
        view = new IndicateurQualiteView();
        histo = new IndicateurQualiteView();
        module = new Module();
        service = new CouvertureCalculateurService(utilsService, conversionService, mapper);
    }

    // =========================
    // APPLICATION - SO (negatif)
    // =========================
    @Test
    void should_return_SO_when_nbLigne_negative_application() {

        view.setNbLigneCode("-10");

        service.calculCouvertureEtEvolution(view, histo, Context.APPLICATION, null);

        assertEquals("SO", view.getLettreCouvertureTestUnitaire());
    }

    // =========================
    // APPLICATION - CALCUL
    // =========================
    @Test
    void should_calculate_percentage_when_nbLigne_positive() {

        view.setNbLigneCode("100");
        view.setNbLigneCodeNonTeste("20");

        when(utilsService.calculPourcentageCouvertureTest(100, 20)).thenReturn(80.0);

        when(conversionService.convertPourcentageEnNote(80.0)).thenReturn("A");

        service.calculCouvertureEtEvolution(view, histo, Context.APPLICATION, null);

        assertEquals("A", view.getLettreCouvertureTestUnitaire());
    }

    // =========================
    // MODULE - SO (SANS_OBJET)
    // =========================
    @Test
    void should_return_SO_when_module_is_sans_objet() {

        view.setNbLigneCode("0");
        module.setKeySonar("SANS_OBJET");

        service.calculCouvertureEtEvolution(view, histo, Context.MODULE, module);

        assertEquals("SO", view.getLettreCouvertureTestUnitaire());
    }

    // =========================
    // EVOLUTION
    // =========================
    @Test
    void should_calculate_evolution() {

        view.setNbLigneCode("100");
        view.setNbLigneCodeNonTeste("20");

        histo.setNbLigneCode("100");
        histo.setNbLigneCodeNonTeste("10");

        when(utilsService.calculPourcentageCouvertureTest(100, 20)).thenReturn(80.0);

        when(utilsService.calculPourcentageCouvertureTest(100, 10)).thenReturn(90.0);

        service.calculCouvertureEtEvolution(view, histo, Context.APPLICATION, null);

        assertEquals(-10.0, view.getEvolutionCouvertureTestUnitaire());
    }
}
