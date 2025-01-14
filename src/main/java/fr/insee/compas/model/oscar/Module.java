package fr.insee.compas.model.oscar;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Module {
    Integer id;
    String modName;
    Integer idApplication;
    String appName;
    String domaineSndi;
    String domaineFonctionnel;
    String keySonar;
    String sndi;
    LocalDate dateDerniereLivraison;

    public Module(
            Integer id,
            String modName,
            Integer idApplication,
            String appName,
            String domaineSndi,
            String domaineFonctionnel,
            String keySonar,
            String sndi) {
        this.id = id;
        this.modName = modName;
        this.idApplication = idApplication;
        this.appName = appName;
        this.domaineSndi = domaineSndi;
        this.domaineFonctionnel = domaineFonctionnel;
        this.keySonar = keySonar;
        this.sndi = sndi;
    }

    public Module(
            Integer id, String modName, String domaineFonctionnel, String keySonar, String sndi) {
        this.id = id;
        this.modName = modName;
        this.domaineFonctionnel = domaineFonctionnel;
        this.keySonar = keySonar;
        this.sndi = sndi;
    }

    public Module(
            Integer id,
            String modName,
            String domaineFonctionnel,
            String keySonar,
            String sndi,
            LocalDate dateDerniereLivraison) {
        this.id = id;
        this.modName = modName;
        this.domaineFonctionnel = domaineFonctionnel;
        this.keySonar = keySonar;
        this.sndi = sndi;
        this.dateDerniereLivraison = dateDerniereLivraison;
    }

    public Module() {
        super();
    }
}
