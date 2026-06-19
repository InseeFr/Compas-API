package fr.insee.compas.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.projection.SecuriteProjection;

public interface IndicateurSecuriteRepository extends JpaRepository<TableFaits, Long> {

    @Query(
            value =
                    """
                    WITH latest_data AS (
                        SELECT
                            id_module,
                            id_indicateur,
                            valeur,
                            date,
                            ROW_NUMBER() OVER (PARTITION BY id_module, id_indicateur ORDER BY date DESC) AS rn
                        FROM
                            table_faits
                        Where date <= :dateReference
                    )
                    SELECT
                        id_module AS id,
                        MAX(CASE WHEN id_indicateur = 3 THEN valeur END) AS nbCveCritical,
                        MAX(CASE WHEN id_indicateur = 4 THEN valeur END) AS nbCveHigh,
                        MAX(CASE WHEN id_indicateur = 5 THEN valeur END) AS nbCveMedium,
                        MAX(CASE WHEN id_indicateur = 6 THEN valeur END) AS nbCveLow
                    FROM
                        latest_data
                    WHERE
                        rn = 1 AND id_module IS NOT NULL
                    GROUP BY
                        id_module
                    """,
            nativeQuery = true)
    List<SecuriteProjection> findValueBruteModule(Date dateReference);

    @Query(
            value =
                    """
                        WITH latest_data AS (
                            SELECT
                                id_application,
                                id_module,
                                id_indicateur,
                                valeur,
                                date,
                                ROW_NUMBER() OVER (PARTITION BY id_application, id_indicateur ORDER BY date DESC) AS rn
                            FROM
                                table_faits
                            Where date <= :dateReference
                        )
                        SELECT
                            id_application AS id,
                            MAX(CASE WHEN id_indicateur = 7 THEN valeur END) AS nbCveCritical,
                            MAX(CASE WHEN id_indicateur = 8 THEN valeur END) AS nbCveHigh,
                            MAX(CASE WHEN id_indicateur = 9 THEN valeur END) AS nbCveMedium,
                            MAX(CASE WHEN id_indicateur = 10 THEN valeur END) AS nbCveLow,
                            MAX(CASE WHEN id_indicateur = 102 THEN valeur END) AS nbVmNonMaj,
                            MAX(CASE WHEN id_indicateur = 101 THEN valeur END) AS delaiMaj
                        FROM
                            latest_data
                        WHERE
                            rn = 1 AND id_module IS NULL
                        GROUP BY
                            id_application
                    """,
            nativeQuery = true)
    List<SecuriteProjection> findValueBruteApplication(Date dateReference);

    @Query(
            value =
                    """
                    WITH per_month AS (
                        SELECT
                            tf.id_application,
                            date_trunc('month', tf.date)::date AS "month",
                            tf.date,
                            tf.valeur,
                            tf.id,
                            CASE WHEN EXTRACT(DAY FROM tf.date) = 1 THEN 0 ELSE 1 END AS not_first
                        FROM table_faits tf
                        WHERE tf.id_indicateur = 7              -- CVE critiques (application)
                          AND tf.id_module IS NULL
                          AND tf.id_application IS NOT NULL
                    ),
                    ranked AS (
                        SELECT
                            id_application,
                            "month",
                            valeur,
                            ROW_NUMBER() OVER (
                                PARTITION BY id_application, "month"
                                ORDER BY not_first ASC, date ASC, id DESC
                            ) AS rn
                        FROM per_month
                    )
                    SELECT id_application, "month", valeur
                    FROM ranked
                    WHERE rn = 1
                    ORDER BY id_application, "month"
                    """,
            nativeQuery = true)
    List<Object[]> findMonthlyCriticalByApplication();
}
