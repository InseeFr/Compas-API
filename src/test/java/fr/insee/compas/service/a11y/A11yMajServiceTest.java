package fr.insee.compas.service.a11y;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.a11y.InfosSaisiesA11yEntity;
import fr.insee.compas.model.a11y.InfosSaisiesA11yToSaveDTO;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.InfosSaisiesA11yRepository;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.SonarService;
import fr.insee.compas.util.observer.EventTypeObserver;
import fr.insee.compas.util.observer.IEventManager;

@ExtendWith(MockitoExtension.class)
class A11yMajServiceTest {

    @Mock private InfosSaisiesA11yRepository infosSaisiesA11yRepository;

    @Mock private SonarService sonarService;

    @Mock private OscarService oscarService;

    @Mock private TableFaitsRepository tableFaitsRepository;

    @Mock private IEventManager eventManager;

    @InjectMocks private A11yMajService a11yMajService;

    private InfosSaisiesA11yToSaveDTO infosSaisiesA11yToSaveDTO;
    private InfosSaisiesA11yEntity infosSaisiesA11yEntity;
    private List<Module> modules;

    @BeforeEach
    void setUp() {
        infosSaisiesA11yToSaveDTO = new InfosSaisiesA11yToSaveDTO();
        infosSaisiesA11yToSaveDTO.setIdModule(1);
        infosSaisiesA11yToSaveDTO.setDateMajInfosSaisies(LocalDate.now());
        infosSaisiesA11yToSaveDTO.setIsDeclaration(true);
        infosSaisiesA11yToSaveDTO.setIdIndicateurTypeAudit(1);
        infosSaisiesA11yToSaveDTO.setScoreAudit(90);
        infosSaisiesA11yToSaveDTO.setDateAudit(LocalDate.now());
        infosSaisiesA11yToSaveDTO.setDateDeclaration(LocalDate.now());

        infosSaisiesA11yEntity =
                InfosSaisiesA11yEntity.builder()
                        .id(1L)
                        .idModule(infosSaisiesA11yToSaveDTO.getIdModule())
                        .dateMajInfosSaisies(infosSaisiesA11yToSaveDTO.getDateMajInfosSaisies())
                        .isDeclaration(infosSaisiesA11yToSaveDTO.getIsDeclaration())
                        .idIndicateurTypeAudit(infosSaisiesA11yToSaveDTO.getIdIndicateurTypeAudit())
                        .scoreAudit(infosSaisiesA11yToSaveDTO.getScoreAudit())
                        .dateAudit(infosSaisiesA11yToSaveDTO.getDateAudit())
                        .dateDeclaration(infosSaisiesA11yToSaveDTO.getDateDeclaration())
                        .build();

        Module module1 = new Module();
        module1.setId(1);
        module1.setIdApplication(1);
        module1.setTypeLivrable("IHM");
        module1.setKeySonar("key1");
        module1.setModName("Module1");

        Module module2 = new Module();
        module2.setId(2);
        module2.setIdApplication(2);
        module2.setTypeLivrable("IHM");
        module2.setKeySonar("key2");
        module2.setModName("Module2");

        modules = Arrays.asList(module1, module2);
    }

    @Test
    void testMajInfosSaisiesA11y() {
        when(infosSaisiesA11yRepository.save(any(InfosSaisiesA11yEntity.class)))
                .thenReturn(infosSaisiesA11yEntity);

        long result = a11yMajService.majInfosSaisiesA11y(infosSaisiesA11yToSaveDTO);

        assertThat(result).isEqualTo(1L);
        verify(infosSaisiesA11yRepository, times(1)).save(any(InfosSaisiesA11yEntity.class));
    }

    @Test
    void testGetNbIssueSonarAccessibility_Success() {
        when(oscarService.getModules()).thenReturn(modules);
        when(sonarService.getNbIssueSonarAccessibility("key1", "gitlab", "Module1"))
                .thenReturn("0");
        when(sonarService.getNbIssueSonarAccessibility("key1", "github", "Module1"))
                .thenReturn("5");
        when(sonarService.getNbIssueSonarAccessibility("key2", "gitlab", "Module2"))
                .thenReturn("10");

        a11yMajService.getNbIssueSonarAccessibility();

        verify(tableFaitsRepository, times(2)).save(any(TableFaits.class));
        verify(eventManager, never()).notifyObservers(any(), anyString());
    }

    @Test
    void testGetNbIssueSonarAccessibility_Exception() {
        when(oscarService.getModules()).thenReturn(modules);
        when(sonarService.getNbIssueSonarAccessibility("key1", "gitlab", "Module1"))
                .thenThrow(new RuntimeException("Erreur Sonar"));
        when(sonarService.getNbIssueSonarAccessibility("key2", "gitlab", "Module2"))
                .thenReturn("10");

        a11yMajService.getNbIssueSonarAccessibility();

        verify(tableFaitsRepository, times(1)).save(any(TableFaits.class));
        verify(eventManager, times(1))
                .notifyObservers(eq(EventTypeObserver.EVENT_TYPE_ERROR), anyString());
    }

    @Test
    void testGetIndicateutA11y() {
        List<InfosSaisiesA11yEntity> metric = Collections.singletonList(infosSaisiesA11yEntity);
        when(infosSaisiesA11yRepository.findLatestinfosSaisiesA11yForAllModules())
                .thenReturn(metric);

        Map<Integer, InfosSaisiesA11yEntity> result = a11yMajService.getIndicateutA11y();

        assertThat(result).hasSize(1);
        assertThat(result.get(1)).isEqualTo(infosSaisiesA11yEntity);
    }
}
