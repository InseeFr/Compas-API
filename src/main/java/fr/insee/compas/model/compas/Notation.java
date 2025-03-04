package fr.insee.compas.model.compas;

import lombok.Getter;

@Getter
public enum Notation {
    A("A"),
    B("B"),
    C("C"),
    D("D"),
    E("E"),
    X("X"), // pour le cas où la distance est -1
    NR("NR"), // pour les cas où l'indicateur est non renseigné
    SO("SO"); // pour les cas où l'indicateur est sans objet

    private final String grade;

    Notation(String grade) {
        this.grade = grade;
    }
}
