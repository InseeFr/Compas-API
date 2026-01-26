package fr.insee.compas.service.securite;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.insee.compas.repository.IndicateurSecuriteRepository;
import fr.insee.compas.view.IndicateurApplicationSecuriteMonthly;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CveCriticalMonthlyService {

    private final IndicateurSecuriteRepository repo;

    public List<IndicateurApplicationSecuriteMonthly> getMonthly() {
        List<Object[]> rows = repo.findMonthlyCriticalByApplication();
        List<IndicateurApplicationSecuriteMonthly> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Integer appId = (Integer) r[0];
            LocalDate month = (LocalDate) r[1];
            BigDecimal val = (BigDecimal) r[2];
            out.add(
                    new IndicateurApplicationSecuriteMonthly(
                            appId, month, val == null ? null : val.intValue()));
        }
        return out;
    }
}
