package fr.insee.compas.model.sonar;

import java.util.List;

import lombok.Data;

@Data
public class Component {

    private String key;
    private String name;
    private String description;
    private String qualifier;
    private List<Measure> measures;
}
