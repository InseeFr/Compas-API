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
    SELECT *
          FROM (
               SELECT *,
               ROW_NUMBER() OVER (PARTITION BY id_module ORDER BY date DESC, id DESC) AS row_num
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
                    SELECT dv.id_application, AVG(dv.valeur) AS sumValeur
                    FROM DernieresValeurs dv
                    GROUP BY dv.id_application;
                    """,
            nativeQuery = true)
    List<Object[]> findAggregatedAvgResults(Integer idIndicateur);
}
