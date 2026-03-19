package fr.insee.compas.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insee.compas.model.greenit.MetriqueVm;
import fr.insee.compas.model.greenit.MetriqueVmCsvRead;

class MetriqueVmMapperTest {

    private MetriqueVmMapper vmMapper;

    @BeforeEach
    void setUp() {
        vmMapper = new MetriqueVmMapper();
    }

    @Test
    void toMetriqueVm_whenCsvRawIsValid() {
        final MetriqueVmCsvRead metriqueVmCsvRead =
                MetriqueVmCsvRead.builder()
                        .vm("pdsirene4")
                        .conso("12")
                        .cpuAllocated("54")
                        .ramAllocated("1024")
                        .diskAllocated("2048")
                        .diskUsed("512")
                        .build();

        Optional<MetriqueVm> optMetriqueVm = vmMapper.toMetriqueVm(metriqueVmCsvRead);
        MetriqueVm metriqueVm = optMetriqueVm.get();

        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(optMetriqueVm).isPresent();
        softAssertions.assertThat(metriqueVm.getVm()).isEqualTo("pdsirene4");
        softAssertions.assertThat(metriqueVm.getConso()).isEqualTo("12.0");
        softAssertions.assertThat(metriqueVm.getCpuAllocated()).isEqualTo("54.0");
        softAssertions.assertThat(metriqueVm.getRamAllocated()).isEqualTo("1024.0");
        softAssertions.assertThat(metriqueVm.getDiskAllocated()).isEqualTo("2048.0");
        softAssertions.assertThat(metriqueVm.getDiskUsed()).isEqualTo("512.0");
        softAssertions.assertAll();
    }

    @Test
    void toVMetriqueVm_shouldReturnEmpty_whenInputIsNull() {
        final Optional<MetriqueVm> result = vmMapper.toMetriqueVm(null);
        assertTrue(result.isEmpty());
    }
}
