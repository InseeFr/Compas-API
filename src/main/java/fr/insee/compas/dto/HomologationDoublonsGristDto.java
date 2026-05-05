package fr.insee.compas.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomologationDoublonsGristDto {
    private String nomApplication;
    private Integer nombreOccurrences;
    private List<String> listeSI;
}
