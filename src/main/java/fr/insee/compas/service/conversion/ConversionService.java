package fr.insee.compas.service.conversion;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.insee.compas.service.conversion.strategie.IConversionStrategie;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConversionService {
    private final IConversionStrategie<String, Double> pourcentageConversion;
    private final IConversionStrategie<String, Double> niveauCveConversion;
    private final IConversionStrategie<String, String> detteConversion;
    private final IConversionStrategie<String, String> issueConversion;
    private final IConversionStrategie<String, Double> vmConversion;
    private final IConversionStrategie<String, Integer> strategieCloudConversion;
    private final IConversionStrategie<String, Integer> maturiteEnvCibleConversion;
    private final IConversionStrategie<String, Integer> maturiteCloudConversion;
    private final IConversionStrategie<String, String> maturiteZoneDeProdConversion;

    public ConversionService(
            @Qualifier("pourcentageEnNoteConversion")
                    IConversionStrategie<String, Double> pourcentageConversion,
            @Qualifier("cveConversion") IConversionStrategie<String, Double> niveauCveConversion,
            @Qualifier("detteTechniqueConversion")
                    IConversionStrategie<String, String> detteConversion,
            @Qualifier("issueConversion") IConversionStrategie<String, String> issueConversion,
            @Qualifier("vmConversion") IConversionStrategie<String, Double> vmConversion,
            @Qualifier("strategieCloudConversion")
                    IConversionStrategie<String, Integer> strategieCloudConversion,
            @Qualifier("maturiteEnvCibleConversion")
                    IConversionStrategie<String, Integer> maturiteEnvCibleConversion,
            @Qualifier("maturiteCloudConversion")
                    IConversionStrategie<String, Integer> maturiteCloudConversion,
            @Qualifier("maturiteZoneDeProdConversion")
                    IConversionStrategie<String, String> maturiteZoneDeProdConversion) {
        this.pourcentageConversion = pourcentageConversion;
        this.niveauCveConversion = niveauCveConversion;
        this.detteConversion = detteConversion;
        this.issueConversion = issueConversion;
        this.vmConversion = vmConversion;
        this.strategieCloudConversion = strategieCloudConversion;
        this.maturiteEnvCibleConversion = maturiteEnvCibleConversion;
        this.maturiteCloudConversion = maturiteCloudConversion;
        this.maturiteZoneDeProdConversion = maturiteZoneDeProdConversion;
    }

    public String convertPourcentageEnNote(double percentage) {
        log.debug("Conversion de la valeur pour pourcentage En note : {}", percentage);
        return pourcentageConversion.conversion(percentage);
    }

    public String convertNiveauCveEnLettre(double niveau) {
        log.debug("Conversion de la valeur pour cve en Lettre : {}", niveau);
        return niveauCveConversion.conversion(niveau);
    }

    public String convertDetteTechnique(String fiabilite) {
        log.debug("Conversion de la valeur pour dette Technique : {}", fiabilite);
        return detteConversion.conversion(fiabilite);
    }

    public String convertIssueAccessebilite(String nbIssues) {
        log.debug("Conversion de la valeur pour Issues accessibilité : {}", nbIssues);
        return issueConversion.conversion(nbIssues);
    }

    public String convertNbVmNonMiseAJour(double niveau) {
        log.debug("Conversion de la valeur pour NbVM : {}", niveau);
        return vmConversion.conversion(niveau);
    }

    public String convertStrategieCloud(Integer strategie) {
        log.debug("Conversion de la valeur pour stratégie : {}", strategie);
        return strategieCloudConversion.conversion(strategie);
    }

    public String convertMaturiteEnvCible(Integer maturite) {
        log.debug("Conversion de la valeur pour envCible : {}", maturite);
        return maturiteEnvCibleConversion.conversion(maturite);
    }

    public String convertMaturiteCloud(Integer maturite) {
        log.debug("Conversion de la valeur pour maturité : {}", maturite);
        return maturiteCloudConversion.conversion(maturite);
    }

    public String convertMaturiteZoneDeProd(String maturite) {
        log.debug("Conversion de la valeur pour zone de prod : {}", maturite);
        return maturiteZoneDeProdConversion.conversion(maturite);
    }
}
