package fr.insee.compas.model.compas;

import lombok.Data;

@Data
public class ModuleGrade {

    private String name;
    private String appName;
    private String sndi;
    private String domaine;
    private String grade;
    private String pourcentage;

    public ModuleGrade(
            String name,
            String appName,
            String sndi,
            String domaine,
            String grade,
            String pourcentage) {
        this.name = name;
        this.appName = appName;
        this.sndi = sndi;
        this.domaine = domaine;
        this.grade = grade;
        this.pourcentage = pourcentage;
    }
}
