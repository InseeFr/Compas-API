package fr.insee.compas.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import fr.insee.compas.model.compas.TableFaits;

@DataJpaTest(
        showSql = false,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:testdb",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.defer-datasource-initialization=true"
        })
class TableFaitsRepositoryTest {

    @Autowired private TableFaitsRepository tableFaitsRepository;

    @Autowired private TestEntityManager entityManager;

    @Test
    void testFindIdsByEtatCourant() {
        final List<TableFaits> tdfs =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(201, 244);
        assertThat(tdfs).isNotNull().hasSize(1);
        assertThat(tdfs.get(0).getValeur()).isNotNull().isGreaterThan(BigDecimal.valueOf(1));
    }

    @Test
    void testDontFindIdsByEtatCourant() {
        final List<TableFaits> tdfs =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(242, 6);
        assertThat(tdfs).isNotNull().isEmpty();
    }

    @Test
    void testFindLatestValueByIndicateurAndApplication() {
        final List<BigDecimal> value =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(201, 130);
        assertNotNull(value);
        assertEquals(3, value.size());
        assertEquals(new BigDecimal("16.00"), value.get(0));
    }

    @Test
    void testFindAggregatedSumResults() {

        final Integer idIndicateur = 2;
        final List<Object[]> results = tableFaitsRepository.findAggregatedSumResults(idIndicateur);

        assertEquals(1, results.size());
        assertEquals(1, ((Number) results.getFirst()[0]).intValue()); // id_application attendu
        assertEquals(300, ((Number) results.getFirst()[1]).intValue()); // Somme attendue
    }

    @Test
    void testFindAggregatedAvgResults() {

        final Integer idIndicateur = 2;
        final List<Object[]> results = tableFaitsRepository.findAggregatedAvgResults(idIndicateur);

        assertEquals(1, results.size());
        assertEquals(1, ((Number) results.getFirst()[0]).intValue()); // id_application attendu
        assertEquals(150, ((Number) results.getFirst()[1]).intValue()); // Somme attendue
    }

    @Test
    void testFindLatestSummedValuesByIndicateurForAllModules_Indicateur2() {
        final List<Object[]> results =
                tableFaitsRepository.findLatestSummedValuesByIndicateurForAllModules(2);
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(results).isNotNull();
        softAssertions.assertThat(results.size()).isEqualTo(2);
        softAssertions.assertThat(results.get(0)[0]).isEqualTo(2);
        softAssertions.assertThat(results.get(0)[1]).isEqualTo(Date.valueOf("2024-12-02"));
        softAssertions.assertThat(((Number) results.get(0)[2]).intValue()).isEqualTo(395);
        softAssertions.assertThat(results.get(1)[0]).isEqualTo(1);
        softAssertions.assertThat(((Number) results.get(1)[2]).intValue()).isEqualTo(102);
        softAssertions.assertAll();
    }

    @Test
    void testFindLatestSummedValuesByIndicateurForAllModules_NoResult() {
        final List<Object[]> results =
                tableFaitsRepository.findLatestSummedValuesByIndicateurForAllModules(207);
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(results).isNotNull();
        softAssertions.assertThat(results.size()).isEqualTo(0);
        softAssertions.assertAll();
    }

    @Test
    void testFindLatestSummedValuesByIndicateurForAllApplications_Indicateur201() {
        final List<Object[]> results =
                tableFaitsRepository.findLatestSummedValuesByIndicateurForAllApplications(201);
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(results).isNotNull();
        softAssertions.assertThat(results.size()).isEqualTo(2);
        softAssertions.assertThat(results.get(0)[0]).isEqualTo(130);
        softAssertions.assertThat(results.get(0)[1]).isEqualTo(Date.valueOf("2024-12-04"));
        softAssertions.assertThat(((Number) results.get(0)[2]).intValue()).isEqualTo(16);
        softAssertions.assertThat(results.get(1)[0]).isEqualTo(131);
        softAssertions.assertThat(((Number) results.get(1)[2]).intValue()).isEqualTo(8);
        softAssertions.assertAll();
    }

    @Test
    void testFindLatestSummedValuesByIndicateurForAllApplications_NoResult() {
        final List<Object[]> results =
                tableFaitsRepository.findLatestSummedValuesByIndicateurForAllApplications(207);
        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(results).isNotNull();
        softAssertions.assertThat(results.size()).isEqualTo(0);
        softAssertions.assertAll();
    }
}
