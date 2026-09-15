package fr.insee.compas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.projection.maturite.MaturiteCloudProjection;

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
                          AND tf.id_indicateur IN (601,602,603,604,605,606,607,608,609,610,611,612,615,616,617)
                    )
                    SELECT
                        r.id_application AS "idApplication",
                        MAX(CASE WHEN r.id_indicateur = 601 AND r.rn = 1 THEN r.valeur END) AS "scoreMaturite",
                        CASE
                            WHEN MAX(CASE WHEN r.id_indicateur = 601 AND r.rn = 1 THEN r.valeur END) IS NULL THEN NULL
                            WHEN CAST(MAX(CASE WHEN r.id_indicateur = 601 AND r.rn = 1 THEN r.valeur END) AS DECIMAL) < 0.2 THEN 'D'
                            WHEN CAST(MAX(CASE WHEN r.id_indicateur = 601 AND r.rn = 1 THEN r.valeur END) AS DECIMAL) < 0.46 THEN 'C'
                            WHEN CAST(MAX(CASE WHEN r.id_indicateur = 601 AND r.rn = 1 THEN r.valeur END) AS DECIMAL) < 0.7 THEN 'B'
                            ELSE 'A'
                        END AS maturite,
                        MAX(CASE WHEN r.id_indicateur = 602 AND r.rn = 1 THEN r.valeur END) AS robustesse,
                        MAX(CASE WHEN r.id_indicateur = 603 AND r.rn = 1 THEN r.valeur END) AS "scoreBenefice",
                        MAX(CASE WHEN r.id_indicateur = 604 AND r.rn = 1 THEN r.valeur END) AS "scoreOrga",
                        MAX(CASE WHEN r.id_indicateur = 605 AND r.rn = 1 THEN r.valeur END) AS "scoreComplexite",
                        MAX(CASE WHEN r.id_indicateur = 606 AND r.rn = 1 THEN r.valeur END) AS "scoreTechnique",
                        MAX(CASE WHEN r.id_indicateur = 615 AND r.rn = 1 THEN r.valeur END) AS "scoreOpportunite",
                        MAX(CASE WHEN r.id_indicateur = 616 AND r.rn = 1 THEN r.valeur END) AS "deltaMaturite",
                        MAX(CASE WHEN r.id_indicateur = 617 AND r.rn = 1 THEN r.valeur END) AS "deltaOpportunite",
                        MAX(CASE WHEN r.id_indicateur = 607 AND r.rn = 1 THEN r.valeur END) AS "progressionDeploy",
                        MAX(CASE WHEN r.id_indicateur = 608 AND r.rn = 1 THEN r.valeur END) AS "progressionTechnos",
                        MAX(CASE WHEN r.id_indicateur = 609 AND r.rn = 1 THEN r.valeur END) AS "progressionArchi",
                        MAX(CASE WHEN r.id_indicateur = 610 AND r.rn = 1 THEN r.valeur END) AS "progressionMateqip",
                        MAX(CASE WHEN r.id_indicateur = 611 AND r.rn = 1 THEN r.valeur END) AS "progressionDevops",
                        MAX(CASE WHEN r.id_indicateur = 612 AND r.rn = 1 THEN r.valeur END) AS "progressionCloud"
                    FROM ranked r
                    WHERE r.rn = 1
                    GROUP BY r.id_application
                    ORDER BY r.id_application
                    """,
            nativeQuery = true)
    List<MaturiteCloudProjection> findAllLatestMatRobAndScores();
}
