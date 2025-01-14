package fr.insee.compas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.insee.compas.model.compas.TableFaits;

public interface IndicateurRepository extends JpaRepository<TableFaits, Integer> {
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
                        and tf.idIndicateur = :indicateur
                    """)
    List<TableFaits> findLatestValuesForAllModules(int indicateur);

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
            @Param("idModule") Integer idModule, @Param("idIndicateur") Integer idIndicateur);
}
