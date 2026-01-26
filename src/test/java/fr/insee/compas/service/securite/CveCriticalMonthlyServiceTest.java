package fr.insee.compas.service.securite;

import static org.springframework.test.util.AssertionErrors.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import fr.insee.compas.view.IndicateurApplicationSecuriteMonthly;

@SpringBootTest
@ActiveProfiles("test")
class CveCriticalMonthlyServiceTest {
    @Autowired private CveCriticalMonthlyService cveCriticalMonthlyService;

    @Test
    @Sql(
            scripts = {"classpath:cveSecurite/data-cve-monthly.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getMonthly_Success() {
        IndicateurApplicationSecuriteMonthly indicateurApplicationSecuriteMonthly1 =
                IndicateurApplicationSecuriteMonthly.builder()
                        .applicationId(192)
                        .nbCveCritical(32)
                        .month(LocalDate.of(2026, 1, 1))
                        .build();
        IndicateurApplicationSecuriteMonthly indicateurApplicationSecuriteMonthly2 =
                IndicateurApplicationSecuriteMonthly.builder()
                        .applicationId(192)
                        .nbCveCritical(2)
                        .month(LocalDate.of(2025, 4, 1))
                        .build();
        IndicateurApplicationSecuriteMonthly indicateurApplicationSecuriteMonthly3 =
                IndicateurApplicationSecuriteMonthly.builder()
                        .applicationId(192)
                        .nbCveCritical(0)
                        .month(LocalDate.of(2025, 1, 1))
                        .build();
        List<IndicateurApplicationSecuriteMonthly> result = cveCriticalMonthlyService.getMonthly();
        assertEquals(
                "Devrait retourner des applications avec les cves critiques par mois",
                List.of(
                        indicateurApplicationSecuriteMonthly3,
                        indicateurApplicationSecuriteMonthly2,
                        indicateurApplicationSecuriteMonthly1),
                result);
    }
}
