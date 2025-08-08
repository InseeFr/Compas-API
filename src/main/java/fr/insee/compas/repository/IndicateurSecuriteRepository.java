package fr.insee.compas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.insee.compas.model.compas.TableFaits;

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
)
SELECT
    id_module AS moduleId,
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
    List<Object[]> findValueBruteModule();

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
    )
    SELECT
        id_application AS applicationId,
        MAX(CASE WHEN id_indicateur = 7 THEN valeur END) AS nbCveCritical,
        MAX(CASE WHEN id_indicateur = 8 THEN valeur END) AS nbCveHigh,
        MAX(CASE WHEN id_indicateur = 9 THEN valeur END) AS nbCveMedium,
        MAX(CASE WHEN id_indicateur = 10 THEN valeur END) AS nbCveLow
    FROM
        latest_data
    WHERE
        rn = 1 AND id_module IS NULL
    GROUP BY
        id_application
""",
            nativeQuery = true)
    List<Object[]> findValueBruteApplication();
}
