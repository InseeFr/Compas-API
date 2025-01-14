package fr.insee.compas.model.compas;

import lombok.Data;

@Data
public class ModuleGradeDistance {

    private String name;
    private String appName;
    private String sndi;
    private String domaine;
    private String grade;

    public ModuleGradeDistance(
            String name, String appName, String sndi, String domaine, String grade) {
        this.name = name;
        this.appName = appName;
        this.sndi = sndi;
        this.domaine = domaine;
        this.grade = grade;
    }
}
