package fr.insee.compas.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationConsommationElectriqueView {
    private Integer applicationId;
    private String date;
    private String conso;
    private String lettreGreen;
}
