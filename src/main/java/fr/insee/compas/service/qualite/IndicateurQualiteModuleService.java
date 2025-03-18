package fr.insee.compas.service.qualite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;
import fr.insee.compas.service.UtilsService;
import fr.insee.compas.view.IndicateurModuleQualiteView;

@Service
public class IndicateurQualiteModuleService {

    private final OscarService oscarService;

    private final TableFaitsService tableFaitsService;

    private final UtilsService utilsService;

    public IndicateurQualiteModuleService(
            OscarService oscarService,
            TableFaitsService tableFaitsService,
            UtilsService utilsService) {
        this.oscarService = oscarService;
        this.tableFaitsService = tableFaitsService;
        this.utilsService = utilsService;
    }

    /**
     * Extrait en base de données les indicateurs de types qualité dans des Map<IdModule,TableFait>
     * Calcul les indicateurs finaux à partir des indicateurs brute
     *
     * @return Liste des indicateurs avec les informations de filtre des modules pour affichages
     *     dans le table.
     */
    public List<IndicateurModuleQualiteView> getIndicateurNiveauModule() {

        // Récupérer les informations des modules depuis l'API
        List<Module> modules = oscarService.getModules();

        // Metrics au niveau module
        Map<Integer, IndicateurModuleQualiteView> mapQualite =
                tableFaitsService.getIndicateurQualite();

        List<IndicateurModuleQualiteView> resultat = new ArrayList<>();

        // Traiter chaque module
        for (Module module : modules) {
            IndicateurModuleQualiteView viewModule = mapQualite.get(module.getId());
            if (viewModule == null) {
                viewModule = new IndicateurModuleQualiteView();
            }
            viewModule.setModuleId(module.getId());
            viewModule.setApplicationName(module.getAppName());
            viewModule.setSndi(module.getSndi());
            viewModule.setDomaineSndi(module.getDomaineSndi());
            viewModule.setModuleName(module.getModName());

            calculIndicateurCouvertureTestUnitaire(module, viewModule);
            calculIndicateurCve(viewModule);
            calculIndicateurFiabilite(module, viewModule);
            calculIndicateurDetteTechnique(module, viewModule);
            resultat.add(viewModule);
        }

        return resultat;
    }

    private void calculIndicateurDetteTechnique(
            Module module, IndicateurModuleQualiteView viewModule) {
        if (StringUtils.isNotEmpty(viewModule.getDetteTechnique())) {
            viewModule.setLettreDetteTechnique(
                    utilsService.getLettreDetteTechnique(viewModule.getDetteTechnique()));
        } else {
            if ("Sans objet".equals(module.getKeySonar().trim())) {
                viewModule.setDetteTechnique(Notation.SO.getGrade());
                viewModule.setLettreDetteTechnique(Notation.SO.getGrade());

            } else {
                viewModule.setDetteTechnique(Notation.NR.getGrade());
                viewModule.setLettreDetteTechnique(Notation.NR.getGrade());
            }
        }
    }

    private void calculIndicateurFiabilite(Module module, IndicateurModuleQualiteView viewModule) {
        if (StringUtils.isNotEmpty(viewModule.getFiabilite())) {
            viewModule.setLettreFiabilite(
                    Character.toString(
                            (char) ('A' + Double.parseDouble(viewModule.getFiabilite()) - 1)));
        } else {
            if ("Sans objet".equals(module.getKeySonar().trim())) {
                viewModule.setFiabilite(Notation.SO.getGrade());
                viewModule.setLettreFiabilite(Notation.SO.getGrade());

            } else {
                viewModule.setFiabilite(Notation.NR.getGrade());
                viewModule.setLettreFiabilite(Notation.NR.getGrade());
            }
        }
    }

    private void calculIndicateurCve(IndicateurModuleQualiteView viewModule) {
        if (StringUtils.isNotEmpty(viewModule.getNbCveCritical())) {

            BigDecimal calcul =
                    utilsService.getCalculIndicateurCve(
                            BigDecimal.valueOf(Double.parseDouble(viewModule.getNbCveCritical())),
                            BigDecimal.valueOf(Double.parseDouble(viewModule.getNbCveCritical())),
                            BigDecimal.valueOf(Double.parseDouble(viewModule.getNbCveCritical())),
                            BigDecimal.valueOf(Double.parseDouble(viewModule.getNbCveCritical())));
            viewModule.setLettreNiveauCve(
                    utilsService.convertNiveauCveEnLettre(calcul.doubleValue()));
        }
    }

    private void calculIndicateurCouvertureTestUnitaire(
            Module module, IndicateurModuleQualiteView viewModule) {

        if (StringUtils.isNotEmpty(viewModule.getNbLigneCode())) {
            // Calculer le pourcentage
            int ligne = (int) Double.parseDouble(viewModule.getNbLigneCode());
            int ligneNonTeste = (int) Double.parseDouble(viewModule.getNbLigneCodeNonTeste());
            double percentage = utilsService.calculPourcentageCouvertureTest(ligne, ligneNonTeste);

            String pourcentage = (int) percentage + " %";

            // Obtenir la note
            String lettre = utilsService.convertPourcentageEnNote(percentage);

            // Ajouter le module avec le grade calculé
            viewModule.setPourcentageCouvertureTestUniaire(pourcentage);
            viewModule.setLettreCouvertureTestUniaire(lettre);

        } else {
            // Ajouter le module avec un grade par défaut
            if ("Sans objet".equals(module.getKeySonar().trim())) {
                viewModule.setPourcentageCouvertureTestUniaire("SO");
                viewModule.setLettreCouvertureTestUniaire("SO");

            } else {
                viewModule.setPourcentageCouvertureTestUniaire("NR");
                viewModule.setLettreCouvertureTestUniaire("NR");
            }
        }
    }
}
