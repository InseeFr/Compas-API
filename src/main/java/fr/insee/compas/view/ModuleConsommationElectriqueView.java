package fr.insee.compas.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleConsommationElectriqueView {
    private Integer moduleId;
    private String date;
    private String conso;
    private String lettreGreen;
}
