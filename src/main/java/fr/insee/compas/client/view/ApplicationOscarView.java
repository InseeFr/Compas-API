package fr.insee.compas.client.view;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationOscarView implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String nomTechnique;
    private String nom;
    private String description;
    private LocalDate dateDerniereLivraisonEnProduction;
    private String rga;
    private String balfMetier;
    private List<ApplicationNomAlternatif> applicationNomAlternatifs;

    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationNomAlternatif implements Serializable {
        private static final long serialVersionUID = 2405172041950251807L;
        private Integer idApplication;
        private String nomAlternatif;
        private String source;
    }
}
