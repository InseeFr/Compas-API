package fr.insee.compas.service.securite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.IndicateurSecuriteRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.conversion.ConversionService;
import fr.insee.compas.view.IndicateurSecuriteView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IndicateurSecuriteService {
    private final ConversionService conversionService;
    private final IndicateurSecuriteRepository indicateurSecuriteRepository;
    private final OscarService oscarService;

    public List<IndicateurSecuriteView> getIndicateursApplicationView() {
        List<Object[]> rawData = indicateurSecuriteRepository.findValueBruteApplication();
        List<IndicateurSecuriteView> result = new ArrayList<>();

        for (Object[] row : rawData) {
            Integer idApp = toInteger(row[0]);

            boolean hasCveData =
                    row[1] != null || row[2] != null || row[3] != null || row[4] != null;

            String lettreCve = "NR";
            String lettreVm = "NR";
            Integer nbVmNonMaj = null;
            Integer delaiMaj = null;
            if (hasCveData) {
                double niveau =
                        getCalculIndicateurCve(
                                toInteger(row[1]),
                                toInteger(row[2]),
                                toInteger(row[3]),
                                toInteger(row[4]));
                lettreCve = conversionService.convertNiveauCveEnLettre(niveau);
            }
            if (row[5] != null) {
                nbVmNonMaj = toInteger(row[5]);
                lettreVm = conversionService.convertNbVmNonMiseAJour(nbVmNonMaj);
            }
            if (row[6] != null) {
                delaiMaj = toInteger(row[6]);
            }

            IndicateurSecuriteView view =
                    IndicateurSecuriteView.builder()
                            .applicationId(idApp)
                            .moduleId(null)
                            .nbCveCritical(toString(row[1]))
                            .nbCveHigh(toString(row[2]))
                            .nbCveMedium(toString(row[3]))
                            .nbCveLow(toString(row[4]))
                            .nbVmNonMaj(toString(row[5]))
                            .lettreCve(lettreCve)
                            .nbVmNonMaj(nbVmNonMaj != null ? String.valueOf(nbVmNonMaj) : "")
                            .delaiVmNonMiseAjour(delaiMaj != null ? String.valueOf(delaiMaj) : "")
                            .lettreMajVm(lettreVm)
                            .build();
            view.calculerLettreGlobaleSecurite();

            result.add(view);
        }

        return result;
    }

    public List<IndicateurSecuriteView> getIndicateursModuleView() {
        List<Object[]> rawData = indicateurSecuriteRepository.findValueBruteModule();
        List<Module> modules = oscarService.getModules();
        Map<Integer, Module> moduleMap =
                modules.stream().collect(Collectors.toMap(Module::getId, m -> m));

        List<IndicateurSecuriteView> result = new ArrayList<>();

        for (Object[] row : rawData) {
            Integer moduleId = toInteger(row[0]);
            Module mod = moduleMap.get(moduleId);

            boolean hasCveData =
                    row[1] != null && row[2] != null && row[3] != null && row[4] != null;

            String lettre = null;
            if (hasCveData) {
                double niveau =
                        getCalculIndicateurCve(
                                toInteger(row[1]),
                                toInteger(row[2]),
                                toInteger(row[3]),
                                toInteger(row[4]));
                lettre = conversionService.convertNiveauCveEnLettre(niveau);
            }
            IndicateurSecuriteView view =
                    IndicateurSecuriteView.builder()
                            .moduleId(moduleId)
                            .applicationId(mod != null ? mod.getIdApplication() : null)
                            .nbCveCritical(toString(row[1]))
                            .nbCveHigh(toString(row[2]))
                            .nbCveMedium(toString(row[3]))
                            .nbCveLow(toString(row[4]))
                            .lettreCve(lettre)
                            .lettreMajVm("NR")
                            .build();
            view.calculerLettreGlobaleSecurite();
            result.add(view);
        }

        return result;
    }

    private Integer toInteger(Object obj) {
        return obj != null ? ((Number) obj).intValue() : null;
    }

    private String toString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    public double getCalculIndicateurCve(Integer c, Integer e, Integer m, Integer f) {
        int somme = c * 1000 + e * 100 + m * 10 + f + 1;
        return Math.log10(somme);
    }
}
