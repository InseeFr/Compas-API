package fr.insee.compas.service.maturitecloud.indicateur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.dto.DemandeCreationStrategieCloud;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;

@ExtendWith(MockitoExtension.class)
class CloudCreationServiceTest {

    @Mock private TableFaitsRepository tableFaitsRepository;
    @Mock private OscarService oscarService;

    @InjectMocks private CloudCreationService cloudCreationService;

    private DemandeCreationStrategieCloud buildDemande(BigDecimal avancement, BigDecimal envCible) {
        return DemandeCreationStrategieCloud.builder()
                .idsModule(List.of(10, 20))
                .avancement(avancement)
                .envCibleProd(envCible)
                .commentaire("Test")
                .date(LocalDate.of(2026, 2, 25))
                .build();
    }

    @Test
    void creerStrategieCloud_creation_nominale() {
        Module m2 = Module.builder().id(20).idApplication(3).build();
        Module m = Module.builder().id(10).idApplication(3).build();
        when(oscarService.getModules()).thenReturn(List.of(m, m2));

        DemandeCreationStrategieCloud demande =
                buildDemande(new BigDecimal("2"), new BigDecimal("1"));

        when(tableFaitsRepository.findByIdIndicateurAndIdModule(anyInt(), anyInt()))
                .thenReturn(List.of());

        when(tableFaitsRepository.save(any())).thenReturn(TableFaits.builder().id(1L).build());

        List<Long> ids = cloudCreationService.creerStrategieCloud(demande);

        assertThat(ids).hasSize(4);
        verify(tableFaitsRepository, times(4)).save(any());
    }

    @Test
    void creerStrategieCloud_mise_a_jour_si_existant() {

        DemandeCreationStrategieCloud demande =
                buildDemande(new BigDecimal("3"), new BigDecimal("2"));

        TableFaits existing = TableFaits.builder().id(100L).valeur(BigDecimal.ONE).build();

        when(tableFaitsRepository.findByIdIndicateurAndIdModule(anyInt(), anyInt()))
                .thenReturn(List.of(existing));

        when(tableFaitsRepository.save(any())).thenReturn(existing);

        List<Long> ids = cloudCreationService.creerStrategieCloud(demande);

        assertThat(ids).hasSize(4);
    }

    @Test
    void creerStrategieCloud_sauvegarde_les_bonnes_valeurs() {

        DemandeCreationStrategieCloud demande =
                DemandeCreationStrategieCloud.builder()
                        .idsModule(List.of(10))
                        .avancement(new BigDecimal("2"))
                        .envCibleProd(new BigDecimal("3"))
                        .commentaire("Mon commentaire")
                        .date(LocalDate.of(2026, 2, 25))
                        .build();

        Module m = Module.builder().id(10).idApplication(3).build();
        when(oscarService.getModules()).thenReturn(List.of(m));
        when(tableFaitsRepository.findByIdIndicateurAndIdModule(anyInt(), anyInt()))
                .thenReturn(List.of());

        when(tableFaitsRepository.save(any()))
                .thenAnswer(
                        inv -> {
                            TableFaits t = inv.getArgument(0);
                            t.setId(1L);
                            return t;
                        });

        cloudCreationService.creerStrategieCloud(demande);

        verify(tableFaitsRepository)
                .save(
                        argThat(
                                t ->
                                        t.getIdModule().equals(10)
                                                && t.getIdIndicateur().equals(614)
                                                && t.getValeur().compareTo(new BigDecimal("2")) == 0
                                                && "Mon commentaire".equals(t.getCommentaire())));

        verify(tableFaitsRepository)
                .save(
                        argThat(
                                t ->
                                        t.getIdModule().equals(10)
                                                && t.getIdIndicateur().equals(613)
                                                && t.getValeur().compareTo(new BigDecimal("3"))
                                                        == 0));
    }

    @Test
    void creerStrategieCloud_commentaire_null_accepte() {

        DemandeCreationStrategieCloud demande =
                DemandeCreationStrategieCloud.builder()
                        .idsModule(List.of(132))
                        .avancement(new BigDecimal("2"))
                        .envCibleProd(new BigDecimal("1"))
                        .commentaire(null)
                        .date(LocalDate.now())
                        .build();

        Module m = Module.builder().id(132).idApplication(3).build();
        when(oscarService.getModules()).thenReturn(List.of(m));
        when(tableFaitsRepository.findByIdIndicateurAndIdModule(anyInt(), anyInt()))
                .thenReturn(List.of());

        when(tableFaitsRepository.save(any())).thenReturn(TableFaits.builder().id(1L).build());

        List<Long> ids = cloudCreationService.creerStrategieCloud(demande);

        assertThat(ids).hasSize(2);
    }
}
