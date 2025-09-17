package fr.insee.compas.view;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ApplicationTipView")
public class ApplicationTipView {
    private String nomOscar;
    private String date;
    private Short sourceId; // 1=tech, 2=orga
    private String conseil;
    private String priorite;
    private String variable;
    private String modalite;
    private String contrib;
    private String answer;
    private String pointsAppli;
    private String delta;
}
