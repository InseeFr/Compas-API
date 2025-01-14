package fr.insee.compas.model;

public enum Grade {
    A("A"),
    B("B"),
    C("C"),
    D("D"),
    E("E"),
    X("X"), // pour le cas où la distance est -1
    NR("NR"), // pour les cas où l'indicateur est non renseigné
    SO("SO"); // pour les cas où l'indicateur est sans objet

    private final String grade;

    Grade(String grade) {
        this.grade = grade;
    }

    public String getGrade() {
        return grade;
    }
}
