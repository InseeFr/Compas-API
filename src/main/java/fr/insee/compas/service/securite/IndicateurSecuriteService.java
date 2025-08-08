package fr.insee.compas.service.securite;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.IndicateurSecuriteRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.view.IndicateurSecuriteApplicationView;
import fr.insee.compas.view.IndicateurSecuriteModuleView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IndicateurSecuriteService {

    private final IndicateurSecuriteRepository indicateurSecuriteRepository;
    private final OscarService oscarService;

    public List<IndicateurSecuriteApplicationView> getIndicateursApplicationView() {
        List<Object[]> rawData = indicateurSecuriteRepository.findValueBruteApplication();
        List<Application> applications = oscarService.getApplications();
        Map<Integer, Application> appMap =
                applications.stream()
                        .collect(Collectors.toMap(Application::getIdApplication, a -> a));

        List<IndicateurSecuriteApplicationView> result = new ArrayList<>();

        for (Object[] row : rawData) {
            Integer idApp = toInteger(row[0]);
            Application app = appMap.get(idApp);

            boolean hasCveData =
                    row[1] != null || row[2] != null || row[3] != null || row[4] != null;

            String lettre = null;
            if (hasCveData) {
                double niveau =
                        getCalculIndicateurCve(
                                toInteger(row[1]),
                                toInteger(row[2]),
                                toInteger(row[3]),
                                toInteger(row[4]));
                lettre = convertNiveauCveEnLettre(niveau);
            }

            result.add(
                    IndicateurSecuriteApplicationView.builder()
                            .applicationId(idApp)
                            .applicationName(app != null ? app.getAppName() : null)
                            .nbCveCritical(toString(row[1]))
                            .nbCveHigh(toString(row[2]))
                            .nbCveMedium(toString(row[3]))
                            .nbCveLow(toString(row[4]))
                            .lettreSecurite(lettre)
                            .build());
        }

        return result;
    }

    public List<IndicateurSecuriteModuleView> getIndicateursModuleView() {
        List<Object[]> rawData = indicateurSecuriteRepository.findValueBruteModule();
        List<Module> modules = oscarService.getModules();
        Map<Integer, Module> moduleMap =
                modules.stream().collect(Collectors.toMap(Module::getId, m -> m));

        List<IndicateurSecuriteModuleView> result = new ArrayList<>();

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
                lettre = convertNiveauCveEnLettre(niveau);
            }

            result.add(
                    IndicateurSecuriteModuleView.builder()
                            .moduleId(moduleId)
                            .applicationId(mod != null ? mod.getIdApplication() : null)
                            .moduleName(mod != null ? mod.getModName() : null)
                            .applicationName(mod != null ? mod.getAppName() : null)
                            .nbCveCritical(toString(row[1]))
                            .nbCveHigh(toString(row[2]))
                            .nbCveMedium(toString(row[3]))
                            .nbCveLow(toString(row[4]))
                            .lettreSecurite(lettre)
                            .build());
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

    public String convertNiveauCveEnLettre(double niveau) {
        if (niveau >= 3) return Notation.E.getGrade();
        else if (niveau >= 2) return Notation.D.getGrade();
        else if (niveau >= 1) return Notation.C.getGrade();
        else if (niveau > 0) return Notation.B.getGrade();
        else return Notation.A.getGrade();
    }
}
