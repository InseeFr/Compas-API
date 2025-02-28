package fr.insee.compas.model.compas;

import lombok.Getter;

@Getter
public enum SourceType {
    SONAR(0),
    OSCAR(1),
    SAISIE_MANUELLE(2),
    GITLAB(3),
    FICHIER_VM(101);

    private final int value;

    SourceType(int value) {
        this.value = value;
    }
}
