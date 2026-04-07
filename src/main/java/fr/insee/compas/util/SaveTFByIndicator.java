package fr.insee.compas.util;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.repository.TableFaitsRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SaveTFByIndicator {
    private final TableFaitsRepository tableFaitsRepository;

    @Transactional
    public void saveByIndicator(
            Integer idModule,
            Integer idApplication,
            IndicateurType type,
            BigDecimal valeur,
            SourceType sourceType) {
        TableFaits fait =
                TableFaits.builder()
                        .idModule(idModule)
                        .idApplication(idApplication)
                        .idIndicateur(type.getValue())
                        .valeur(valeur)
                        .idSource(sourceType.getValue())
                        .date(LocalDate.now())
                        .build();
        tableFaitsRepository.save(fait);
    }
}
