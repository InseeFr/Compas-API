package fr.insee.compas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.insee.compas.model.compas.ApplicationTip;

public interface ApplicationTipsRepository extends JpaRepository<ApplicationTip, Long> {
    List<ApplicationTip> findAllByNomOscarIgnoreCaseOrderByDateDescIdDesc(String nomOscar);
}
