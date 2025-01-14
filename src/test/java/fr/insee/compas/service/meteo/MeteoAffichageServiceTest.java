package fr.insee.compas.service.meteo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import fr.insee.compas.model.meteo.Meteo;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.service.OscarService;

@SpringBootTest
@ActiveProfiles("test")
class MeteoAffichageServiceTest {

    @MockitoBean OscarService oscarService;

    @Autowired MeteoAffichageService meteoAffichageService;

    @Test
    @Sql(
            scripts = {"classpath:meteo/data-meteo.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void listerApplicationsMeteoRenvoieApplisOscarAvecDerniereMeteoTest() {
        String sndi = "sndi (aka Service dev) test";
        String appName = "Application test";
        String domaineSndi = "Domaine sndi test";
        Application applicationOscar =
                Application.builder()
                        .idApplication(1)
                        .appName(appName)
                        .domaineSndi(domaineSndi)
                        .sndi(sndi)
                        .build();
        Mockito.when(oscarService.getApplications()).thenReturn(List.of(applicationOscar));

        List<Meteo> listeMeteos = meteoAffichageService.listerApplicationsMeteo();
        assertThat(listeMeteos).hasSize(1);
        Meteo meteo = listeMeteos.get(0);
        assertThat(meteo.getValeurMeteo()).isEqualByComparingTo(BigDecimal.valueOf(4));
        assertThat(meteo.getSndi()).isEqualTo(sndi);
        assertThat(meteo.getAppName()).isEqualTo(appName);
        assertThat(meteo.getDomaineSndi()).isEqualTo(domaineSndi);
        assertThat(meteo.getDate()).isEqualTo(LocalDate.of(2025, 1, 10));
    }
}
