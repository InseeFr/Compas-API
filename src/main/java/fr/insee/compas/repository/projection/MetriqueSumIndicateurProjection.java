package fr.insee.compas.repository.projection;

import java.math.BigDecimal;

public interface MetriqueSumIndicateurProjection {
    Integer getIdIndicateur();

    BigDecimal getTotalValeur();
}
