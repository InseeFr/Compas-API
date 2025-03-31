package fr.insee.compas.model.sonar;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Component {

    private String key;
    private String name;
    private String description;
    private String qualifier;
    private List<Measure> measures;
}
