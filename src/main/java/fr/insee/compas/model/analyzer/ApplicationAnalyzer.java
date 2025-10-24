package fr.insee.compas.model.analyzer;

import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationAnalyzer {

    private Integer id;
    private String nom;
    private String sndi;
    private String domaine;
    private List<ModuleAnalyzer> modules;
    private CveActivesAnalyzer cveActives;
}
