package fr.insee.compas.model.sonar;

import java.sql.Date;

import lombok.Data;

@Data
public class History {
    private Date date;
    private double value;
}
