package fr.insee.compas.mapper;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.view.IndicateurQualiteView;

@Component
public class IndicateurQualiteViewMapper {

    public void applyGlobal(IndicateurQualiteView view, Notation notation) {
        view.setPourcentageCouvertureTestUnitaire("");
        view.setLettreCouvertureTestUnitaire(notation.name());
        view.setLettreDetteTechnique(notation.name());
        view.setLettreFiabilite(notation.name());
    }

    public void applyCouvertureOnly(IndicateurQualiteView view, Notation notation) {
        view.setPourcentageCouvertureTestUnitaire(notation.name());
        view.setLettreCouvertureTestUnitaire(notation.name());
    }
}
