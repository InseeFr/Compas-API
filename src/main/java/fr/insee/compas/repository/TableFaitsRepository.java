package fr.insee.compas.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.insee.compas.model.compas.TableFaits;

public interface TableFaitsRepository extends JpaRepository<TableFaits, Long> {

    @Query(
            value =
                    """
                        SELECT tf
                        FROM TableFaits tf
                        WHERE tf.date = (
                            SELECT MAX(tf2.date)
                            FROM TableFaits tf2
                            WHERE tf2.idModule = tf.idModule
                            AND tf2.idIndicateur = tf.idIndicateur
                            AND tf2.idModule = :idModule
                            AND tf2.idIndicateur = :idIndicateur
                        )
                    """)
    List<TableFaits> findLatestValueByIndicateurAndModule(
            @Param("idIndicateur") Integer idIndicateur, @Param("idModule") Integer idModule);

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
                        SELECT tf
                        FROM TableFaits tf
                        WHERE tf.date = (
                            SELECT MAX(tf2.date)
                            FROM TableFaits tf2
                            WHERE tf2.idIndicateur = tf.idIndicateur
                            AND tf2.idIndicateur = :idIndicateur
                        )
                        AND tf.idIndicateur = :idIndicateur
                    """)
    List<TableFaits> findLatestValueByIndicateur(@Param("idIndicateur") Integer idIndicateur);

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
    select subquery.id_Application, subquery.date, subquery.totalValeur from (select id_Application , date, sum(tf.valeur) over (partition by tf.id_Application, tf.date order by tf.date desc) as totalValeur,
     row_number() over (partition by tf.id_Application order by tf.date desc) as row_num FROM Table_Faits tf WHERE tf.id_Indicateur = :idIndicateur) as subquery where subquery.row_num = 1 order by totalValeur desc
""",
            nativeQuery = true)
    List<Object[]> findLatestSummedValuesByIndicateurForAllApplications(
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
    select subquery.id_Module, subquery.date, subquery.totalValeur from (select id_Module , date, sum(tf.valeur) over (partition by tf.id_Module, tf.date order by tf.date desc) as totalValeur,
     row_number() over (partition by tf.id_Module order by tf.date desc) as row_num FROM Table_Faits tf WHERE tf.id_Indicateur = :idIndicateur and id_Module is not null) as subquery where subquery.row_num = 1 order by totalValeur desc
""",
            nativeQuery = true)
    List<Object[]> findLatestSummedValuesByIndicateurForAllModules(
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
                    SELECT dv.id_application, AVG(dv.valeur) AS sumValeur
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
                                           id_application,
                                           id_indicateur,
                                           valeur,
                                           date,
                                           ROW_NUMBER() OVER (PARTITION BY id_module, id_application, id_indicateur ORDER BY date DESC) AS rn
                                       FROM
                                           table_faits
                                   )
                                   SELECT
                                      id_module AS moduleId,
                                      MAX(CASE WHEN id_indicateur = 1 THEN valeur END) AS nbLigneCode,
                                      MAX(CASE WHEN id_indicateur = 2 THEN valeur END) AS nbLigneCodeNonTeste,
                                      MAX(CASE WHEN id_indicateur = 3 THEN valeur END) AS nbCveCritical,
                                      MAX(CASE WHEN id_indicateur = 4 THEN valeur END) AS nbCveHigh,
                                      MAX(CASE WHEN id_indicateur = 5 THEN valeur END) AS nbCveMedium,
                                      MAX(CASE WHEN id_indicateur = 6 THEN valeur END) AS nbCveLow,
                                      MAX(CASE WHEN id_indicateur = 11 THEN valeur END) AS detteTechnique,
                                      MAX(CASE WHEN id_indicateur = 12 THEN valeur END) AS fiabilite
                                   FROM
                                       latest_data
                                   WHERE
                                       rn = 1 and id_module is not null
                                   GROUP BY
                                       id_module,
                                       id_application;
""",
            nativeQuery = true)
    List<Object[]> findValueIndicateurQualiteBrute();
}
