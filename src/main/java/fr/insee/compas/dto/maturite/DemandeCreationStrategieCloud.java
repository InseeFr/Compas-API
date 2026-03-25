package fr.insee.compas.dto.maturite;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO pour la saisie de la stratégie cloud */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeCreationStrategieCloud {

    @NotEmpty(message = "La liste des Ids de modules ne peut pas être vide")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Integer> idsModule;

    /** Avancement de la stratégie cloud 1 => A instruire 2 => En cours 3 => Validée */
    @NotNull(message = "L'avancement ne peut pas être nul")
    @DecimalMin(value = "1", message = "L'avancement doit être compris entre 1 et 3")
    @DecimalMax(value = "3", message = "L'avancement doit être compris entre 1 et 3")
    private BigDecimal avancement;

    /** Commentaire facultatif */
    private String commentaire;

    @NotNull(message = "L'environnement cible ne peut pas être nul")
    private BigDecimal envCibleProd;

    @NotNull(message = "La date ne peut pas être nulle")
    private LocalDate date;

    @JsonIgnore
    @SuppressWarnings("unused")
    @AssertTrue(
            message =
                    "Pour une stratégie validée, vous devez sélectionner un environnement cible de"
                            + " production autre que « Non renseigné »")
    public boolean isEnvCibleValide() {
        if (avancement == null || envCibleProd == null) return true;
        return !(envCibleProd.compareTo(BigDecimal.ZERO) == 0
                && avancement.compareTo(BigDecimal.valueOf(3)) == 0);
    }
}
