package fr.insee.compas.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

// app
public interface MetriqueApplicationProjection {
    Integer getIdApplication();

    LocalDate getDate();

    BigDecimal getTotalValeur();
}
