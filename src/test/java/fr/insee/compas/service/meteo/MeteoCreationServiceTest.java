package fr.insee.compas.service.meteo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import fr.insee.compas.model.meteo.DemandeCreationMeteo;
import fr.insee.compas.repository.TableFaitsRepository;

@SpringBootTest
@ActiveProfiles("test")
public class MeteoCreationServiceTest {

    @Autowired MeteoCreationService meteoService;

    @Autowired TableFaitsRepository tableFaitsRepository;

    @Test
    void creerRessentiMeteoServiceTest() {
        int idApplication1 = 100;
        int idApplication2 = 101;
        BigDecimal valeurMeteo = new BigDecimal(4);
        String commentaire = "Tout se passe bien";
        DemandeCreationMeteo demandeCreationMeteo =
                DemandeCreationMeteo.builder()
                        .idsApplication(List.of(idApplication1, idApplication2))
                        .valeurMeteo(valeurMeteo)
                        .commentaire(commentaire)
                        .date(LocalDate.of(2025, 1, 10))
                        .build();
        assertEquals(List.of(1L, 2L), meteoService.creerMeteo(demandeCreationMeteo));
        tableFaitsRepository
                .findById(1L)
                .ifPresentOrElse(
                        tableFaits -> {
                            assertThat(tableFaits.getValeur()).isEqualByComparingTo(valeurMeteo);
                            assertThat(tableFaits.getCommentaire()).isEqualTo(commentaire);
                            assertThat(tableFaits.getDate()).isEqualTo(LocalDate.of(2025, 1, 10));
                        },
                        AssertionsForClassTypes::fail);
        tableFaitsRepository
                .findById(2L)
                .ifPresentOrElse(
                        tableFaits -> {
                            assertThat(tableFaits.getValeur()).isEqualByComparingTo(valeurMeteo);
                            assertThat(tableFaits.getCommentaire()).isEqualTo(commentaire);
                            assertThat(tableFaits.getDate()).isEqualTo(LocalDate.of(2025, 1, 10));
                        },
                        AssertionsForClassTypes::fail);
    }
}
