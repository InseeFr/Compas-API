package fr.insee.compas.util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.insee.compas.dto.DemandeCreationStrategieCloud;

class UtilsDemandeTest {

    private DemandeCreationStrategieCloud buildDemandeValide() {
        DemandeCreationStrategieCloud demande = new DemandeCreationStrategieCloud();
        demande.setIdsModule(List.of(1, 2, 3));
        demande.setDate(LocalDate.now());
        demande.setAvancement(BigDecimal.valueOf(2));
        demande.setEnvCibleProd(BigDecimal.valueOf(3));
        return demande;
    }

    @Test
    void shouldNotThrow_whenDemandeValide() {
        assertDoesNotThrow(() -> UtilsDemande.validateDemande(buildDemandeValide()));
    }

    @Test
    void shouldThrow_whenDemandeNull() {
        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class, () -> UtilsDemande.validateDemande(null));
        assertEquals("La demande ne peut pas être nulle", ex.getMessage());
    }

    @Test
    void shouldThrow_whenIdsModuleNull() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setIdsModule(null);
        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UtilsDemande.validateDemande(demande));
        assertEquals("La liste des IDs de modules ne peut pas être vide", ex.getMessage());
    }

    @Test
    void shouldThrow_whenIdsModuleEmpty() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setIdsModule(List.of());
        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UtilsDemande.validateDemande(demande));
        assertEquals("La liste des IDs de modules ne peut pas être vide", ex.getMessage());
    }

    @Test
    void shouldThrow_whenDateNull() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setDate(null);
        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UtilsDemande.validateDemande(demande));
        assertEquals("La date ne peut pas être nulle", ex.getMessage());
    }

    @Test
    void shouldThrow_whenAvancementNull() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setAvancement(null);
        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UtilsDemande.validateDemande(demande));
        assertEquals("L'avancement ne peut pas être nul", ex.getMessage());
    }

    @Test
    void shouldThrow_whenEnvCibleProdNull() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setEnvCibleProd(null);
        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UtilsDemande.validateDemande(demande));
        assertEquals("L'environnement cible ne peut pas être nul", ex.getMessage());
    }

    @Test
    void shouldThrow_whenAvancementInferieurA1() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setAvancement(BigDecimal.valueOf(0));
        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UtilsDemande.validateDemande(demande));
        assertTrue(ex.getMessage().contains("L'avancement doit être compris entre 1 et 3"));
    }

    @Test
    void shouldThrow_whenAvancementSuperieurA3() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setAvancement(BigDecimal.valueOf(4));
        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> UtilsDemande.validateDemande(demande));
        assertTrue(ex.getMessage().contains("L'avancement doit être compris entre 1 et 3"));
    }

    @Test
    void shouldNotThrow_whenAvancementEgalA1() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setAvancement(BigDecimal.ONE);
        assertDoesNotThrow(() -> UtilsDemande.validateDemande(demande));
    }

    @Test
    void shouldNotThrow_whenAvancementEgalA3() {
        DemandeCreationStrategieCloud demande = buildDemandeValide();
        demande.setAvancement(BigDecimal.valueOf(3));
        assertDoesNotThrow(() -> UtilsDemande.validateDemande(demande));
    }
}
