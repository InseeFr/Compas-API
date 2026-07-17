package fr.insee.compas.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.maturite.MaturiteIndicateurTableProjection;
import fr.insee.compas.repository.projection.DevopsProjection;
import fr.insee.compas.repository.projection.MetriqueApplicationProjection;
import fr.insee.compas.repository.projection.MetriqueSumIndicateurProjection;
import fr.insee.compas.repository.projection.green.GreenItAppKubeProjection;
import fr.insee.compas.repository.projection.green.GreenItAppVmProjection;

public interface TableFaitsRepository extends JpaRepository<TableFaits, Long> {

    List<TableFaits> findByIdIndicateurAndIdModule(Integer idIndicateur, Integer idModule);

    @Query(
            value =
                    """
                           SELECT
                               tf.id_module,
                               tf.id_application,
                               tf2.commentaire,
                               tf2.valeur,
                               tf2.id_indicateur
                           FROM (
                               SELECT DISTINCT id_module, id_application
                               FROM table_faits
                               WHERE id_module IS NOT NULL
                           ) tf
                           LEFT JOIN (
                               SELECT DISTINCT ON (id_module, id_application, id_indicateur)
                                   id_module,
                                   id_application,
                                   id_indicateur,
                                   commentaire,
                                   valeur
                               FROM table_faits
                               WHERE id_module IS NOT NULL
                                 AND id_indicateur IN (:idIndicateurs)
                               ORDER BY
                                   id_module,
                                   id_application,
                                   id_indicateur,
                                   date DESC
                           ) tf2
                               ON tf.id_module = tf2.id_module
                              AND tf.id_application = tf2.id_application
                           ORDER BY tf.id_application, tf.id_module
                    """,
            nativeQuery = true)
    List<MaturiteIndicateurTableProjection> getValuesByMaturiteIndicateur(
            @Param("idIndicateurs") List<Integer> idIndicateurs);

    @Query(
            value =
"""
WITH latest_data AS (
           SELECT
               tf.id_application,
               tf.id_indicateur,
               SUM(tf.valeur) AS valeur
           FROM table_faits tf
           WHERE tf.id_application IS NOT null
           and tf.date = :dateReference
           GROUP BY tf.id_application, tf.id_indicateur
       )
       SELECT
           id_application,
           MAX(CASE WHEN id_indicateur = 201 THEN valeur END) AS ramAlloue,
           MAX(CASE WHEN id_indicateur = 202 THEN valeur END) AS ramMaxi,
           MAX(CASE WHEN id_indicateur = 203 THEN valeur END) AS disqueAlloue,
           MAX(CASE WHEN id_indicateur = 204 THEN valeur END) AS disqueConsomme,
           MAX(CASE WHEN id_indicateur = 205 THEN valeur END) AS cpuAllouee,
           MAX(CASE WHEN id_indicateur = 206 THEN valeur END) AS cpuMaxi,
           MAX(CASE WHEN id_indicateur = 207 THEN valeur END) AS consoElec,
           MAX(CASE WHEN id_indicateur = 208 THEN valeur END) AS nbrVM,
           MAX(CASE WHEN id_indicateur = 211 THEN valeur END) AS ramAlloueePd,
           MAX(CASE WHEN id_indicateur = 212 THEN valeur END) AS ramMaxiPd,
           MAX(CASE WHEN id_indicateur = 213 THEN valeur END) AS disqueAllouePd,
           MAX(CASE WHEN id_indicateur = 214 THEN valeur END) AS disqueConsommePd,
           MAX(CASE WHEN id_indicateur = 215 THEN valeur END) AS cpuAlloueePd,
           MAX(CASE WHEN id_indicateur = 216 THEN valeur END) AS cpuMaxiPd,
           MAX(CASE WHEN id_indicateur = 217 THEN valeur END) AS consoElecPd,
           MAX(CASE WHEN id_indicateur = 218 THEN valeur END) AS nbrVmPd,
           MAX(CASE WHEN id_indicateur = 227 THEN valeur END) AS asConsomme,
           MAX(CASE WHEN id_indicateur = 228 THEN valeur END) AS asConsommePd,
           MAX(CASE WHEN id_indicateur = 229 THEN valeur END) AS asAlloue,
           MAX(CASE WHEN id_indicateur = 230 THEN valeur END) AS asAllouePd
       FROM latest_data
       GROUP BY id_application
""",
            nativeQuery = true)
    List<GreenItAppVmProjection> getGreenItAppVm(@Param("dateReference") Date dateReference);

    @Query(
            value =
"""
WITH latest_data AS (
           SELECT
               tf.id_application,
               tf.id_indicateur,
               SUM(tf.valeur) AS valeur
           FROM table_faits tf
           WHERE tf.id_application IS NOT null
           and tf.date = :dateReference
           GROUP BY tf.id_application, tf.id_indicateur
       )
       SELECT
           id_application,
           MAX(CASE WHEN id_indicateur = 209 THEN valeur END) AS ramConsommee,
           MAX(CASE WHEN id_indicateur = 210 THEN valeur END) AS cpuConsomme,
           MAX(CASE WHEN id_indicateur = 219 THEN valeur END) AS ramConsommeePd,
           MAX(CASE WHEN id_indicateur = 220 THEN valeur END) AS cpuConsommeePd,
           MAX(CASE WHEN id_indicateur = 221 THEN valeur END) AS s3Consomme,
           MAX(CASE WHEN id_indicateur = 222 THEN valeur END) AS pvcConsomme,
           MAX(CASE WHEN id_indicateur = 223 THEN valeur END) AS nbPodMaxi,
           MAX(CASE WHEN id_indicateur = 224 THEN valeur END) AS s3ConsommePd,
           MAX(CASE WHEN id_indicateur = 225 THEN valeur END) AS pvcConsommePd,
           MAX(CASE WHEN id_indicateur = 226 THEN valeur END) AS nbPodMaxiPd
       FROM latest_data
       GROUP BY id_application
""",
            nativeQuery = true)
    List<GreenItAppKubeProjection> getGreenItAppKube(@Param("dateReference") Date dateReference);

    @Query(
            value =
                    """
                        SELECT DISTINCT ON (tf.id_application) tf.id_application, tf.valeur
                        FROM table_faits tf
                        WHERE tf.id_indicateur = :idIndicateur
                        ORDER BY tf.id_application, tf.date DESC
                    """,
            nativeQuery = true)
    List<MaturiteIndicateurTableProjection> getMaturitesByIdIndicateur(
            @Param("idIndicateur") Integer idIndicateur);

    List<TableFaits> findByDateAndIdIndicateurAndIdModule(
            LocalDate date, Integer idIndicateur, Integer idModule);

    Optional<List<TableFaits>> findByIdApplicationAndDateAndIdIndicateur(
            Integer idApplication, LocalDate date, Integer idIndicateur);

    @Query(
            value =
                    """
                        SELECT tf.idIndicateur as idIndicateur, sum(tf.valeur) as totalValeur FROM TableFaits tf WHERE tf.idApplication = :idApplication
                            AND tf.idIndicateur IN  :indicateurIds and tf.date = :date
                            GROUP BY tf.idIndicateur
                    """)
    List<MetriqueSumIndicateurProjection> findSumByDateAndListIndicateurIdsAndIdApplication(
            @Param("date") LocalDate date,
            @Param("indicateurIds") List<Integer> indicateurIds,
            @Param("idApplication") Integer idApplication);

    @Query(
            value =
                    """
                        SELECT DISTINCT date
                        FROM table_faits
                        WHERE id_indicateur = 207
                        UNION
                        SELECT DISTINCT date
                        FROM table_faits
                        WHERE id_indicateur = 223
                        UNION
                        SELECT DISTINCT date
                        FROM table_faits
                        WHERE id_indicateur = 227
                        ORDER BY date DESC
                    """,
            nativeQuery = true)
    List<LocalDate> findLastDateIndicateur();

    @Query(
            value =
                    """
                        SELECT sum(tf.valeur) over (partition by date order by date desc) as valeur
                        FROM TableFaits tf WHERE tf.idApplication = :idApplication
                            AND tf.idIndicateur = :idIndicateur order by date desc
                    """)
    List<BigDecimal> findLatestValueByIndicateurAndApplication(
            @Param("idIndicateur") Integer idIndicateur,
            @Param("idApplication") Integer idApplication);

    @Query(
            value =
                    """
                        SELECT*

                        FROM (
                                   SELECT *,
                                   ROW_NUMBER()

                        OVER (PARTITION BY id_module ORDER BY date DESC, id DESC) AS row_num
                                          FROM table_faits
                                          WHERE id_indicateur = :idIndicateur
                                   ) subquery
                                   WHERE row_num = 1
                    """,
            nativeQuery = true)
    List<TableFaits> findLatestValueByIndicateurByModule(
            @Param("idIndicateur") Integer idIndicateur);

    @Query(
            value =
"""
        select subquery.id_Application as idApplication, subquery.date as date, subquery.totalValeur as totalValeur from (select id_Application , date, sum(tf.valeur) over (partition by tf.id_Application, tf.date order by tf.date desc) as totalValeur,
     row_number() over (partition by tf.id_Application order by tf.date desc) as row_num FROM Table_Faits tf WHERE tf.id_Indicateur = :idIndicateur) as subquery where subquery.row_num = 1 order by totalValeur desc
""",
            nativeQuery = true)
    List<MetriqueApplicationProjection> findLatestSummedValuesByIndicateurForAllApplications(
            @Param("idIndicateur") Integer idIndicateur);

    @Query(
            value =
"""
            SELECT *
          FROM (
               SELECT *,
               ROW_NUMBER() OVER (PARTITION BY id_application ORDER BY date DESC, id DESC) AS row_num
                      FROM table_faits
                      WHERE id_indicateur = :idIndicateur
               ) subquery
               WHERE row_num = 1
""",
            nativeQuery = true)
    List<TableFaits> findLatestValueByIndicateurByApplication(
            @Param("idIndicateur") Integer idIndicateur);

    @Query(
            value =
                    """
                    WITH DernieresValeurs AS (
                        SELECT tf.id_application, tf.id_module, tf.date, tf.valeur
                        FROM table_faits tf
                        WHERE tf.id_indicateur = :idIndicateur
                             AND tf.id = (
                                 SELECT MAX(tf2.id)
                                 FROM table_faits tf2
                                 WHERE tf2.id_module = tf.id_module
                                 AND tf2.id_application = tf.id_application
                                 AND tf2.id_indicateur = tf.id_indicateur
                             )
                    )
                    SELECT dv.id_application, SUM(dv.valeur) AS sumValeur
                    FROM DernieresValeurs dv
                    GROUP BY dv.id_application;
                    """,
            nativeQuery = true)
    List<Object[]> findAggregatedSumResults(Integer idIndicateur);

    @Query(
            value =
                    """
                    WITH DernieresValeurs AS (
                        SELECT tf.id_application, tf.id_module, tf.date, tf.valeur
                        FROM table_faits tf
                        WHERE tf.id_indicateur = :idIndicateur
                             AND tf.id = (
                                 SELECT MAX(tf2.id)
                                 FROM table_faits tf2
                                 WHERE tf2.id_module = tf.id_module
                                 AND tf2.id_application = tf.id_application
                                 AND tf2.id_indicateur = tf.id_indicateur
                             )
                    )
                    SELECT dv.id_application, MAX(dv.valeur) AS sumValeur
                    FROM DernieresValeurs dv
                    GROUP BY dv.id_application;
                    """,
            nativeQuery = true)
    List<Object[]> findAggregatedMaxResults(Integer idIndicateur);

    @Query(
            value =
"""
    WITH DernieresValeurs AS (
        SELECT tf.id_application, tf.id_module, tf.date, tf.valeur
        FROM table_faits tf
        WHERE tf.id_indicateur = :idIndicateur
             AND tf.id = (
                 SELECT MAX(tf2.id)
                 FROM table_faits tf2
                 WHERE tf2.id_module = tf.id_module
                 AND tf2.id_application = tf.id_application
                 AND tf2.id_indicateur = tf.id_indicateur
             )
    )
    SELECT dv.id_application,
        CASE
            WHEN COUNT(CASE WHEN dv.valeur NOT IN (-1, -2) THEN 1 END) > 0 THEN AVG(dv.valeur)
            WHEN COUNT(DISTINCT dv.valeur) = 1 THEN MAX(dv.valeur)
            ELSE -2 -- Si mélange de NR (-2) et SO (-1), mettre NR (-2)
        END AS sumValeur
    FROM DernieresValeurs dv
    GROUP BY dv.id_application;
""",
            nativeQuery = true)
    List<Object[]> findAggregatedAvgResults(Integer idIndicateur);

    @Query(
            value =
                    """
                     WITH latest_data AS (
                                                                         SELECT
                                                                             id_module,
                                                                             id_indicateur,
                                                                             valeur,
                                                                             date,
                                                                             ROW_NUMBER() OVER (
                                                                                 PARTITION BY id_module, id_indicateur
                                                                                 ORDER BY date DESC
                                                                             ) AS rn
                                                                         FROM
                                                                             table_faits
                                                                         WHERE
                                                                             date <= :dateReference
                                                                     )
                                                                     SELECT
                                                                         id_module AS moduleId,
                                                                         MAX(CASE WHEN id_indicateur = 1 THEN valeur END) AS nbLigneCode,
                                                                         MAX(CASE WHEN id_indicateur = 2 THEN valeur END) AS nbLigneCodeNonTeste,
                                                                         MAX(CASE WHEN id_indicateur = 11 THEN valeur END) AS detteTechnique,
                                                                         MAX(CASE WHEN id_indicateur = 12 THEN valeur END) AS fiabilite
                                                                     FROM
                                                                         latest_data
                                                                     WHERE
                                                                         rn = 1
                                                                         AND id_module IS NOT NULL
                                                                     GROUP BY
                                                                         id_module;
                    """,
            nativeQuery = true)
    List<Object[]> findValueIndicateurModuleQualiteBrute(Date dateReference);

    @Query(
            value =
                    """
                           WITH latest_data AS (
                                                           SELECT
                                                               id_module,
                                                               id_application,
                                                               id_indicateur,
                                                               valeur,
                                                               date,
                                                               ROW_NUMBER() OVER (PARTITION BY id_application, id_indicateur ORDER BY date DESC) AS rn
                                                           FROM
                                                               table_faits
                                                           WHERE id_module is null  and id_application  is not null and date <= :dateReference
                                                       )
                                                       SELECT
                                                          id_application AS applicationId,
                                                          MAX(CASE WHEN id_indicateur = 1 THEN valeur END) AS nbLigneCode,
                                                          MAX(CASE WHEN id_indicateur = 2 THEN valeur END) AS nbLigneCodeNonTeste,
                                                          MAX(CASE WHEN id_indicateur = 11 THEN valeur END) AS detteTechnique,
                                                          MAX(CASE WHEN id_indicateur = 12 THEN valeur END) AS fiabilite
                                                       FROM
                                                           latest_data
                                                       WHERE
                                                           rn = 1
                                                       GROUP BY
                                                           id_application;
                    """,
            nativeQuery = true)
    List<Object[]> findValueIndicateurApplicationQualiteBrute(Date dateReference);

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
                                WHERE id_module is not null and date <= :dateReference
                           )
                           SELECT
                              id_module,
                              MAX(CASE WHEN id_indicateur = 301 THEN valeur END) AS distanceCount,
                              MAX(CASE WHEN id_indicateur = 302 THEN valeur END) AS nbDeploymentCount,
                              MAX(CASE WHEN id_indicateur = 303 THEN valeur END) AS nbContributorCount
                           FROM
                               latest_data
                           WHERE
                               rn = 1 and id_module is not null
                           GROUP BY
                               id_module;
""",
            nativeQuery = true)
    List<DevopsProjection> findValueIndicateurModuleDevopsBrute(Date dateReference);

    @Query(
            value =
"""
WITH latest_data AS (
                               SELECT
                                   id_module,
                                   id_application,
                                   id_indicateur,
                                   valeur,
                                   date,
                                   ROW_NUMBER() OVER (PARTITION BY id_application, id_indicateur ORDER BY date DESC) AS rn
                               FROM
                                   table_faits
                               WHERE id_module is null and id_application is not null and date <= :dateReference
                           )
                           SELECT
                              id_application,
                              MAX(CASE WHEN id_indicateur = 301 THEN valeur END) AS distanceCount,
                              MAX(CASE WHEN id_indicateur = 302 THEN valeur END) AS nbDeploymentCount,
                              MAX(CASE WHEN id_indicateur = 303 THEN valeur END) AS nbContributorCount
                           FROM
                               latest_data
                           WHERE
                               rn = 1
                           GROUP BY
                               id_application;
""",
            nativeQuery = true)
    List<DevopsProjection> findValueIndicateurApplicationDevopsBrute(Date dateReference);

    @Query(
            value =
"""
select count(tf) from TableFaits tf where tf.idIndicateur = :idIndicateur and tf.date = :dateIn
""")
    Integer countGreenItValuesByDate(
            @Param("dateIn") LocalDate dateIn, @Param("idIndicateur") Integer idIndicateur);

    @Query(
            value =
                    """
                    SELECT
                        f.id_application     AS id_application,
                        f.date::date         AS date,
                        f.valeur::numeric    AS valeur_meteo,
                        f.commentaire        AS commentaire
                    FROM (
                        SELECT m.id_application, m.date, m.valeur, m.commentaire,
                               ROW_NUMBER() OVER (PARTITION BY m.id_application ORDER BY m.date DESC) AS rn
                        FROM table_faits m
                        WHERE m.id_indicateur = 401 AND m.date >= :startDate AND m.date <= CURRENT_DATE
                    ) f
                    ORDER BY f.id_application, f.date DESC
                    """,
            nativeQuery = true)
    List<Object[]> findLast10MeteoPerApp(@Param("startDate") LocalDate startDate);
}
