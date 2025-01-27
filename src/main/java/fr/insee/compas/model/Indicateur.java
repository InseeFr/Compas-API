package fr.insee.compas.model;

import lombok.Getter;

@Getter
public enum Indicateur {
    NBR_LIGNE(1),
    NBR_LIGNE_TEST(2),
    RAM_ALLOUEE(201),
    RAM_MAXI(202),
    DISQUE_ALLOUE(203),
    DISQUE_CONSOMME(204),
    CPU_ALLOUEE(205),
    CPU_MAXI(206),
    CONSO_ELEC(207),
    NBR_VM(208),
    NBR_JOUR_MEP(301);

    private final int value;

    Indicateur(int value) {
        this.value = value;
    }
}
