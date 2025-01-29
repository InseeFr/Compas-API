package fr.insee.compas.model;

import lombok.Getter;

@Getter
public enum Source {
    SONAR(0),
    OSCAR(1),
    SAISIE_MANUELLE(2),
    GITLAB(3),
    FICHIER_VM(101);

    private final int value;

    Source(int value) {
        this.value = value;
    }
}
