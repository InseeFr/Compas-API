package fr.insee.compas.view;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "CveCriticalMonthlyView")
public class IndicateurApplicationSecuriteMonthly {

    @Schema(example = "95")
    private Integer applicationId;

    @Schema(description = "Premier jour du mois mesuré", example = "2025-09-01")
    private LocalDate month;

    @Schema(description = "Nombre de CVE critiques", example = "3")
    private Integer nbCveCritical;
}
