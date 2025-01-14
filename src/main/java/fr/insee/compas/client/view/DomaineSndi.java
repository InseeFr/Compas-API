package fr.insee.compas.client.view;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

@Data
public class DomaineSndi implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private Integer id;
    private String nom;
}
