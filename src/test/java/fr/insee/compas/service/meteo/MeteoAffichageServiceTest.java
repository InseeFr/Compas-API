package fr.insee.compas.service.meteo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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

    private static final ZoneId TZ_PARIS = ZoneId.of("Europe/Paris");

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
        when(oscarService.getApplications()).thenReturn(List.of(applicationOscar));

        List<Meteo> listeMeteos = meteoAffichageService.listerApplicationsMeteo();
        assertThat(listeMeteos).hasSize(1);
        Meteo meteo = listeMeteos.getFirst();
        assertThat(meteo.getValeurMeteo()).isEqualByComparingTo(BigDecimal.valueOf(4));
        assertThat(meteo.getSndi()).isEqualTo(sndi);
        assertThat(meteo.getAppName()).isEqualTo(appName);
        assertThat(meteo.getDomaineSndi()).isEqualTo(domaineSndi);
        assertThat(meteo.getDate()).isEqualTo(LocalDate.of(2025, 1, 10));
    }

    // --- 1) Filtrage par ancienneté : >= 23 jours ---
    // today = 2025-02-15
    // app 10 -> 2025-01-23 (23 jours) => incluse
    // app 11 -> 2025-01-24 (22 jours) => exclue
    @Test
    @Sql(
            scripts = "classpath:meteo/data-meteo-ages.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void listerApplicationsMeteoAncienne_filtreAgeMin23() {
        Application app10 =
                Application.builder()
                        .idApplication(10)
                        .appName("App 10")
                        .domaineSndi("DS1")
                        .sndi("S1")
                        .build();
        Application app11 =
                Application.builder()
                        .idApplication(11)
                        .appName("App 11")
                        .domaineSndi("DS2")
                        .sndi("S2")
                        .build();
        when(oscarService.getApplications()).thenReturn(List.of(app10, app11));

        // ✅ Calcule les dates hors mockStatic
        LocalDate today = LocalDate.of(2025, 2, 15);
        LocalDate expectedDate = LocalDate.of(2025, 1, 23);

        List<Meteo> results;
        try (MockedStatic<LocalDate> mocked =
                Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> LocalDate.now(TZ_PARIS)).thenReturn(today);
            results = meteoAffichageService.listerApplicationsMeteoAncienne();
        }

        assertThat(results).extracting(Meteo::getIdApplication).containsExactly(10);
        Meteo m = results.getFirst();
        assertThat(m.getAppName()).isEqualTo("App 10");
        assertThat(m.getValeurMeteo()).isEqualByComparingTo("3");
        assertThat(m.getDate()).isEqualTo(expectedDate);
    }

    // --- 2) Variante paramétrable : seuil 30 jours, avec un app sans météo (filtré) ---
    // today = 2025-02-15
    // app 12 -> 2024-12-31 (46 jours) => incluse
    // app 13 -> pas de météo => exclue
    @Test
    @Sql(
            scripts = "classpath:meteo/data-meteo-ages.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void listerApplicationsMeteoAvecAgeMin_30j_inclutSeulementPlusAnciennesEtIgnoreSansMeteo() {
        Application app12 =
                Application.builder()
                        .idApplication(12)
                        .appName("App 12")
                        .domaineSndi("DS3")
                        .sndi("S3")
                        .build();
        Application app13 =
                Application.builder()
                        .idApplication(13)
                        .appName("App 13")
                        .domaineSndi("DS4")
                        .sndi("S4")
                        .build();
        when(oscarService.getApplications()).thenReturn(List.of(app12, app13));

        LocalDate today = LocalDate.of(2025, 2, 15);
        LocalDate expectedDate = LocalDate.of(2024, 12, 31);

        List<Meteo> results;
        try (MockedStatic<LocalDate> mocked =
                Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> LocalDate.now(TZ_PARIS)).thenReturn(today);
            results = meteoAffichageService.listerApplicationsMeteoAvecAgeMin(30);
        }

        assertThat(results).extracting(Meteo::getIdApplication).containsExactly(12);
        Meteo m = results.getFirst();
        assertThat(m.getValeurMeteo()).isEqualByComparingTo("5");
        assertThat(m.getDate()).isEqualTo(expectedDate);
    }

    // --- 3) Sélection de la dernière météo + TIMESTAMP -> LocalDate ---
    @Test
    @Sql(
            scripts = "classpath:meteo/data-meteo-multiples-dates.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void listerApplicationsMeteo_prendDerniereParTri_DatePuisId_etGereTimestamp() {
        Application app20 =
                Application.builder()
                        .idApplication(20)
                        .appName("App 20")
                        .domaineSndi("DS5")
                        .sndi("S5")
                        .build();
        Application app21 =
                Application.builder()
                        .idApplication(21)
                        .appName("App 21")
                        .domaineSndi("DS6")
                        .sndi("S6")
                        .build();
        when(oscarService.getApplications()).thenReturn(List.of(app20, app21));

        // Pas de mock de date nécessaire ici
        List<Meteo> liste = meteoAffichageService.listerApplicationsMeteo();
        assertThat(liste).hasSize(2);

        Meteo m20 =
                liste.stream().filter(m -> m.getIdApplication() == 20).findFirst().orElseThrow();
        assertThat(m20.getValeurMeteo()).isEqualByComparingTo("8"); // prend la ligne id=200
        assertThat(m20.getDate()).isEqualTo(LocalDate.of(2025, 1, 20));

        Meteo m21 =
                liste.stream().filter(m -> m.getIdApplication() == 21).findFirst().orElseThrow();
        assertThat(m21.getDate()).isEqualTo(LocalDate.of(2025, 1, 1)); // TIMESTAMP -> LocalDate
        assertThat(m21.getValeurMeteo()).isEqualByComparingTo("9");
    }

    // --- 4) Vide si toutes trop récentes ---
    // today = 2025-02-15, app 11 = 2025-01-24 (22 jours), seuil 23 => vide
    @Test
    @Sql(
            scripts = "classpath:meteo/data-meteo-ages.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void listerApplicationsMeteoAvecAgeMin_aucunResultatQuandToutesTropRecentes() {
        Application app11 =
                Application.builder()
                        .idApplication(11)
                        .appName("App 11")
                        .domaineSndi("DS2")
                        .sndi("S2")
                        .build();
        when(oscarService.getApplications()).thenReturn(List.of(app11));

        LocalDate today = LocalDate.of(2025, 2, 15);

        List<Meteo> results;
        try (MockedStatic<LocalDate> mocked =
                Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> LocalDate.now(TZ_PARIS)).thenReturn(today);
            results = meteoAffichageService.listerApplicationsMeteoAvecAgeMin(23);
        }

        assertThat(results).isEmpty();
    }
}
