package fr.insee.compas.model.compas;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;

import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class TableFaits {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_module")
    private Integer idModule;

    @Column(name = "id_application")
    private Integer idApplication;

    @Column(name = "id_indicateur")
    private Integer idIndicateur;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "valeur")
    private BigDecimal valeur;

    @Column(name = "id_source")
    private Integer idSource;

    @Column(name = "commentaire")
    private String commentaire;

    public TableFaits(
            Integer idModule,
            Integer idApplication,
            Integer idIndicateur,
            LocalDate date,
            BigDecimal valeur,
            Integer idSource,
            String commentaire) {
        this.idModule = idModule;
        this.idApplication = idApplication;
        this.idIndicateur = idIndicateur;
        this.date = date;
        this.valeur = valeur;
        this.idSource = idSource;
        this.commentaire = commentaire;
    }

    public TableFaits(
            Integer idModule,
            Integer idIndicateur,
            LocalDate date,
            BigDecimal valeur,
            Integer idSource) {
        this.idModule = idModule;
        this.idIndicateur = idIndicateur;
        this.date = date;
        this.valeur = valeur;
        this.idSource = idSource;
    }
}
