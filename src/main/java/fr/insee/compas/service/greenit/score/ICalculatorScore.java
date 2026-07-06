package fr.insee.compas.service.greenit.score;

public interface ICalculatorScore<U, T> {
    T computeAppScore(U indicator);
}
