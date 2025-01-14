package fr.insee.compas.client.view;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sndi implements Serializable {

    private Integer id;
    private String nom;
}
