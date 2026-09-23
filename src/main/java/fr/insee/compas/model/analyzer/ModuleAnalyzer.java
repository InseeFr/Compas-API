package fr.insee.compas.model.analyzer;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleAnalyzer {

    private Integer id;
    private String nom;
    private String sndi;
    private String domaine;
    private ComptageCve comptageCve;
    private Date dateMajAnalyseTrivyCodeSource;
}
