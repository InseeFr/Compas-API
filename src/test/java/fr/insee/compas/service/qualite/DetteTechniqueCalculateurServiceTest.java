package fr.insee.compas.service.qualite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.conversion.ConversionService;
import fr.insee.compas.view.IndicateurQualiteView;

@ExtendWith(MockitoExtension.class)
class DetteTechniqueCalculateurServiceTest {

    @Mock private ConversionService conversionService;

    @InjectMocks private DetteTechniqueCalculateurService service;

    @Test
    void should_calculate_dette_and_evolution() {

        IndicateurQualiteView current = new IndicateurQualiteView();
        current.setDetteTechnique("10");

        IndicateurQualiteView histo = new IndicateurQualiteView();
        histo.setDetteTechnique("7");

        when(conversionService.convertDetteTechnique("10")).thenReturn("A");

        service.calculDetteTechnique(current, histo, Context.APPLICATION, null);

        assertThat(current.getLettreDetteTechnique()).isEqualTo("A");
        assertThat(current.getEvolutionDetteTechnique()).isEqualTo(-3.0);
        assertThat(current.getDetteTechniquePast()).isEqualTo("7");
    }

    @Test
    void should_show_negative_evolution_when_dette_increases() {

        IndicateurQualiteView current = new IndicateurQualiteView();
        current.setDetteTechnique("12");

        IndicateurQualiteView histo = new IndicateurQualiteView();
        histo.setDetteTechnique("10");

        when(conversionService.convertDetteTechnique("12")).thenReturn("B");

        service.calculDetteTechnique(current, histo, Context.APPLICATION, null);

        assertThat(current.getEvolutionDetteTechnique()).isEqualTo(-2.0);
    }

    @Test
    void should_set_SO_when_module_sans_objet() {

        IndicateurQualiteView current = new IndicateurQualiteView();

        Module module = new Module();
        module.setKeySonar("Sans objet");

        service.calculDetteTechnique(current, null, Context.MODULE, module);

        assertThat(current.getDetteTechnique()).isEqualTo(Notation.SO.getGrade());
        assertThat(current.getLettreDetteTechnique()).isEqualTo(Notation.SO.getGrade());
    }

    @Test
    void should_set_NR_when_module_not_sans_objet() {

        IndicateurQualiteView current = new IndicateurQualiteView();

        Module module = new Module();
        module.setKeySonar("OTHER");

        service.calculDetteTechnique(current, null, Context.MODULE, module);

        assertThat(current.getDetteTechnique()).isEqualTo(Notation.NR.getGrade());
        assertThat(current.getLettreDetteTechnique()).isEqualTo(Notation.NR.getGrade());
    }
}
