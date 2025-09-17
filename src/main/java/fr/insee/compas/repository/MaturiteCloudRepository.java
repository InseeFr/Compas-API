package fr.insee.compas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.insee.compas.model.compas.TableFaits;

public interface MaturiteCloudRepository extends JpaRepository<TableFaits, Long> {

    @Query(
            value =
                    """
WITH ranked AS (
    SELECT
        tf.id_application,
        tf.id_indicateur,
        tf.valeur,
        tf."date",
        tf.id,
        ROW_NUMBER() OVER (
            PARTITION BY tf.id_application, tf.id_indicateur
            ORDER BY tf."date" DESC, tf.id DESC
        ) AS rn
    FROM table_faits tf
    WHERE tf.id_application IS NOT NULL
      AND tf.id_module IS NULL
      AND tf.id_indicateur IN (601,602,603,604,605,606,607,608,609,610,611,612)
)
SELECT
    r.id_application AS id_application,
    -- 601 maturité -> lettre
    CASE CAST(MAX(CASE WHEN r.id_indicateur = 601 THEN r.valeur END) AS INT)
         WHEN 1 THEN 'A' WHEN 2 THEN 'B' WHEN 3 THEN 'C' WHEN 4 THEN 'D'
         ELSE NULL
    END AS maturite,
    -- 602 robustesse (numérique)
    MAX(CASE WHEN r.id_indicateur = 602 THEN r.valeur END) AS robustesse,
    -- 603..606 scores
    MAX(CASE WHEN r.id_indicateur = 603 THEN r.valeur END) AS score_benefice,
    MAX(CASE WHEN r.id_indicateur = 604 THEN r.valeur END) AS score_orga,
    MAX(CASE WHEN r.id_indicateur = 605 THEN r.valeur END) AS score_complexite,
    MAX(CASE WHEN r.id_indicateur = 606 THEN r.valeur END) AS score_technique,
    -- 607..612 progressions
    MAX(CASE WHEN r.id_indicateur = 607 THEN r.valeur END) AS progression_deploy,
    MAX(CASE WHEN r.id_indicateur = 608 THEN r.valeur END) AS progression_technos,
    MAX(CASE WHEN r.id_indicateur = 609 THEN r.valeur END) AS progression_archi,
    MAX(CASE WHEN r.id_indicateur = 610 THEN r.valeur END) AS progression_mateqip,
    MAX(CASE WHEN r.id_indicateur = 611 THEN r.valeur END) AS progression_devops,
    MAX(CASE WHEN r.id_indicateur = 612 THEN r.valeur END) AS progression_cloud
FROM ranked r
WHERE r.rn = 1
GROUP BY r.id_application
ORDER BY r.id_application
""",
            nativeQuery = true)
    List<Object[]> findAllLatestMatRobAndScores();
}
