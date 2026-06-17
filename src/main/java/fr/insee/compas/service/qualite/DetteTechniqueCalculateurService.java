package fr.insee.compas.service.qualite;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.conversion.ConversionService;
import fr.insee.compas.view.IndicateurQualiteView;

@Service
public class DetteTechniqueCalculateurService {
    private static final String SANS_OBJET = "Sans objet";
    private final ConversionService conversionService;

    public DetteTechniqueCalculateurService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public void calculDetteTechnique(
            IndicateurQualiteView current,
            IndicateurQualiteView histo,
            Context context,
            Module module) {

        String dette = current.getDetteTechnique();

        if (StringUtils.isEmpty(dette)) {

            if (context == Context.MODULE) {

                if (module.getKeySonar() != null
                        && SANS_OBJET.equals(module.getKeySonar().trim())) {

                    current.setDetteTechnique(Notation.SO.getGrade());
                    current.setLettreDetteTechnique(Notation.SO.getGrade());

                } else {
                    current.setDetteTechnique(Notation.NR.getGrade());
                    current.setLettreDetteTechnique(Notation.NR.getGrade());
                }
            }

            return;
        }

        current.setLettreDetteTechnique(conversionService.convertDetteTechnique(dette));

        if (histo != null && StringUtils.isNotEmpty(histo.getDetteTechnique())) {

            double now = Double.parseDouble(dette);
            double histoValue = Double.parseDouble(histo.getDetteTechnique());

            current.setEvolutionDetteTechnique(histoValue - now);
            current.setDetteTechniquePast(String.valueOf((long) histoValue));
        }
    }
}
