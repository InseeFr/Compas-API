package fr.insee.compas.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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

    @Test
    void testFindIdsByEtatCourant() {
        final List<TableFaits> tdfs =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(101, 244);
        assertThat(tdfs).isNotNull().hasSize(1);
        assertThat(tdfs.get(0).getValeur()).isNotNull().isGreaterThan(BigDecimal.valueOf(1));
    }

    @Test
    void testDontFindIdsByEtatCourant() {
        final List<TableFaits> tdfs =
                tableFaitsRepository.findLatestValueByIndicateurAndModule(242, 6);
        assertThat(tdfs).isNotNull().hasSize(0);
    }

    @Test
    void testFindLatestValueByIndicateurAndApplication() {
        final List<Integer> value =
                tableFaitsRepository.findLatestValueByIndicateurAndApplication(101, 130);
        assertNotNull(value);
        assertEquals(1, value.size());
        assertEquals(8, value.get(0));
    }
}
