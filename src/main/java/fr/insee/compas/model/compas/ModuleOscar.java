package fr.insee.compas.model.compas;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ModuleOscar {

    @Id private int idModule;

    private boolean actif;
}
