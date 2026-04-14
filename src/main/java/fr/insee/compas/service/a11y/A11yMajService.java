package fr.insee.compas.service.a11y;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.a11y.InfosSaisiesA11yEntity;
import fr.insee.compas.model.a11y.InfosSaisiesA11yToSaveDTO;
import fr.insee.compas.model.compas.IndicateurType;
import fr.insee.compas.model.compas.TableFaits;
import fr.insee.compas.model.oscar.Module;
import fr.insee.compas.repository.InfosSaisiesA11yRepository;
import fr.insee.compas.repository.TableFaitsRepository;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.SonarService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class A11yMajService {

    private InfosSaisiesA11yRepository infosSaisiesA11yRepository;
    private SonarService sonarService;
    private OscarService oscarService;
    private TableFaitsRepository tableFaitsRepository;

    public long majInfosSaisiesA11y(InfosSaisiesA11yToSaveDTO infosSaisiesA11yToSaveDTO) {

        InfosSaisiesA11yEntity infosSaisiesA11yEntity =
                InfosSaisiesA11yEntity.builder()
                        .idModule(infosSaisiesA11yToSaveDTO.getIdModule())
                        .dateMajInfosSaisies(infosSaisiesA11yToSaveDTO.getDateMajInfosSaisies())
                        .isDeclaration(infosSaisiesA11yToSaveDTO.getIsDeclaration())
                        .idIndicateurTypeAudit(infosSaisiesA11yToSaveDTO.getIdIndicateurTypeAudit())
                        .scoreAudit(infosSaisiesA11yToSaveDTO.getScoreAudit())
                        .dateAudit(infosSaisiesA11yToSaveDTO.getDateAudit())
                        .dateDeclaration(infosSaisiesA11yToSaveDTO.getDateDeclaration())
                        .build();

        return infosSaisiesA11yRepository.save(infosSaisiesA11yEntity).getId();
    }

    public void getNbIssueSonarAccessibility() {
        List<Module> modules = oscarService.getModules();
        LocalDate now = LocalDate.now();
        for (Module module : modules) {
            if (module.getTypeLivrable()!=null && module.getKeySonar()!=null && module.getTypeLivrable().contains("IHM")
                    && !"null".equals(module.getKeySonar())
                    && !module.getKeySonar().equals("Sans objet")) {
                String issue =
                        sonarService.getNbIssueSonarAccessibility(
                                module.getKeySonar(), "gitlab", module.getModName());
                // Si c'est O ca peut être un 'vrai' 0 ou une analyse qui est sur github
                if (issue != null && issue.equals("0")) {
                    issue =
                            sonarService.getNbIssueSonarAccessibility(
                                    module.getKeySonar(), "github", module.getModName());
                }
                if (StringUtils.isNotEmpty(issue)) {
                    log.info("Sauvegarde des nb issues sonar dans la table");
                    tableFaitsRepository.save(
                            TableFaits.builder()
                                    .idModule(module.getId())
                                    .idApplication(module.getIdApplication())
                                    .idIndicateur(IndicateurType.ISSUE_ACCESSIBILITY.getValue())
                                    .date(now)
                                    .valeur(new BigDecimal(issue))
                                    .idSource(0)
                                    .build());
                }
            }
        }
    }

    public Map<Integer, InfosSaisiesA11yEntity> getIndicateutA11y() {
        List<InfosSaisiesA11yEntity> metric =
                infosSaisiesA11yRepository.findLatestinfosSaisiesA11yForAllModules();
        return metric.stream()
                .collect(
                        Collectors.toMap(
                                InfosSaisiesA11yEntity::getIdModule, // Clé : idModule
                                infosSaisiesA11yEntity -> infosSaisiesA11yEntity // Valeur : l'objet
                                // TableFaits
                                ));
    }
}
