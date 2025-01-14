package fr.insee.compas.service.meteo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import fr.insee.compas.model.meteo.DemandeCreationModificationMeteo;
import fr.insee.compas.repository.TableFaitsRepository;

@SpringBootTest
@ActiveProfiles("test")
public class MeteoCreationServiceTest {

    @Autowired MeteoCreationService meteoService;

    @Autowired TableFaitsRepository tableFaitsRepository;

    @Test
    void creerRessentiMeteoServiceTest() {
        int idApplication = 100;
        BigDecimal valeurMeteo = new BigDecimal(4);
        String commentaire = "Tout se passe bien";
        DemandeCreationModificationMeteo demandeCreationMeteo =
                DemandeCreationModificationMeteo.builder()
                        .idApplication(idApplication)
                        .idIndicateur(MeteoCreationService.ID_INDICATEUR_METEO)
                        .valeurMeteo(valeurMeteo)
                        .commentaire(commentaire)
                        .date(LocalDate.of(2025, 1, 10))
                        .build();
        assertEquals(1L, meteoService.creerMeteo(demandeCreationMeteo));
        tableFaitsRepository
                .findById(1L)
                .ifPresentOrElse(
                        tableFaits -> {
                            assertThat(tableFaits.getValeur()).isEqualByComparingTo(valeurMeteo);
                            assertThat(tableFaits.getCommentaire()).isEqualTo(commentaire);
                            assertThat(tableFaits.getDate()).isEqualTo(LocalDate.of(2025, 1, 10));
                        },
                        AssertionsForClassTypes::fail);
    }
}
