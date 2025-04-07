package fr.insee.compas.model.oscar;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class Module {
    Integer id;
    String sourceCreation;
    String modName;
    Integer idApplication;
    String appName;
    String domaineSndi;
    String domaineFonctionnel;
    String keySonar;
    String sndi;
    String statut;
    LocalDate dateDerniereLivraisonEnProduction;
    String typeLivrable;

    public Module() {
        super();
    }
}
