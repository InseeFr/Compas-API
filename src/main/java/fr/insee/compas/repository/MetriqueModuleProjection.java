package fr.insee.compas.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

// mod
public interface MetriqueModuleProjection {
    Integer getIdModule();

    LocalDate getDate();

    BigDecimal getTotalValeur();
}
