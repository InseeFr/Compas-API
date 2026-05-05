package fr.insee.compas.service.securite;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.SourceType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.hyperx.IndicateurRecuperationSecuriteVM;
import fr.insee.compas.model.oscar.Application;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.HyperxService;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.util.observer.EventTypeObserver;
import fr.insee.compas.util.observer.IEventManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecupHyperxSecuriteService {
    private final TableFaitsRepository tableFaitsRepository;
    private final HyperxService hyperxService;
    private final OscarService oscarService;
    private final IEventManager eventObserverManager;

    public void updateDonneesVmNonMiseAjourDansDelaiParHyperX() {
        List<Application> applications = oscarService.getApplications();

        for (Application application : applications) {
            try {
                IndicateurRecuperationSecuriteVM indicateur =
                        hyperxService.maxMajVm(application.getAppName().trim());

                TableFaits max =
                        TableFaits.builder()
                                .idModule(null)
                                .idApplication(application.getIdApplication())
                                .idIndicateur(IndicateurType.DELAI_MAJ_VM.getValue())
                                .valeur(BigDecimal.valueOf(indicateur.getMax()))
                                .idSource(SourceType.HYPERX.getValue())
                                .commentaire("")
                                .date(LocalDate.now())
                                .build();

                TableFaits nb =
                        TableFaits.builder()
                                .idModule(null)
                                .idApplication(application.getIdApplication())
                                .idIndicateur(IndicateurType.NB_VM_NON_MISES_A_JOUR.getValue())
                                .valeur(BigDecimal.valueOf(indicateur.getNb()))
                                .idSource(SourceType.HYPERX.getValue())
                                .commentaire("")
                                .date(LocalDate.now())
                                .build();

                tableFaitsRepository.save(max);
                tableFaitsRepository.save(nb);

            } catch (Exception e) {
                log.error(
                        "Erreur application {} : {}",
                        application.getIdApplication(),
                        e.getMessage());
                eventObserverManager.notifyObservers(
                        EventTypeObserver.EVENT_TYPE_ERROR,
                        "RecupHyperx : Erreur application "
                                + application.getIdApplication()
                                + " : "
                                + e.getMessage());
            }
        }
    }
}
