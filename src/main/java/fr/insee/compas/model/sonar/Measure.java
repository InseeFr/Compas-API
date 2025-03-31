package fr.insee.compas.model.sonar;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Measure {

    private String metric;
    private String value;
}
