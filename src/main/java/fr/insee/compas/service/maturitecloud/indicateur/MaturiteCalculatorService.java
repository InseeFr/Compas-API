package fr.insee.compas.service.maturitecloud.indicateur;

import static fr.insee.compas.util.MaturiteConstantes.SANS_OBJET;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.compas.service.conversion.ConversionService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MaturiteCalculatorService {

    private ConversionService conversionService;

    public String getEnvActuelProd(String envActuelProd, String zoneDeProduction) {
        if (envActuelProd == null) return SANS_OBJET;
        if (envActuelProd.equals("Saisie manuelle"))
            return conversionService.convertMaturiteZoneDeProd(zoneDeProduction);
        if (envActuelProd.equals("Puppet")) return "VM";
        return envActuelProd;
    }

    public String getAllCommentaires(List<String> commentaires) {
        return commentaires.stream()
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .collect(Collectors.joining("; "));
    }

    public String getEnvApp(List<String> envAppList) {
        return envAppList.stream()
                .filter(e -> e != null && !e.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    public boolean hasOneEcart(List<String> ecartCibleList) {
        return ecartCibleList.stream().anyMatch(e -> e.equals("oui"));
    }

    public String getStratCloud(List<String> stratCloudList) {
        boolean hasEnCours = false;
        boolean allValide = true;

        for (String s : stratCloudList) {
            if (s.equals("En cours")) hasEnCours = true;
            if (!s.equals("Validée")) allValide = false;
        }

        if (stratCloudList.isEmpty()) return "A instruire";
        if (hasEnCours) return "En cours";
        if (allValide) return "Validée";
        return "A instruire";
    }

    public String calculateTauxCloudProd(List<String> envActuelList) {
        if (envActuelList.isEmpty()) return "0%";
        int size = envActuelList.size();
        long cloudCount =
                envActuelList.stream()
                        .filter(e -> e.equals("Kube") || e.equals("Cloud Externe"))
                        .count();
        int taux = (int) Math.round((cloudCount * 100.0) / size);
        return taux + "%";
    }

    public String calculateTauxCloudProdModule(String envActuel) {
        return envActuel.equals("Kube") || envActuel.equals("Cloud Externe") ? "100%" : "0%";
    }

    protected String calculateMaturiteCloud(Integer valeur) {
        return conversionService.convertMaturiteCloud(valeur);
    }

    public String calculateEnvCible(Integer valeur) {
        return conversionService.convertMaturiteEnvCible(valeur);
    }

    public String calculateStrategieCLoud(Integer valeur) {
        return conversionService.convertStrategieCloud(valeur);
    }
}
