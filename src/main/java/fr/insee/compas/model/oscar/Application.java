package fr.insee.compas.model.oscar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    Integer idApplication;
    String appName;
    String domaineSndi;
    String domaineFonctionnel;
    String sndi;

    public Application(Integer idApplication, String appName) {}
}
