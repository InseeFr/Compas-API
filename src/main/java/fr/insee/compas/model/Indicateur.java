package fr.insee.compas.model;

import lombok.Getter;

@Getter
public enum Indicateur {
    NBR_LIGNE(1),
    NBR_LIGNE_TEST(2),
    NBR_JOUR_MEP(301),
    RAM_ALLOUEE(101);

    private final int value;

    Indicateur(int value) {
        this.value = value;
    }
}
