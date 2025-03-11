package fr.insee.compas.service.a11y;

import org.springframework.stereotype.Service;

import fr.insee.compas.model.a11y.InfosSaisiesA11yEntity;
import fr.insee.compas.model.a11y.InfosSaisiesA11yToSaveDTO;
import fr.insee.compas.repository.InfosSaisiesA11yRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class A11yMajService {

    private InfosSaisiesA11yRepository infosSaisiesA11yRepository;

    public long majInfosSaisiesA11y(InfosSaisiesA11yToSaveDTO infosSaisiesA11yToSaveDTO) {

        InfosSaisiesA11yEntity infosSaisiesA11yEntity =
                InfosSaisiesA11yEntity.builder()
                        .idModule(infosSaisiesA11yToSaveDTO.getIdModule())
                        .dateMajInfosSaisies(infosSaisiesA11yToSaveDTO.getDateMajInfosSaisies())
                        .isDeclaration(infosSaisiesA11yToSaveDTO.isDeclaration())
                        .idIndicateurTypeAudit(infosSaisiesA11yToSaveDTO.getIdIndicateurTypeAudit())
                        .ScoreAudit(infosSaisiesA11yToSaveDTO.getScoreAudit())
                        .DateAudit(infosSaisiesA11yToSaveDTO.getDateAudit())
                        .build();

        return infosSaisiesA11yRepository.save(infosSaisiesA11yEntity).getId();
    }
}
