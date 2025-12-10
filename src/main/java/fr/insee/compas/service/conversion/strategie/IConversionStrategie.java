package fr.insee.compas.service.conversion.strategie;

public interface IConversionStrategie<U, V> {
    U conversion(V label);
}
