package fr.insee.compas.dto.maturite;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DemandeCreationStrategieCloudValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private DemandeCreationStrategieCloud buildDemandeValide() {
        DemandeCreationStrategieCloud demande = new DemandeCreationStrategieCloud();
        demande.setIdsModule(List.of(1));
        demande.setDate(LocalDate.now());
        demande.setAvancement(BigDecimal.ONE);
        demande.setEnvCibleProd(BigDecimal.ONE);
        return demande;
    }

    private Set<ConstraintViolation<DemandeCreationStrategieCloud>> validate(
            DemandeCreationStrategieCloud demande) {
        return validator.validate(demande);
    }

    private String getMessageForField(
            Set<ConstraintViolation<DemandeCreationStrategieCloud>> violations, String field) {
        return violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals(field))
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse(null);
    }

    // --- Cas valide ---

    @Test
    void shouldHaveNoViolations_whenDemandeValide() {
        assertTrue(validate(buildDemandeValide()).isEmpty());
    }

    // --- idsModule ---

    @Test
    void shouldHaveViolation_whenIdsModuleNull() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setIdsModule(null);
        assertEquals(
                "La liste des Ids de modules ne peut pas être vide",
                getMessageForField(validate(demande), "idsModule"));
    }

    @Test
    void shouldHaveViolation_whenIdsModuleEmpty() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setIdsModule(List.of());
        assertEquals(
                "La liste des Ids de modules ne peut pas être vide",
                getMessageForField(validate(demande), "idsModule"));
    }

    // --- date ---

    @Test
    void shouldHaveViolation_whenDateNull() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setDate(null);
        assertEquals(
                "La date ne peut pas être nulle", getMessageForField(validate(demande), "date"));
    }

    // --- avancement ---

    @Test
    void shouldHaveViolation_whenAvancementNull() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setAvancement(null);
        assertEquals(
                "L'avancement ne peut pas être nul",
                getMessageForField(validate(demande), "avancement"));
    }

    @Test
    void shouldHaveViolation_whenAvancementInferieurA1() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setAvancement(BigDecimal.ZERO);
        assertFalse(validate(demande).isEmpty());
        assertTrue(
                getMessageForField(validate(demande), "avancement")
                        .contains("L'avancement doit être compris entre 1 et 3"));
    }

    @Test
    void shouldHaveViolation_whenAvancementSuperieurA3() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setAvancement(BigDecimal.valueOf(4));
        assertFalse(validate(demande).isEmpty());
        assertTrue(
                getMessageForField(validate(demande), "avancement")
                        .contains("L'avancement doit être compris entre 1 et 3"));
    }

    @Test
    void shouldHaveNoViolation_whenAvancementEgalA1() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setAvancement(BigDecimal.ONE);
        assertTrue(validate(demande).isEmpty());
    }

    @Test
    void shouldHaveNoViolation_whenAvancementEgalA3() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setAvancement(BigDecimal.valueOf(3));
        assertTrue(validate(demande).isEmpty());
    }

    // --- envCibleProd ---

    @Test
    void shouldHaveViolation_whenEnvCibleProdNull() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setEnvCibleProd(null);
        assertEquals(
                "L'environnement cible ne peut pas être nul",
                getMessageForField(validate(demande), "envCibleProd"));
    }

    // --- Règle cross-champs (@AssertTrue) ---

    @Test
    void shouldHaveViolation_whenEnvCibleZeroEtAvancement3() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setEnvCibleProd(BigDecimal.ZERO);
        demande.setAvancement(BigDecimal.valueOf(3));

        Set<ConstraintViolation<DemandeCreationStrategieCloud>> violations = validate(demande);

        // Debug : affiche tous les champs et messages
        violations.forEach(
                v ->
                        System.out.println(
                                "Field: " + v.getPropertyPath() + " | Message: " + v.getMessage()));

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldHaveNoViolation_whenEnvCibleZeroEtAvancementDifferentDe3() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setEnvCibleProd(BigDecimal.ZERO);
        demande.setAvancement(BigDecimal.ONE); // pas validée, OK
        assertTrue(validate(demande).isEmpty());
    }
}
