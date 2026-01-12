package fr.insee.compas.service.a11y;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.model.a11y.IndicateursModuleA11Y;
import fr.insee.compas.model.a11y.InfosSaisiesA11yEntity;
import fr.insee.compas.model.compas.Notation;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;

@ExtendWith(MockitoExtension.class)
class A11yAffichageServiceTest {

    @Mock OscarService oscarService;
    @Mock A11yMajService a11yMajService;
    @Mock TableFaitsService tableFaitsService;
    @InjectMocks A11yAffichageService service;

    private fr.insee.compas.model.oscar.Module module(
            int id, String name, String domaine, String sndi) {
        fr.insee.compas.model.oscar.Module m = mock(fr.insee.compas.model.oscar.Module.class);
        when(m.getId()).thenReturn(id);
        when(m.getIdApplication()).thenReturn(100 + id); // utile si le service l'utilise
        when(m.getModName()).thenReturn(name);
        when(m.getDomaineSndi()).thenReturn(domaine);
        when(m.getSndi()).thenReturn(sndi);
        return m;
    }

    private InfosSaisiesA11yEntity infos(boolean declaration, int typeAuditId, float score) {
        InfosSaisiesA11yEntity i = mock(InfosSaisiesA11yEntity.class);
        when(i.getIsDeclaration()).thenReturn(declaration);
        when(i.getIdIndicateurTypeAudit()).thenReturn(typeAuditId);
        when(i.getScoreAudit()).thenReturn(score);
        return i;
    }

    @Test
    @DisplayName("listerModulesA11y: sans infos -> modules par défaut avec Notation.NR")
    void listerModulesA11y_defaultNR_whenNoInfos() {
        fr.insee.compas.model.oscar.Module m1 = module(1, "M1", "DomA", "SNDI-A");
        fr.insee.compas.model.oscar.Module m2 = module(2, "M2", "DomB", "SNDI-B");
        when(oscarService.getModulesIhm()).thenReturn(List.of(m1, m2));

        // Pas d'infos A11y ni d'issues Sonar
        when(a11yMajService.getIndicateutA11y()).thenReturn(Map.of());
        when(tableFaitsService.getMapMetricByModule(anyInt())).thenReturn(Map.of());

        List<IndicateursModuleA11Y> out = service.listerModulesA11y();

        assertThat(out).hasSize(2);
        // module 1
        IndicateursModuleA11Y v1 = out.getFirst();
        assertThat(v1.getIdModule()).isEqualTo(1);
        assertThat(v1.getModName()).isEqualTo("M1");
        assertThat(v1.getDomaineSndi()).isEqualTo("DomA");
        assertThat(v1.getSndi()).isEqualTo("SNDI-A");
        assertThat(v1.getNotation()).isEqualTo(Notation.NR);
        // module 2
        IndicateursModuleA11Y v2 = out.get(1);
        assertThat(v2.getIdModule()).isEqualTo(2);
        assertThat(v2.getNotation()).isEqualTo(Notation.NR);
    }

    @Test
    @DisplayName(
            "listerModulesA11y: avec infos -> mapping des champs + libellé type d'audit + notation")
    void listerModulesA11y_withInfos_mappingAndNotation() {
        fr.insee.compas.model.oscar.Module m1 = module(1, "M1", "DomA", "SNDI-A");
        fr.insee.compas.model.oscar.Module m2 = module(2, "M2", "DomB", "SNDI-B");
        fr.insee.compas.model.oscar.Module m3 = module(3, "M3", "DomC", "SNDI-C");
        fr.insee.compas.model.oscar.Module m4 = module(4, "M4", "DomD", "SNDI-D");
        when(oscarService.getModulesIhm()).thenReturn(List.of(m1, m2, m3, m4));

        // Cas 1: pas de déclaration -> H (peu importe type/score), libellé issu de 512
        InfosSaisiesA11yEntity i1 = infos(false, 512, 0f);
        // Cas 2: déclaration + type 511 (partiel) score<50 -> F / libellé "Audit partiel"
        InfosSaisiesA11yEntity i2 = infos(true, 511, 49.9f);
        // Cas 3: déclaration + type 513 (complet externe) score=92 -> Notation.B (75 <= score <
        // 100)
        InfosSaisiesA11yEntity i3 = infos(true, 513, 92f);
        // Cas 4: déclaration + type inconnu 999 -> libellé "Inconnu"
        InfosSaisiesA11yEntity i4 = infos(true, 999, 80f);

        Map<Integer, InfosSaisiesA11yEntity> map = new HashMap<>();
        map.put(1, i1);
        map.put(2, i2);
        map.put(3, i3);
        map.put(4, i4);

        when(a11yMajService.getIndicateutA11y()).thenReturn(map);
        when(tableFaitsService.getMapMetricByModule(anyInt())).thenReturn(Map.of());

        List<IndicateursModuleA11Y> out = service.listerModulesA11y();

        assertThat(out).hasSize(4);

        // Module 1
        IndicateursModuleA11Y v1 =
                out.stream().filter(v -> v.getIdModule().equals(1)).findFirst().orElseThrow();
        assertThat(v1.getIsDeclaration()).isFalse();
        assertThat(v1.getTypeAuditId()).isEqualTo(512);
        assertThat(v1.getTypeAuditLibelle()).isEqualTo("Audit complet");
        assertThat(v1.getNotation()).isEqualTo(Notation.H);

        // Module 2
        IndicateursModuleA11Y v2 =
                out.stream().filter(v -> v.getIdModule().equals(2)).findFirst().orElseThrow();
        assertThat(v2.getIsDeclaration()).isTrue();
        assertThat(v2.getTypeAuditId()).isEqualTo(511);
        assertThat(v2.getTypeAuditLibelle()).isEqualTo("Audit partiel");
        assertThat(v2.getScoreAudit()).isEqualTo(49.9f);
        assertThat(v2.getNotation()).isEqualTo(Notation.F);

        // Module 3
        IndicateursModuleA11Y v3 =
                out.stream().filter(v -> v.getIdModule().equals(3)).findFirst().orElseThrow();
        assertThat(v3.getTypeAuditId()).isEqualTo(513);
        assertThat(v3.getTypeAuditLibelle()).isEqualTo("Audit complet externe");
        assertThat(v3.getNotation()).isEqualTo(Notation.B);

        // Module 4
        IndicateursModuleA11Y v4 =
                out.stream().filter(v -> v.getIdModule().equals(4)).findFirst().orElseThrow();
        assertThat(v4.getTypeAuditId()).isEqualTo(999);
        assertThat(v4.getTypeAuditLibelle()).isEqualTo("Inconnu");
    }

    @Test
    @DisplayName("listerModulesA11y: déclaration + type 510 -> G")
    void listerModulesA11y_type510_returnsG() {
        fr.insee.compas.model.oscar.Module m = module(10, "M10", "DomX", "SNDI-X");
        when(oscarService.getModulesIhm()).thenReturn(List.of(m));

        InfosSaisiesA11yEntity info = infos(true, 510, 12.3f);
        when(a11yMajService.getIndicateutA11y()).thenReturn(Map.of(10, info));
        when(tableFaitsService.getMapMetricByModule(anyInt())).thenReturn(Map.of());

        List<IndicateursModuleA11Y> out = service.listerModulesA11y();
        assertThat(out).hasSize(1);
        assertThat(out.getFirst().getNotation()).isEqualTo(Notation.G);
        assertThat(out.getFirst().getTypeAuditLibelle()).isEqualTo("Aucun audit");
    }
}
