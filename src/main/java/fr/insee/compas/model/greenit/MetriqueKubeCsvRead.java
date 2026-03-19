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
public class MetriqueKubeCsvRead {
    @CsvBindByName(column = "namespace")
    private String namespace;

    @CsvBindByName(column = "namespace_owner")
    private String namespaceOwner;

    @CsvBindByName(column = "time_cpu_consumed")
    private String cpuUsed;

    @CsvBindByName(column = "avg_memory_used")
    private String ramUsed;

    @CsvBindByName(column = "s3_storage_used")
    private String s3Used;

    @CsvBindByName(column = "pvc_storage_used")
    private String pvcUsed;

    @CsvBindByName(column = "nb_pod_max")
    private String nbPodMaxi;

    @CsvBindByName(column = "env")
    private String environnement;
}
