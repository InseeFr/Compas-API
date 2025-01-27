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
                        )
                    """)
    List<TableFaits> findLatestValuesForAllModules();

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
                            AND tf.idIndicateur = :idIndicateur
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
}
