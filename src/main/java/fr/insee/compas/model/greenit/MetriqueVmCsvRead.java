package fr.insee.compas.model.greenit;

import com.opencsv.bean.CsvBindByName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetriqueVmCsvRead {
    @CsvBindByName(column = "vmName")
    private String vm;

    @CsvBindByName(column = "diskAllocated")
    private String diskAllocated;

    @CsvBindByName(column = "diskUsed")
    private String diskUsed;

    @CsvBindByName(column = "vCpu")
    private String vCpu;

    @CsvBindByName(column = "cpuAllocated")
    private String cpuAllocated;

    @CsvBindByName(column = "cpuMaxi")
    private String cpuMaxi;

    @CsvBindByName(column = "ramAllocated")
    private String ramAllocated;

    @CsvBindByName(column = "ramMaxi")
    private String ramMaxi;

    @CsvBindByName(column = "conso")
    private String conso;
}
