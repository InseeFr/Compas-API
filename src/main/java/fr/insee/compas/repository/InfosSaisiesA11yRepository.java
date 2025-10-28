package fr.insee.compas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.insee.compas.model.a11y.InfosSaisiesA11yEntity;

public interface InfosSaisiesA11yRepository extends JpaRepository<InfosSaisiesA11yEntity, Long> {

    @Query(
            value =
                    """
                        SELECT infos
                        FROM InfosSaisiesA11yEntity infos
                        WHERE infos.id IN (
                            SELECT MAX(sub.id)
                            FROM InfosSaisiesA11yEntity sub
                            WHERE sub.idModule=infos.idModule
                            )
                        ORDER BY dateMajInfosSaisies DESC, id DESC
                    """)
    List<InfosSaisiesA11yEntity> findLatestinfosSaisiesA11yForAllModules();
}
