package fr.insee.compas.mapper;

import java.util.Optional;

import org.springframework.stereotype.Component;

import fr.insee.compas.model.greenit.MetriqueVm;
import fr.insee.compas.model.greenit.MetriqueVmCsvRead;
import fr.insee.compas.model.greenit.util.LectureCsvUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MetriqueVmMapper {

    public Optional<MetriqueVm> toMetriqueVm(MetriqueVmCsvRead csvRead) {
        log.debug("construction du Pojo métrique");
        return Optional.ofNullable(csvRead).map(this::csvToPojo);
    }

    private MetriqueVm csvToPojo(MetriqueVmCsvRead ligne) {
        return MetriqueVm.builder()
                .vm(ligne.getVm())
                .ramAllocated(LectureCsvUtil.process(ligne.getRamAllocated()))
                .ramMaxi(LectureCsvUtil.process(ligne.getRamMaxi()))
                .diskAllocated(LectureCsvUtil.process(ligne.getDiskAllocated()))
                .diskUsed(LectureCsvUtil.process(ligne.getDiskUsed()))
                .cpuAllocated(LectureCsvUtil.process(ligne.getCpuAllocated()))
                .cpuMaxi(LectureCsvUtil.process(ligne.getCpuMaxi()))
                .conso(LectureCsvUtil.process(ligne.getConso()))
                .build();
    }
}
