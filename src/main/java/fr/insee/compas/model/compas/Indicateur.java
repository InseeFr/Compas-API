package fr.insee.compas.model.compas;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Indicateur {

    @Id private int id;
    private String nom;
    private String type;
    private String brutFinal;
}
