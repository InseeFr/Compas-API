package fr.insee.compas.model.homologation;

import com.opencsv.bean.CsvBindByName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Homologation {

    @CsvBindByName(column = "Applications_Oscar")
    private String apsOscar;

    @CsvBindByName(column = "Sensibilite_SI")
    private String sensitivity;

    @CsvBindByName(column = "Statut_homologation")
    private String statutHomologation;

    @CsvBindByName(column = "Applications_homologuees_liste_partielle")
    private String homologationSI;

    @CsvBindByName(column = "Date_debut_homologation")
    private String homologationBeginDate;

    @CsvBindByName(column = "Date_fin_homologation")
    private String homologationEndDate;

    @CsvBindByName(column = "Remarques")
    private String homologationRemarks;
}
