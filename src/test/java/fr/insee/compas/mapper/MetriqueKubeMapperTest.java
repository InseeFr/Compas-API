package fr.insee.compas.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insee.compas.model.greenit.MetriqueKube;
import fr.insee.compas.model.greenit.MetriqueKubeCsvRead;

class MetriqueKubeMapperTest {

    private MetriqueKubeMapper kubeMapper;

    @BeforeEach
    void setUp() {
        kubeMapper = new MetriqueKubeMapper();
    }

    @Test
    void toMetriqueKube_whenCsvRawIsValid() {
        final MetriqueKubeCsvRead metriqueKubeCsvRead =
                MetriqueKubeCsvRead.builder()
                        .namespace("projet-toto")
                        .cpuUsed("54")
                        .ramUsed("1024")
                        .pvcUsed("2048")
                        .s3Used("512")
                        .build();

        Optional<MetriqueKube> optMetriqueKube = kubeMapper.toMetriqueKube(metriqueKubeCsvRead);
        MetriqueKube metriqueKube = optMetriqueKube.get();

        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(optMetriqueKube).isPresent();
        softAssertions.assertThat(metriqueKube.getNamespace()).isEqualTo("projet-toto");
        softAssertions.assertThat(metriqueKube.getCpuUsed()).isEqualTo("54.0");
        softAssertions.assertThat(metriqueKube.getRamUsed()).isEqualTo("1024.0");
        softAssertions.assertThat(metriqueKube.getPvcUsed()).isEqualTo("2048.0");
        softAssertions.assertThat(metriqueKube.getS3Used()).isEqualTo("512.0");
        softAssertions.assertAll();
    }

    @Test
    void toVMetriqueKube_shouldReturnEmpty_whenInputIsNull() {
        final Optional<MetriqueKube> result = kubeMapper.toMetriqueKube(null);
        assertTrue(result.isEmpty());
    }
}
