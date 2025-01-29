package fr.insee.compas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.insee.compas.model.compas.ModuleOscar;

public interface ModuleOscarRepository extends JpaRepository<ModuleOscar, Integer> {

    @Modifying
    @Query(
            value =
                    "INSERT INTO module_oscar (id_module) VALUES (:id) "
                            + "ON CONFLICT (id_module) DO UPDATE SET id_module = :id, actif = true",
            nativeQuery = true)
    void upsertProduct(@Param("id") Integer id);

    @Modifying
    @Query(value = "UPDATE  ModuleOscar m set m.actif=false ")
    void desactivateAllModules();

    List<ModuleOscar> findByActif(boolean actif);
}
