package fr.insee.compas.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MetriqueModuleProjection {
    Integer getIdModule();

    LocalDate getDate();

    BigDecimal getTotalValeur();
}
