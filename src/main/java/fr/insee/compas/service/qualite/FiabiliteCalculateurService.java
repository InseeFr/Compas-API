package fr.insee.compas.service.qualite;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.view.IndicateurQualiteView;

@Service
public class FiabiliteCalculateurService {

    private static final String SANS_OBJET = "Sans objet";

    public void calculFiabilite(
            IndicateurQualiteView current,
            IndicateurQualiteView histo,
            Context context,
            Module module) {

        String fiabilite = current.getFiabilite();

        if (StringUtils.isEmpty(fiabilite)) {

            if (context == Context.MODULE) {

                if (module.getKeySonar() != null
                        && SANS_OBJET.equals(module.getKeySonar().trim())) {

                    current.setFiabilite(Notation.SO.getGrade());
                    current.setLettreFiabilite(Notation.SO.getGrade());

                } else {
                    current.setFiabilite(Notation.NR.getGrade());
                    current.setLettreFiabilite(Notation.NR.getGrade());
                }
            }

            return;
        }

        double value = Double.parseDouble(fiabilite);

        current.setLettreFiabilite(Character.toString((char) ('A' + value - 1)));

        if (histo != null && StringUtils.isNotEmpty(histo.getFiabilite())) {
            double past = Double.parseDouble(histo.getFiabilite());
            current.setEvolutionFiabilite(past - value);
            current.setFiabilitePast(Character.toString((char) ('A' + past - 1)));
        }
    }
}
