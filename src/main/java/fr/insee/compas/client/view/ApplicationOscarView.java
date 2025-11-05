package fr.insee.compas.client.view;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApplicationOscarView implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String nomTechnique;
    private String nom;
    private String description;
    private LocalDate dateDerniereLivraisonEnProduction;
    private String rga;
    private String balfMetier;
}
