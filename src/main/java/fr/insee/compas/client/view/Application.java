package fr.insee.compas.client.view;

import java.io.Serializable;

import lombok.Data;

@Data
public class Application implements Serializable {
    private Integer id;
    private String nomTechnique;
    private String nom;
    private String nomLong;
    private Sndi sndi;
    private DomaineSndi domaineSndi;
}
