package fr.insee.compas.service.qualite;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.view.IndicateurQualiteView;

@ExtendWith(MockitoExtension.class)
class FiabiliteCalculateurServiceTest {

    @InjectMocks private FiabiliteCalculateurService service;

    @Test
    void should_calculate_fiabilite_letter_and_evolution() {

        IndicateurQualiteView current = new IndicateurQualiteView();
        current.setFiabilite("3");

        IndicateurQualiteView histo = new IndicateurQualiteView();
        histo.setFiabilite("5");

        service.calculFiabilite(current, histo, Context.APPLICATION, null);

        assertThat(current.getLettreFiabilite()).isEqualTo("C");
        assertThat(current.getEvolutionFiabilite()).isEqualTo(2.0);
        assertThat(current.getFiabilitePast()).isEqualTo("E");
    }

    @Test
    void should_set_SO_when_module_and_sans_objet() {

        IndicateurQualiteView current = new IndicateurQualiteView();
        current.setFiabilite(null);

        fr.insee.compas.model.oscar.Module module = new fr.insee.compas.model.oscar.Module();
        module.setKeySonar("Sans objet");

        service.calculFiabilite(current, null, Context.MODULE, module);

        assertThat(current.getFiabilite()).isEqualTo(Notation.SO.getGrade());
        assertThat(current.getLettreFiabilite()).isEqualTo(Notation.SO.getGrade());
    }

    @Test
    void should_set_NR_when_module_and_not_sans_objet() {

        IndicateurQualiteView current = new IndicateurQualiteView();
        current.setFiabilite(null);

        fr.insee.compas.model.oscar.Module module = new fr.insee.compas.model.oscar.Module();
        module.setKeySonar("OTHER");

        service.calculFiabilite(current, null, Context.MODULE, module);

        assertThat(current.getFiabilite()).isEqualTo(Notation.NR.getGrade());
        assertThat(current.getLettreFiabilite()).isEqualTo(Notation.NR.getGrade());
    }

    @Test
    void should_convert_fiabilite_to_letter() {

        IndicateurQualiteView current = new IndicateurQualiteView();
        current.setFiabilite("1");

        service.calculFiabilite(current, null, Context.APPLICATION, null);

        assertThat(current.getLettreFiabilite()).isEqualTo("A");
    }

    @Test
    void should_convert_fiabilite_to_letter_C() {

        IndicateurQualiteView current = new IndicateurQualiteView();
        current.setFiabilite("3");

        service.calculFiabilite(current, null, Context.APPLICATION, null);

        assertThat(current.getLettreFiabilite()).isEqualTo("C");
    }

    @Test
    void should_calculate_evolution_only_when_histo_present() {

        IndicateurQualiteView current = new IndicateurQualiteView();
        current.setFiabilite("4");

        IndicateurQualiteView histo = new IndicateurQualiteView();
        histo.setFiabilite("6");

        service.calculFiabilite(current, histo, Context.APPLICATION, null);

        assertThat(current.getEvolutionFiabilite()).isEqualTo(2.0);
    }
}
