package fr.insee.compas.dto;

public record HomologationDto(
        Integer applicationId,
        String nomApp,
        String sensitivity,
        String statutHomologation,
        String homologationSI,
        String homologationBeginDate,
        String homologationEndDate,
        String homologationRemarks) {}
