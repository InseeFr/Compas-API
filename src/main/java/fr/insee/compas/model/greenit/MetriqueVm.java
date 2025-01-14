package fr.insee.compas.model.greenit;

import com.opencsv.bean.CsvBindByName;

public class MetriqueVm {
    @CsvBindByName(column = "vmName")
    private String vm;

    @CsvBindByName(column = "diskAllocated")
    private Integer diskAllocated;

    @CsvBindByName(column = "diskUsed")
    private Integer diskUsed;

    @CsvBindByName(column = "diskDecimalUsed")
    private Integer diskDecimalUsed;

    @CsvBindByName(column = "cpu")
    private Integer cpuAllocated;

    @CsvBindByName(column = "ram")
    private Integer ramAllocated;

    public MetriqueVm() {
        super();
    }

    public MetriqueVm(
            String vm,
            Integer diskAllocated,
            Integer diskUsed,
            Integer diskDecimalUsed,
            Integer cpuAllocated,
            Integer ramAllocated) {
        super();
        this.vm = vm;
        this.diskAllocated = diskAllocated;
        this.diskUsed = diskUsed;
        this.diskDecimalUsed = diskDecimalUsed;
        this.cpuAllocated = cpuAllocated;
        this.ramAllocated = ramAllocated;
    }

    public String getVm() {
        return vm;
    }

    public void setVm(String vm) {
        this.vm = vm;
    }

    public Integer getDiskAllocated() {
        return diskAllocated;
    }

    public void setDiskAllocated(Integer diskAllocated) {
        this.diskAllocated = diskAllocated;
    }

    public Integer getDiskUsed() {
        return diskUsed;
    }

    public void setDiskUsed(Integer diskUsed) {
        this.diskUsed = diskUsed;
    }

    public Integer getDiskDecimalUsed() {
        return diskDecimalUsed;
    }

    public void setDiskDecimalUsed(Integer diskDecimalUsed) {
        this.diskDecimalUsed = diskDecimalUsed;
    }

    public Integer getCpuAllocated() {
        return cpuAllocated;
    }

    public void setCpuAllocated(Integer cpuAllocated) {
        this.cpuAllocated = cpuAllocated;
    }

    public Integer getRamAllocated() {
        return ramAllocated;
    }

    public void setRamAllocated(Integer ramAllocated) {
        this.ramAllocated = ramAllocated;
    }
}
