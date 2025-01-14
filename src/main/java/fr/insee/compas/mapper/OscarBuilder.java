package fr.insee.compas.mapper;

import java.time.Instant;
import java.time.ZoneId;

import com.fasterxml.jackson.databind.JsonNode;

import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;

public class OscarBuilder {

    public Module buildModule(JsonNode moduleNode) {
        Module module = new fr.insee.compas.model.oscar.Module();
        module.setId(moduleNode.path("id").asInt());

        module.setModName(moduleNode.path("nom").asText());
        module.setModName(
                moduleNode.path("nom").asText()); // Assuming modName and appName are the same
        module.setAppName(
                moduleNode.path("nom").asText()); // Assuming modName and appName are the same
        JsonNode dateDerniereLivraisonNode = moduleNode.path("dateDerniereLivraison");

        if (!dateDerniereLivraisonNode.isMissingNode() && !dateDerniereLivraisonNode.isNull()) {
            module.setDateDerniereLivraison(
                    Instant.ofEpochMilli(dateDerniereLivraisonNode.asLong())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
        } else {
            module.setDateDerniereLivraison(null);
        }
        module.setKeySonar(moduleNode.path("projectKeySonar").asText());
        module.setIdApplication(getPathApplication(moduleNode).path("id").asInt());
        module.setSndi(getPathApplication(moduleNode).path("sndi").path("nom").asText());
        module.setDomaineSndi(
                getPathApplication(moduleNode).path("domaineSndi").path("nom").asText());
        module.setDomaineFonctionnel(
                getPathApplication(moduleNode).path("domaineFonctionnel").path("nom").asText());
        return module;
    }

    public Application buildApplication(JsonNode applicationNoeud) {
        Application application = new Application();
        application.setIdApplication(applicationNoeud.path("id").asInt());
        application.setAppName(applicationNoeud.path("nom").asText());
        application.setSndi(applicationNoeud.path("sndi").path("nom").asText());
        application.setDomaineFonctionnel(
                applicationNoeud.path("domaineFonctionnel").path("nom").asText());
        application.setDomaineSndi(applicationNoeud.path("domaineSndi").path("nom").asText());

        return application;
    }

    private static JsonNode getPathApplication(JsonNode noeud) {
        return noeud.path("applicationTechnique").path("application");
    }
}
