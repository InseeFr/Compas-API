package fr.insee.compas.model.hyperx;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicateurRecuperationSecuriteVM {
    int max;
    int nb;
}
