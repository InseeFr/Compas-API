package fr.insee.compas.model.oscar;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleHistorique {
    Integer idModuleHistorique;
    Integer idModule;
    String auteurOperation;
    LocalDateTime dateOperation;
    String operation;
    String statut;
}
