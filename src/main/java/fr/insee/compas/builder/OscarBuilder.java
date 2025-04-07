package fr.insee.compas.builder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.model.oscar.ModuleHistorique;

@Component
public class OscarBuilder {

    public ModuleHistorique buildModuleHistorique(JsonNode moduleHistoriqueNode) {

        ModuleHistorique moduleHistorique = new ModuleHistorique();
        moduleHistorique.setIdModuleHistorique(
                moduleHistoriqueNode.path("idModuleHistorique").asInt());
        moduleHistorique.setAuteurOperation(moduleHistoriqueNode.path("auteurOperation").asText());
        moduleHistorique.setIdModule(moduleHistoriqueNode.path("idModule").asInt());
        moduleHistorique.setStatut(moduleHistoriqueNode.path("statut").asText());
        JsonNode dateNode = moduleHistoriqueNode.path("dateOperation");
        if (dateNode.isArray() && dateNode.size() >= 6) {
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
        }
        moduleHistorique.setOperation(moduleHistoriqueNode.path("operation").asText());

        return moduleHistorique;
    }

    public Module buildModule(JsonNode moduleNode) {
        Module module = new fr.insee.compas.model.oscar.Module();
        module.setId(moduleNode.path("id").asInt());
        module.setSourceCreation(moduleNode.path("sourceCreation").asText());

        module.setModName(moduleNode.path("nom").asText());
        module.setStatut(moduleNode.path("statut").asText());
        module.setAppName(
                getPathApplication(moduleNode)
                        .path("nom")
                        .asText()); // Assuming modName and appName are the same
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
        module.setKeySonar(moduleNode.path("projectKeySonar").asText());
        module.setIdApplication(getPathApplication(moduleNode).path("id").asInt());
        module.setSndi(getPathApplication(moduleNode).path("sndi").path("nom").asText());
        module.setDomaineSndi(
                getPathApplication(moduleNode).path("domaineSndi").path("nom").asText());
        module.setDomaineFonctionnel(
                getPathApplication(moduleNode).path("domaineFonctionnel").path("nom").asText());
        module.setTypeLivrable((moduleNode).path("typeLivrable").asText());
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
