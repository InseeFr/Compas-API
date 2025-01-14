package fr.insee.compas.model.meteo;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Meteo {

    private Integer idApplication;
    private String appName;

    /** Service dev. */
    private String sndi;

    /** Domaine dev. */
    private String domaineSndi;

    private LocalDate date;
    private BigDecimal valeurMeteo;
}
