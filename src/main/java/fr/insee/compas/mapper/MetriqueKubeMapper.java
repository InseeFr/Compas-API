package fr.insee.compas.mapper;

import java.util.Optional;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.greenit.MetriqueKube;
import fr.insee.compas.model.greenit.MetriqueKubeCsvRead;
import fr.insee.compas.model.greenit.util.LectureCsvUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MetriqueKubeMapper {

    public Optional<MetriqueKube> toMetriqueKube(MetriqueKubeCsvRead csvRead) {
        log.debug("construction du Pojo métrique");
        return Optional.ofNullable(csvRead).map(this::csvToPojo);
    }

    private MetriqueKube csvToPojo(MetriqueKubeCsvRead ligne) {
        return MetriqueKube.builder()
                .namespace(ligne.getNamespace())
                .namespaceOwner(ligne.getNamespaceOwner())
                .cpuUsed(LectureCsvUtil.processKube(ligne.getCpuUsed()))
                .ramUsed(LectureCsvUtil.processKube(ligne.getRamUsed()))
                .s3Used(LectureCsvUtil.processKube(ligne.getS3Used()))
                .pvcUsed(LectureCsvUtil.processKube(ligne.getPvcUsed()))
                .nbPodMaxi(LectureCsvUtil.processKube(ligne.getNbPodMaxi()))
                .environnement(ligne.getEnvironnement())
                .build();
    }
}
