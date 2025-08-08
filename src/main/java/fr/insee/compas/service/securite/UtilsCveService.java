package fr.insee.compas.service.securite;

import java.util.*;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import fr.insee.compas.model.compas.IndicateurType;

@Service
public class UtilsCveService {

    public static final String CRITICAL = "CRITICAL";
    public static final String HIGH = "HIGH";
    public static final String MEDIUM = "MEDIUM";
    public static final String LOW = "LOW";

    public Integer getIndicateurApplication(String severity) {
        Integer retour = null;
        switch (severity) {
            case CRITICAL -> retour = IndicateurType.CVE_CRITICAL_APPLI.getValue();
            case HIGH -> retour = IndicateurType.CVE_HIGH_APPLI.getValue();
            case MEDIUM -> retour = IndicateurType.CVE_MEDIUM_APPLI.getValue();
            case LOW -> retour = IndicateurType.CVE_LOW_APPLI.getValue();
            default -> throw new IllegalStateException("Unexpected value: " + severity);
        }
        return retour;
    }

    public Integer getIndicateurModule(String severity) {
        Integer retour = null;
        switch (severity) {
            case CRITICAL -> retour = IndicateurType.CVE_CRITICAL.getValue();
            case HIGH -> retour = IndicateurType.CVE_HIGH.getValue();
            case MEDIUM -> retour = IndicateurType.CVE_MEDIUM.getValue();
            case LOW -> retour = IndicateurType.CVE_LOW.getValue();
            default -> throw new IllegalStateException("Unexpected value: " + severity);
        }
        return retour;
    }

    public Map<String, Set<String>> parseResults(JsonNode resultsNode) {
        Map<String, Set<String>> cves = new HashMap<>();
        cves.put(CRITICAL, new HashSet<>());
        cves.put(HIGH, new HashSet<>());
        cves.put(MEDIUM, new HashSet<>());
        cves.put(LOW, new HashSet<>());

        for (JsonNode result : resultsNode) {
            JsonNode vulnerabilitiesNode = result.get("Vulnerabilities");
            if (vulnerabilitiesNode != null && vulnerabilitiesNode.isArray()) {
                classifyVulnerabilities(vulnerabilitiesNode, cves);
            }
        }

        return cves;
    }

    public void classifyVulnerabilities(
            JsonNode vulnerabilitiesNode, Map<String, Set<String>> classifiedCves) {
        vulnerabilitiesNode.forEach(
                vulnerability -> {
                    String severity = vulnerability.get("Severity").asText();
                    String idCve = vulnerability.get("VulnerabilityID").asText();

                    classifiedCves.computeIfPresent(
                            severity,
                            (k, v) -> {
                                v.add(idCve);
                                return v;
                            });
                });
    }

    public Map<String, Set<String>> concatInventaireCve(
            Map<String, Set<String>> inventaireApplication,
            Map<String, Set<String>> inventaireModule) {
        Map<String, Set<String>> resultat = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : inventaireApplication.entrySet()) {
            String key = entry.getKey();
            Set<String> mergedSet = new HashSet<>(inventaireApplication.get(key));
            mergedSet.addAll(inventaireModule.getOrDefault(key, Collections.emptySet()));
            resultat.put(key, mergedSet);
        }
        return resultat;
    }
}
