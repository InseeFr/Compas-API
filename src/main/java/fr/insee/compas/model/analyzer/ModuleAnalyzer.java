package fr.insee.compas.model.analyzer;

import java.util.Date;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleAnalyzer {

    private Integer id;
    private String nom;
    private String sndi;
    private String domaine;
    private Integer nombreCveCritique;
    private Integer nombreCveMajeur;
    private Integer nombreCveMoyenne;
    private Integer nombreCveFaible;
    private Date dateDernierScanCve;
}
