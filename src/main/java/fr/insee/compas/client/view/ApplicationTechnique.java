package fr.insee.compas.client.view;

import java.io.Serializable;

import lombok.Data;

@Data
public class ApplicationTechnique implements Serializable {
    private Integer id;
    private String nom;
    private Application application;
}
