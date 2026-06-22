package fr.insee.compas.service.securite;

import static fr.insee.compas.util.security.CalculSecurityUtils.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.IndicateurSecuriteRepository;
import fr.insee.compas.repository.projection.SecuriteProjection;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.conversion.ConversionService;
import fr.insee.compas.view.IndicateurSecuriteView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IndicateurSecuriteService implements IIndicateurSecuriteService {

    private final ConversionService conversionService;
    private final IndicateurSecuriteRepository indicateurSecuriteRepository;
    private final OscarService oscarService;

    public List<IndicateurSecuriteView> getIndicateursApplicationView(
            Date dateReference, Date datePassee) {
        List<SecuriteProjection> current =
                indicateurSecuriteRepository.findValueBruteApplication(dateReference);
        Map<Integer, SecuriteProjection> past =
                indexById(indicateurSecuriteRepository.findValueBruteApplication(datePassee));

        return current.stream()
                .map(projection -> buildApplicationView(projection, past.get(projection.getId())))
                .collect(Collectors.toList());
    }

    public List<IndicateurSecuriteView> getIndicateursModuleView(
            Date dateReference, Date datePassee) {
        List<SecuriteProjection> current =
                indicateurSecuriteRepository.findValueBruteModule(dateReference);
        Map<Integer, SecuriteProjection> past =
                indexById(indicateurSecuriteRepository.findValueBruteModule(datePassee));
        Map<Integer, Module> moduleMap = indexById(oscarService.getModules(), Module::getId);

        return current.stream()
                .map(
                        projection ->
                                buildModuleView(
                                        projection,
                                        past.get(projection.getId()),
                                        moduleMap.get(projection.getId())))
                .collect(Collectors.toList());
    }

    private IndicateurSecuriteView buildApplicationView(
            SecuriteProjection current, SecuriteProjection past) {
        return buildAndFinalize(
                buildCommonCveFields(current, past)
                        .applicationId(current.getId())
                        .moduleId(null)
                        .nbVmNonMaj(returnStringOfAnInteger(current.getNbVmNonMaj()))
                        .vmCountPast(
                                past != null ? returnStringOfAnInteger(past.getNbVmNonMaj()) : null)
                        .delaiVmNonMiseAjour(returnStringOfAnInteger(current.getDelaiMaj()))
                        .delaiVmNonMiseAJourPast(
                                past != null ? returnStringOfAnInteger(past.getDelaiMaj()) : null)
                        .lettreCve(resolveLettreCve(current))
                        .lettreMajVm(resolveLettreVm(current)));
    }

    private IndicateurSecuriteView buildModuleView(
            SecuriteProjection current, SecuriteProjection past, Module module) {
        return buildAndFinalize(
                buildCommonCveFields(current, past)
                        .moduleId(current.getId())
                        .applicationId(module != null ? module.getIdApplication() : null)
                        .lettreCve(resolveLettreCve(current))
                        .lettreMajVm("NR")
                        .vmCountPast(null));
    }

    private IndicateurSecuriteView.IndicateurSecuriteViewBuilder buildCommonCveFields(
            SecuriteProjection current, SecuriteProjection past) {
        return IndicateurSecuriteView.builder()
                .nbCveCritical(returnStringOfAnInteger(current.getNbCveCritical()))
                .nbCveHigh(returnStringOfAnInteger(current.getNbCveHigh()))
                .nbCveMedium(returnStringOfAnInteger(current.getNbCveMedium()))
                .nbCveLow(returnStringOfAnInteger(current.getNbCveLow()))
                .nbCveCriticalPast(
                        past != null ? returnStringOfAnInteger(past.getNbCveCritical()) : null)
                .nbCveHighPast(past != null ? returnStringOfAnInteger(past.getNbCveHigh()) : null)
                .nbCveMediumPast(
                        past != null ? returnStringOfAnInteger(past.getNbCveMedium()) : null)
                .nbCveLowPast(past != null ? returnStringOfAnInteger(past.getNbCveLow()) : null);
    }

    @SuppressWarnings("java:S1854")
    private IndicateurSecuriteView buildAndFinalize(
            IndicateurSecuriteView.IndicateurSecuriteViewBuilder builder) {
        IndicateurSecuriteView view = builder.build();
        view.calculerLettreGlobaleSecurite();
        return view;
    }

    private String resolveLettreCve(SecuriteProjection projection) {
        if (!hasCveOnProjection(projection)) return "NR";
        double score =
                getCalculIndicateurCve(
                        projection.getNbCveCritical(),
                        projection.getNbCveHigh(),
                        projection.getNbCveMedium(),
                        projection.getNbCveLow());
        return conversionService.convertNiveauCveEnLettre(score);
    }

    private String resolveLettreVm(SecuriteProjection projection) {
        return projection.getNbVmNonMaj() != null
                ? conversionService.convertNbVmNonMiseAJour(projection.getNbVmNonMaj())
                : "NR";
    }

    private static Map<Integer, SecuriteProjection> indexById(
            List<SecuriteProjection> projections) {
        return projections.stream()
                .collect(Collectors.toMap(SecuriteProjection::getId, Function.identity()));
    }

    private static <T> Map<Integer, T> indexById(List<T> items, Function<T, Integer> idExtractor) {
        return items.stream().collect(Collectors.toMap(idExtractor, Function.identity()));
    }
}
