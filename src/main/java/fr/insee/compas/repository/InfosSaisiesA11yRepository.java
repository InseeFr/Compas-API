package fr.insee.compas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.insee.compas.model.a11y.InfosSaisiesA11yEntity;

public interface InfosSaisiesA11yRepository extends JpaRepository<InfosSaisiesA11yEntity, Long> {

    @Query(
            value =
                    """
                        SELECT infos
                        FROM InfosSaisiesA11yEntity infos
                        WHERE infos.id IN (
                            SELECT sub.id
                            FROM InfosSaisiesA11yEntity sub
                            WHERE sub.idModule = infos.idModule
                            AND sub.dateMajInfosSaisies = (
                                SELECT MAX(sub2.dateMajInfosSaisies)
                                FROM InfosSaisiesA11yEntity sub2
                                WHERE sub2.idModule = infos.idModule
                            )
                            ORDER BY sub.dateMajInfosSaisies DESC, sub.id DESC
                        )
                    """)
    List<InfosSaisiesA11yEntity> findLatestinfosSaisiesA11yForAllModules();

    @Query(
            value =
                    """
                        SELECT infos
                        FROM InfosSaisiesA11yEntity infos
                        WHERE infos.dateMajInfosSaisies = (
                            SELECT MAX(infos2.dateMajInfosSaisies)
                            FROM InfosSaisiesA11yEntity infos2
                            WHERE infos2.idModule = :idModule
                            )
                         AND infos.idModule = :idModule
                         ORDER BY infos.dateMajInfosSaisies DESC, infos.id DESC
                    """)
    List<InfosSaisiesA11yEntity> findLatestinfosSaisiesA11yByModule(
            @Param("idModule") Integer idModule);
}
