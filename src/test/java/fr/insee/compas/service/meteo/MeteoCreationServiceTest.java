package fr.insee.compas.service.meteo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.meteo.DemandeCreationMeteo;
import fr.insee.compas.repository.TableFaitsRepository;

@SpringBootTest
@ActiveProfiles("test")
class MeteoCreationServiceTest {

    @Autowired MeteoCreationService meteoService;

    @Autowired private TableFaitsRepository tableFaitsRepository;

    @Test
    @Sql(
            scripts = "classpath:meteo/data-creer-meteo.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void creerMeteoEcraseLaDerniereMeteoPourUneApplicationEtUneDate() {

        LocalDate date = LocalDate.of(2026, 1, 12);
        TableFaits meteosApp7Avant = tableFaitsRepository.findById(6L).orElseThrow();

        assertThat(meteosApp7Avant.getIdApplication()).isEqualTo(7);
        assertThat(meteosApp7Avant.getValeur()).isEqualByComparingTo("3");
        assertThat(meteosApp7Avant.getCommentaire()).isEqualTo("comm1");
        assertThat(meteosApp7Avant.getDate()).isEqualTo(date);

        DemandeCreationMeteo demande =
                DemandeCreationMeteo.builder()
                        .idsApplication(List.of(7, 8))
                        .date(date)
                        .valeurMeteo(BigDecimal.valueOf(4))
                        .commentaire("nouvelle meteo")
                        .build();

        List<Long> ids = meteoService.creerMeteo(demande);

        assertThat(ids).hasSize(2);

        TableFaits updated = tableFaitsRepository.findById(ids.getFirst()).orElseThrow();

        TableFaits updated2 = tableFaitsRepository.findById(ids.get(1)).orElseThrow();

        assertThat(updated.getIdApplication()).isEqualTo(7);
        assertThat(updated.getValeur()).isEqualByComparingTo("4");
        assertThat(updated.getCommentaire()).isEqualTo("nouvelle meteo");
        assertThat(updated.getDate()).isEqualTo(date);

        assertThat(updated2.getIdApplication()).isEqualTo(8);
        assertThat(updated2.getValeur()).isEqualByComparingTo("4");
        assertThat(updated2.getCommentaire()).isEqualTo("nouvelle meteo");
        assertThat(updated2.getDate()).isEqualTo(date);
    }
}
