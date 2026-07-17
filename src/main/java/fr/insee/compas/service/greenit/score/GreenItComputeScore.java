package fr.insee.compas.service.greenit.score;

import org.springframework.stereotype.Component;

import fr.insee.compas.dto.green.GreenVmDto;
import fr.insee.compas.model.greenit.GreenItScore;
import fr.insee.compas.service.greenit.properties.GreenItScoreConfigProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("greenItScore")
public class GreenItComputeScore extends GreenItAbstractScore {

    private final GreenItScoreConfigProperties config;

    public GreenItComputeScore(GreenItScoreConfigProperties greenItScoreConfigProperties) {
        super();
        this.config = greenItScoreConfigProperties;
    }

    @Override
    public GreenItScore computeAppScore(GreenVmDto indicator) {
        return computeScore(
                indicator,
                this.config.getApplication().getConsoMax(),
                this.config.getApplication().getPressionMaxRam(),
                this.config.getApplication().getPressionMaxCpu(),
                this.config.getApplication().getPressionMaxDisk());
    }
}
