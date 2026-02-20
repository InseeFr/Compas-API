package fr.insee.compas.service.maturitecloud.indicateur;

import static fr.insee.compas.util.MaturiteConstantes.SANS_OBJET;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.service.conversion.ConversionService;

@ExtendWith(MockitoExtension.class)
class MaturiteCalculatorServiceTest {

    @InjectMocks private MaturiteCalculatorService maturiteCalculatorService;

    @Mock private ConversionService conversionService;

    // getEnvActuelProd
    @Test
    void getEnvActuelProd_shouldReturnSansObjet_whenEnvActuelProdIsNull() {
        String result = maturiteCalculatorService.getEnvActuelProd(null, "cloud");
        assertThat(result).isEqualTo(SANS_OBJET);
    }

    @Test
    void getEnvActuelProd_shouldCallConversion_whenEnvActuelProdIsSaisieManuelle() {
        when(conversionService.convertMaturiteZoneDeProd("cloud")).thenReturn("Cloud Externe");
        String result = maturiteCalculatorService.getEnvActuelProd("Saisie manuelle", "cloud");
        assertThat(result).isEqualTo("Cloud Externe");
        verify(conversionService).convertMaturiteZoneDeProd("cloud");
    }

    @Test
    void getEnvActuelProd_shouldReturnEnvActuelProd_whenNotSaisieManuelle() {
        String result = maturiteCalculatorService.getEnvActuelProd("Kube", "cloud");
        assertThat(result).isEqualTo("Kube");
        verifyNoInteractions(conversionService);
    }

    // getAllCommentaires
    @Test
    void getAllCommentaires_shouldReturnJoinedCommentaires_whenAllValid() {
        String result =
                maturiteCalculatorService.getAllCommentaires(
                        List.of("commentaire1", "commentaire2"));
        assertThat(result).isEqualTo("commentaire1; commentaire2");
    }

    @Test
    void getAllCommentaires_shouldFilterNullAndBlank() {
        String result =
                maturiteCalculatorService.getAllCommentaires(
                        Arrays.asList("commentaire1", null, "", " ", "commentaire2"));
        assertThat(result).isEqualTo("commentaire1; commentaire2");
    }

    @Test
    void getAllCommentaires_shouldReturnEmpty_whenAllNullOrBlank() {
        String result = maturiteCalculatorService.getAllCommentaires(Arrays.asList(null, "", " "));
        assertThat(result).isEmpty();
    }

    // hasOneEcart
    @Test
    void hasOneEcart_shouldReturnTrue_whenOneEcartIsOui() {
        boolean result = maturiteCalculatorService.hasOneEcart(List.of("non", "oui", "non"));
        assertThat(result).isTrue();
    }

    @Test
    void hasOneEcart_shouldReturnFalse_whenNoEcartIsOui() {
        boolean result = maturiteCalculatorService.hasOneEcart(List.of("non", "non"));
        assertThat(result).isFalse();
    }

    @Test
    void hasOneEcart_shouldReturnFalse_whenListIsEmpty() {
        boolean result = maturiteCalculatorService.hasOneEcart(Collections.emptyList());
        assertThat(result).isFalse();
    }

    // getStratCloud
    @Test
    void getStratCloud_shouldReturnEnCours_whenOneIsEnCours() {
        String result =
                maturiteCalculatorService.getStratCloud(
                        List.of("Validée", "En cours", "A instruire"));
        assertThat(result).isEqualTo("En cours");
    }

    @Test
    void getStratCloud_shouldReturnValidee_whenAllAreValidee() {
        String result = maturiteCalculatorService.getStratCloud(List.of("Validée", "Validée"));
        assertThat(result).isEqualTo("Validée");
    }

    @Test
    void getStratCloud_shouldReturnAInstruire_whenMixedWithoutEnCours() {
        String result = maturiteCalculatorService.getStratCloud(List.of("Validée", "A instruire"));
        assertThat(result).isEqualTo("A instruire");
    }

    @Test
    void getStratCloud_shouldReturnAInstruire_whenListIsEmpty() {
        String result = maturiteCalculatorService.getStratCloud(Collections.emptyList());
        assertThat(result).isEqualTo("A instruire");
    }

    // calculateTauxCloudProd
    @Test
    void calculateTauxCloudProd_shouldReturn0_whenListIsEmpty() {
        String result = maturiteCalculatorService.calculateTauxCloudProd(Collections.emptyList());
        assertThat(result).isEqualTo("0%");
    }

    @Test
    void calculateTauxCloudProd_shouldReturn100_whenAllAreCloud() {
        String result =
                maturiteCalculatorService.calculateTauxCloudProd(List.of("Kube", "Cloud Externe"));
        assertThat(result).isEqualTo("100%");
    }

    @Test
    void calculateTauxCloudProd_shouldReturn0_whenNoneAreCloud() {
        String result = maturiteCalculatorService.calculateTauxCloudProd(List.of("VM", "Autre"));
        assertThat(result).isEqualTo("0%");
    }

    @Test
    void calculateTauxCloudProdModule_shouldReturn0_whenNoneAreCloud() {
        String result = maturiteCalculatorService.calculateTauxCloudProdModule("VM");
        assertThat(result).isEqualTo("0%");
    }

    @Test
    void calculateTauxCloudProdModule_shouldReturn100_whenIsCloud() {
        String result = maturiteCalculatorService.calculateTauxCloudProdModule("Kube");
        String result2 = maturiteCalculatorService.calculateTauxCloudProdModule("Cloud Externe");
        assertThat(result).isEqualTo("100%");
        assertThat(result2).isEqualTo("100%");
    }

    @Test
    void calculateTauxCloudProdModule_shouldReturn0_whenIsNoNe() {
        String result = maturiteCalculatorService.calculateTauxCloudProdModule("SO");
        assertThat(result).isEqualTo("0%");
    }

    @Test
    void calculateTauxCloudProd_shouldReturnCorrectTaux_whenMixed() {
        String result =
                maturiteCalculatorService.calculateTauxCloudProd(
                        List.of("Kube", "VM", "Cloud Externe", "Autre"));
        assertThat(result).isEqualTo("50%");
    }

    @Test
    void shouldJoinDistinctNonBlankValues() {
        List<String> input = Arrays.asList("DEV", "QA", "DEV", "", " ", null, "PROD");

        String result = maturiteCalculatorService.getEnvApp(input);

        assertEquals("DEV, QA, PROD", result);
    }

    @Test
    void shouldReturnEmptyStringWhenListIsEmpty() {
        List<String> input = List.of();

        String result = maturiteCalculatorService.getEnvApp(input);

        assertEquals("", result);
    }

    @Test
    void shouldReturnEmptyStringWhenAllValuesAreBlankOrNull() {
        List<String> input = Arrays.asList(null, "", "   ");

        String result = maturiteCalculatorService.getEnvApp(input);

        assertEquals("", result);
    }

    @Test
    void shouldReturnSingleValueWhenOnlyOneValidElement() {
        List<String> input = Arrays.asList(null, "DEV", "DEV", " ");

        String result = maturiteCalculatorService.getEnvApp(input);

        assertEquals("DEV", result);
    }
}
