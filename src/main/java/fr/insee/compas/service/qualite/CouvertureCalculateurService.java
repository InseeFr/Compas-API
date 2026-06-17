package fr.insee.compas.service.qualite;

import org.springframework.stereotype.Service;

import fr.insee.compas.mapper.IndicateurQualiteViewMapper;
import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.UtilsService;
import fr.insee.compas.service.conversion.ConversionService;
import fr.insee.compas.view.IndicateurQualiteView;

@Service
public class CouvertureCalculateurService {

    private final UtilsService utilsService;
    private final ConversionService conversionService;
    private final IndicateurQualiteViewMapper mapper;

    private static final String SANS_OBJET = "Sans objet";

    public CouvertureCalculateurService(
            UtilsService utilsService,
            ConversionService conversionService,
            IndicateurQualiteViewMapper mapper) {
        this.utilsService = utilsService;
        this.conversionService = conversionService;
        this.mapper = mapper;
    }

    public void calculCouvertureEtEvolution(
            IndicateurQualiteView current,
            IndicateurQualiteView histo,
            Context context,
            Module module) {

        double percentage;

        Double lignes = parseDoubleSafe(current.getNbLigneCode());

        if (lignes == null) {
            appliquerNR(current, context, module);
            return;
        }

        if (lignes <= 0) {
            appliquerSO(current, context);
            return;
        }
        percentage =
                calculCouverture(
                        current.getNbLigneCode(), defaultIfEmpty(current.getNbLigneCodeNonTeste()));

        appliquerCouverture(current, percentage);

        if (histo != null) {

            Double lignesHisto = parseDoubleSafe(histo.getNbLigneCode());

            if (lignesHisto != null && lignesHisto > 0) {

                double percentageHisto =
                        calculCouverture(histo.getNbLigneCode(), histo.getNbLigneCodeNonTeste());

                current.setEvolutionCouvertureTestUnitaire(percentage - percentageHisto);
                current.setPourcentageCouvertureTestUnitairePast((int) percentageHisto + " %");
            }
        }
    }

    private void appliquerNR(IndicateurQualiteView view, Context context, Module module) {
        if (context.equals(Context.APPLICATION)) {
            mapper.applyGlobal(view, Notation.NR);
        } else {
            if (module != null
                    && module.getKeySonar() != null
                    && SANS_OBJET.equals(module.getKeySonar().trim())) {

                mapper.applyCouvertureOnly(view, Notation.SO);

            } else {
                mapper.applyCouvertureOnly(view, Notation.NR);
            }
        }
    }

    private void appliquerSO(IndicateurQualiteView view, Context context) {

        if (context.equals(Context.APPLICATION)) {
            mapper.applyGlobal(view, Notation.SO);
        } else {
            mapper.applyCouvertureOnly(view, Notation.SO);
        }
    }

    private void appliquerCouverture(IndicateurQualiteView view, double percentage) {

        String pourcentage = (int) percentage + " %";
        String lettre = conversionService.convertPourcentageEnNote(percentage);

        view.setPourcentageCouvertureTestUnitaire(pourcentage);
        view.setLettreCouvertureTestUnitaire(lettre);
    }

    private double calculCouverture(String nbLignes, String nbNonTeste) {

        Double lignes = parseDoubleSafe(nbLignes);
        Double nonTeste = parseDoubleSafe(nbNonTeste);
        if (lignes == null || nonTeste == null) {
            return 0.0;
        }
        return utilsService.calculPourcentageCouvertureTest(lignes.intValue(), nonTeste.intValue());
    }

    private Double parseDoubleSafe(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Double.parseDouble(value);
    }

    private String defaultIfEmpty(String value) {
        return (value == null || value.isEmpty()) ? "0" : value;
    }
}
