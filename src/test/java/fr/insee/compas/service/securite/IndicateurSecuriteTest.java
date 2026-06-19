package fr.insee.compas.service.securite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.IndicateurSecuriteRepository;
import fr.insee.compas.repository.projection.SecuriteProjection;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.conversion.ConversionService;
import fr.insee.compas.view.IndicateurSecuriteView;

@ExtendWith(MockitoExtension.class)
class IndicateurSecuriteServiceTest {

    @Mock private ConversionService conversionService;

    @Mock private IndicateurSecuriteRepository indicateurSecuriteRepository;

    @Mock private OscarService oscarService;

    @InjectMocks private IndicateurSecuriteService indicateurSecuriteService;

    private Date dateReference;
    private Date datePassee;

    @BeforeEach
    void setUp() throws InterruptedException {
        dateReference = new Date(2000_000_000L);
        datePassee = new Date(1000_000_000L);
    }

    @Nested
    class GetIndicateursApplicationView {

        @Test
        void shouldReturnEmptyList_whenNoDataAtDateReference() {
            when(indicateurSecuriteRepository.findValueBruteApplication(dateReference))
                    .thenReturn(Collections.emptyList());
            when(indicateurSecuriteRepository.findValueBruteApplication(datePassee))
                    .thenReturn(Collections.emptyList());

            List<IndicateurSecuriteView> result =
                    indicateurSecuriteService.getIndicateursApplicationView(
                            dateReference, datePassee);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldBuildViewCorrectly_whenCurrentAndPastDataExist() {
            SecuriteProjection current = mockProjection(1, 1, 2, 3, 4, 5, 10);
            SecuriteProjection past = mockProjection(1, 0, 1, 2, 3, 4, 8);

            when(indicateurSecuriteRepository.findValueBruteApplication(dateReference))
                    .thenReturn(List.of(current));
            when(indicateurSecuriteRepository.findValueBruteApplication(datePassee))
                    .thenReturn(List.of(past));
            when(conversionService.convertNiveauCveEnLettre(anyDouble())).thenReturn("B");
            when(conversionService.convertNbVmNonMiseAJour(5.0)).thenReturn("C");

            List<IndicateurSecuriteView> result =
                    indicateurSecuriteService.getIndicateursApplicationView(
                            dateReference, datePassee);

            assertThat(result).hasSize(1);
            IndicateurSecuriteView view = result.get(0);
            assertThat(view.getApplicationId()).isEqualTo(1);
            assertThat(view.getModuleId()).isNull();
            assertThat(view.getNbCveCritical()).isEqualTo("1");
            assertThat(view.getNbCveHigh()).isEqualTo("2");
            assertThat(view.getNbCveMedium()).isEqualTo("3");
            assertThat(view.getNbCveLow()).isEqualTo("4");
            assertThat(view.getNbVmNonMaj()).isEqualTo("5");
            assertThat(view.getLettreCve()).isEqualTo("B");
            assertThat(view.getLettreMajVm()).isEqualTo("C");
        }

        @Test
        void shouldPopulatePastValues_whenPastDataMatchesCurrentId() {
            SecuriteProjection current = mockProjection(1, 1, 2, 3, 4, 5, 10);
            SecuriteProjection past = mockProjection(1, 0, 1, 2, 3, 4, 8);

            when(indicateurSecuriteRepository.findValueBruteApplication(dateReference))
                    .thenReturn(List.of(current));
            when(indicateurSecuriteRepository.findValueBruteApplication(datePassee))
                    .thenReturn(List.of(past));
            when(conversionService.convertNiveauCveEnLettre(anyDouble())).thenReturn("B");
            when(conversionService.convertNbVmNonMiseAJour(5.0)).thenReturn("C");

            IndicateurSecuriteView view =
                    indicateurSecuriteService
                            .getIndicateursApplicationView(dateReference, datePassee)
                            .get(0);

            assertThat(view.getNbCveCriticalPast()).isEqualTo("0");
            assertThat(view.getNbCveHighPast()).isEqualTo("1");
            assertThat(view.getNbCveMediumPast()).isEqualTo("2");
            assertThat(view.getNbCveLowPast()).isEqualTo("3");
            assertThat(view.getVmCountPast()).isEqualTo("4");
            assertThat(view.getDelaiVmNonMiseAjour()).isEqualTo("10");
            assertThat(view.getDelaiVmNonMiseAJourPast()).isEqualTo("8");
        }

        @Test
        void shouldLeaveAllPastValuesNull_whenNoPastDataForId() {
            SecuriteProjection current = mockProjection(42, 1, 2, 3, 4, 5, 10);

            when(indicateurSecuriteRepository.findValueBruteApplication(dateReference))
                    .thenReturn(List.of(current));
            when(indicateurSecuriteRepository.findValueBruteApplication(datePassee))
                    .thenReturn(Collections.emptyList());
            when(conversionService.convertNiveauCveEnLettre(anyDouble())).thenReturn("B");
            when(conversionService.convertNbVmNonMiseAJour(5.0)).thenReturn("C");

            IndicateurSecuriteView view =
                    indicateurSecuriteService
                            .getIndicateursApplicationView(dateReference, datePassee)
                            .get(0);

            assertThat(view.getNbCveCriticalPast()).isNull();
            assertThat(view.getNbCveHighPast()).isNull();
            assertThat(view.getNbCveMediumPast()).isNull();
            assertThat(view.getNbCveLowPast()).isNull();
            assertThat(view.getVmCountPast()).isNull();
            assertThat(view.getDelaiVmNonMiseAJourPast()).isNull();
        }

        @Test
        void shouldSetLettreCveAndLettreMajVmToNR_whenAllCveAndVmDataAreNull() {
            SecuriteProjection current = mockProjection(1, null, null, null, null, null, null);

            when(indicateurSecuriteRepository.findValueBruteApplication(dateReference))
                    .thenReturn(List.of(current));
            when(indicateurSecuriteRepository.findValueBruteApplication(datePassee))
                    .thenReturn(Collections.emptyList());

            IndicateurSecuriteView view =
                    indicateurSecuriteService
                            .getIndicateursApplicationView(dateReference, datePassee)
                            .get(0);

            assertThat(view.getLettreCve()).isEqualTo("NR");
            assertThat(view.getLettreMajVm()).isEqualTo("NR");
        }

        @Test
        void shouldReturnMultipleViews_whenSeveralProjectionsExist() {
            // p1 et p2 ont des CVE et une VM non nulle
            SecuriteProjection p1 = mockProjection(1, 1, 0, 0, 0, 1, 0);
            SecuriteProjection p2 = mockProjection(2, 2, 0, 0, 0, 1, 0);
            // p3 : tout null → pas d'appel aux conversions
            SecuriteProjection p3 = mockProjection(3, null, null, null, null, null, null);

            when(indicateurSecuriteRepository.findValueBruteApplication(dateReference))
                    .thenReturn(List.of(p1, p2, p3));
            when(indicateurSecuriteRepository.findValueBruteApplication(datePassee))
                    .thenReturn(Collections.emptyList());
            when(conversionService.convertNiveauCveEnLettre(anyDouble())).thenReturn("B");
            when(conversionService.convertNbVmNonMiseAJour(1.0)).thenReturn("B");

            List<IndicateurSecuriteView> result =
                    indicateurSecuriteService.getIndicateursApplicationView(
                            dateReference, datePassee);

            assertThat(result).hasSize(3);
            assertThat(result)
                    .extracting(IndicateurSecuriteView::getApplicationId)
                    .containsExactlyInAnyOrder(1, 2, 3);
        }
    }

    @Nested
    class GetIndicateursModuleView {

        @Test
        void shouldReturnEmptyList_whenNoDataAtDateReference() {
            when(indicateurSecuriteRepository.findValueBruteModule(dateReference))
                    .thenReturn(Collections.emptyList());
            when(indicateurSecuriteRepository.findValueBruteModule(datePassee))
                    .thenReturn(Collections.emptyList());
            when(oscarService.getModules()).thenReturn(Collections.emptyList());

            List<IndicateurSecuriteView> result =
                    indicateurSecuriteService.getIndicateursModuleView(dateReference, datePassee);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldBuildViewWithApplicationId_whenModuleIsKnown() {
            SecuriteProjection projection = mockProjectionCveOnly(1, 2, 3, 4);

            Module module = mock(Module.class);
            when(module.getId()).thenReturn(10);
            when(module.getIdApplication()).thenReturn(99);

            when(indicateurSecuriteRepository.findValueBruteModule(dateReference))
                    .thenReturn(List.of(projection));
            when(indicateurSecuriteRepository.findValueBruteModule(datePassee))
                    .thenReturn(Collections.emptyList());
            when(oscarService.getModules()).thenReturn(List.of(module));
            when(conversionService.convertNiveauCveEnLettre(anyDouble())).thenReturn("B");

            List<IndicateurSecuriteView> result =
                    indicateurSecuriteService.getIndicateursModuleView(dateReference, datePassee);

            assertThat(result).hasSize(1);
            IndicateurSecuriteView view = result.get(0);
            assertThat(view.getModuleId()).isEqualTo(10);
            assertThat(view.getApplicationId()).isEqualTo(99);
        }

        @Test
        void shouldSetApplicationIdToNull_whenModuleIsUnknown() {
            SecuriteProjection projection = mockProjectionCveOnly(1, 2, 3, 4);

            when(indicateurSecuriteRepository.findValueBruteModule(dateReference))
                    .thenReturn(List.of(projection));
            when(indicateurSecuriteRepository.findValueBruteModule(datePassee))
                    .thenReturn(Collections.emptyList());
            when(oscarService.getModules()).thenReturn(Collections.emptyList());
            when(conversionService.convertNiveauCveEnLettre(anyDouble())).thenReturn("C");

            List<IndicateurSecuriteView> result =
                    indicateurSecuriteService.getIndicateursModuleView(dateReference, datePassee);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getApplicationId()).isNull();
        }

        @Test
        void shouldAlwaysSetLettreMajVmToNR_forModules() {
            SecuriteProjection projection = mockProjectionCveOnly(1, 2, 3, 4);

            when(indicateurSecuriteRepository.findValueBruteModule(dateReference))
                    .thenReturn(List.of(projection));
            when(indicateurSecuriteRepository.findValueBruteModule(datePassee))
                    .thenReturn(Collections.emptyList());
            when(oscarService.getModules()).thenReturn(Collections.emptyList());
            when(conversionService.convertNiveauCveEnLettre(anyDouble())).thenReturn("A");

            List<IndicateurSecuriteView> result =
                    indicateurSecuriteService.getIndicateursModuleView(dateReference, datePassee);

            assertThat(result).hasSize(1);
            IndicateurSecuriteView view = result.get(0);
            assertThat(view.getLettreMajVm()).isEqualTo("NR");
            assertThat(view.getVmCountPast()).isNull();
        }

        @Test
        void shouldSetLettreCveToNR_whenAllCveDataAreNull() {
            // Tous les CVE à null : pas d'appel à convertNiveauCveEnLettre
            SecuriteProjection projection = mockProjectionCveOnly(null, null, null, null);

            when(indicateurSecuriteRepository.findValueBruteModule(dateReference))
                    .thenReturn(List.of(projection));
            when(indicateurSecuriteRepository.findValueBruteModule(datePassee))
                    .thenReturn(Collections.emptyList());
            when(oscarService.getModules()).thenReturn(Collections.emptyList());

            List<IndicateurSecuriteView> result =
                    indicateurSecuriteService.getIndicateursModuleView(dateReference, datePassee);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLettreCve()).isEqualTo("NR");
        }

        @Test
        void shouldPopulatePastCveValues_whenPastDataMatchesModuleId() {
            SecuriteProjection current = mockProjectionCveOnly(1, 2, 3, 4);
            SecuriteProjection past = mockProjectionCveOnly(0, 1, 2, 3);

            when(indicateurSecuriteRepository.findValueBruteModule(dateReference))
                    .thenReturn(List.of(current));
            when(indicateurSecuriteRepository.findValueBruteModule(datePassee))
                    .thenReturn(List.of(past));
            when(oscarService.getModules()).thenReturn(Collections.emptyList());
            when(conversionService.convertNiveauCveEnLettre(anyDouble())).thenReturn("B");

            IndicateurSecuriteView view =
                    indicateurSecuriteService
                            .getIndicateursModuleView(dateReference, datePassee)
                            .get(0);

            assertThat(view.getNbCveCriticalPast()).isEqualTo("0");
            assertThat(view.getNbCveHighPast()).isEqualTo("1");
            assertThat(view.getNbCveMediumPast()).isEqualTo("2");
            assertThat(view.getNbCveLowPast()).isEqualTo("3");
        }
    }

    private SecuriteProjection mockProjection(
            int id,
            Integer critical,
            Integer high,
            Integer medium,
            Integer low,
            Integer nbVmNonMaj,
            Integer delaiMaj) {

        SecuriteProjection p = mock(SecuriteProjection.class);
        lenient().when(p.getId()).thenReturn(id);
        lenient().when(p.getNbCveCritical()).thenReturn(critical);
        lenient().when(p.getNbCveHigh()).thenReturn(high);
        lenient().when(p.getNbCveMedium()).thenReturn(medium);
        lenient().when(p.getNbCveLow()).thenReturn(low);
        lenient().when(p.getNbVmNonMaj()).thenReturn(nbVmNonMaj);
        lenient().when(p.getDelaiMaj()).thenReturn(delaiMaj);
        return p;
    }

    /**
     * Mock CVE uniquement (usage module — pas de données VM). lenient() car selon le scénario (CVE
     * tous null) les stubs ne seront pas tous lus.
     */
    private SecuriteProjection mockProjectionCveOnly(
            Integer critical, Integer high, Integer medium, Integer low) {

        SecuriteProjection p = mock(SecuriteProjection.class);
        lenient().when(p.getId()).thenReturn(10);
        lenient().when(p.getNbCveCritical()).thenReturn(critical);
        lenient().when(p.getNbCveHigh()).thenReturn(high);
        lenient().when(p.getNbCveMedium()).thenReturn(medium);
        lenient().when(p.getNbCveLow()).thenReturn(low);
        return p;
    }
}
