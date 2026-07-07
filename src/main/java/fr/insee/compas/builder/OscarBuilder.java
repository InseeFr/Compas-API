package fr.insee.compas.builder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import fr.insee.compas.client.view.ApplicationTechnique;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;

import tools.jackson.databind.JsonNode;
//OscarBuilder.java
@Component
public class OscarBuilder {

    public ModuleHistorique buildModuleHistorique(JsonNode moduleHistoriqueNode) {

        ModuleHistorique moduleHistorique = new ModuleHistorique();
        moduleHistorique.setIdModuleHistorique(
                moduleHistoriqueNode.path("idModuleHistorique").asInt());
        moduleHistorique.setAuteurOperation(
                moduleHistoriqueNode.path("auteurOperation").asString(null));
        moduleHistorique.setIdModule(moduleHistoriqueNode.path("idModule").asInt());
        moduleHistorique.setStatut(moduleHistoriqueNode.path("statut").asString(null));
        JsonNode dateNode = moduleHistoriqueNode.path("dateOperation");
        if (dateNode.isArray() && dateNode.size() >= 6) {
            // Cas ancien format : tableau [year, month, day, hour, minute, second, nanos]
            LocalDateTime dateOperation =
                    LocalDateTime.of(
                            dateNode.get(0).asInt(),
                            dateNode.get(1).asInt(),
                            dateNode.get(2).asInt(),
                            dateNode.get(3).asInt(),
                            dateNode.get(4).asInt(),
                            dateNode.get(5).asInt(),
                            dateNode.size() > 6 ? dateNode.get(6).asInt() : 0);
            moduleHistorique.setDateOperation(dateOperation);

        } else if (dateNode.isString()) {
            // Cas ISO 8601 en String : "2025-02-25T16:41:38.994458"
            LocalDateTime dateOperation = LocalDateTime.parse(dateNode.asString(null));
            moduleHistorique.setDateOperation(dateOperation);
        }
        moduleHistorique.setOperation(moduleHistoriqueNode.path("operation").asString(null));

        return moduleHistorique;
    }

    public Module buildModule(JsonNode moduleNode) {
        Module module = new fr.insee.compas.model.oscar.Module();
        module.setNomTechnique(moduleNode.path("nomTechnique").asString(null));
        module.setApplicationTechnique(
                moduleNode.path("applicationTechnique").path("nom").asString(null));
        module.setId(moduleNode.path("id").asInt());
        module.setSourceCreation(moduleNode.path("sourceCreation").asString(null));
        module.setUrlCodeSource(moduleNode.path("urlCodeSource").asString(null));
        module.setModName(moduleNode.path("nom").asString(null));
        module.setStatut(moduleNode.path("statut").asString(null));
        module.setAppName(
                getPathApplication(moduleNode)
                        .path("nom")
                        .asString(null)); // Assuming modName and appName are the same
        JsonNode dateDerniereLivraisonEnProductionNode =
                moduleNode.path("dateDerniereLivraisonEnProduction");

        if (!dateDerniereLivraisonEnProductionNode.isMissingNode()
                && !dateDerniereLivraisonEnProductionNode.isNull()) {
            module.setDateDerniereLivraisonEnProduction(
                    Instant.ofEpochMilli(dateDerniereLivraisonEnProductionNode.asLong())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
        } else {
            module.setDateDerniereLivraisonEnProduction(null);
        }
        module.setKeySonar(moduleNode.path("projectKeySonar").asString(null));
        module.setIdApplication(getPathApplication(moduleNode).path("id").asInt());
        module.setSndi(getPathApplication(moduleNode).path("sndi").path("nom").asString(null));
        module.setDomaineSndi(
                getPathApplication(moduleNode).path("domaineSndi").path("nom").asString(null));
        module.setDomaineFonctionnel(
                getPathApplication(moduleNode)
                        .path("domaineFonctionnel")
                        .path("nom")
                        .asString(null));
        module.setTypeLivrable((moduleNode).path("typeLivrable").asString(null));
        module.setZoneProduction(moduleNode.path("zoneProduction").asString(null));
        return module;
    }

    public Application buildApplication(JsonNode applicationNoeud) {
        Application application = new Application();
        application.setIdApplication(applicationNoeud.path("id").asInt());
        application.setAppName(applicationNoeud.path("nom").asString(null));
        application.setSndi(applicationNoeud.path("sndi").path("nom").asString(null));
        application.setDomaineFonctionnel(
                applicationNoeud.path("domaineFonctionnel").path("nom").asString(null));
        application.setDomaineSndi(applicationNoeud.path("domaineSndi").path("nom").asString(null));

        return application;
    }

    private static JsonNode getPathApplication(JsonNode noeud) {
        return noeud.path("applicationTechnique").path("application");
    }

    public ApplicationTechnique buildApplicationTechnique(JsonNode noeud) {
        ApplicationTechnique appTechnique = new ApplicationTechnique();
        appTechnique.setId(noeud.path("id").asInt());
        appTechnique.setNom(noeud.path("nom").asText(null));
        return appTechnique;
    }
}
