package fr.insee.compas.model.compas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.*;

import lombok.Data;

@Data
@Entity
public class ApplicationTip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_oscar", nullable = false, length = 128)
    private String nomOscar;

    // "date" est un mot réservé en SQL → on force le quoting
    @Column(name = "\"date\"", nullable = false)
    private LocalDate date;

    @Column(name = "source_id", nullable = false)
    private Short sourceId; // int2 → Short

    @Column(columnDefinition = "text", nullable = false)
    private String conseil;

    @Column(length = 32)
    private String priorite;

    @Column(length = 128)
    private String variable;

    @Column(length = 128)
    private String modalite;

    @Column(precision = 12, scale = 4)
    private BigDecimal contrib;

    @Column(length = 64)
    private String answer;

    @Column(name = "points_appli", precision = 12, scale = 4)
    private BigDecimal pointsAppli;

    @Column(precision = 12, scale = 4)
    private BigDecimal delta;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
