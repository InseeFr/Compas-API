package fr.insee.compas.view;

public class IndicateurApplicationGreenITView {
    private Integer applicationId;
    private String applicationName;
    private Integer ramUsed;
    private Integer ramAllocated;
    private Integer cpuUsed;
    private Integer cpuAllocated;
    private Integer diskUsed;
    private Integer diskAllocated;
    private Integer nbVm;

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Integer getNbVm() {
        return nbVm;
    }

    public void setNbVm(Integer nbVm) {
        this.nbVm = nbVm;
    }

    public Integer getRamUsed() {
        return ramUsed;
    }

    public void setRamUsed(Integer ramUsed) {
        this.ramUsed = ramUsed;
    }

    public Integer getRamAllocated() {
        return ramAllocated;
    }

    public void setRamAllocated(Integer ramAllocated) {
        this.ramAllocated = ramAllocated;
    }

    public Integer getCpuUsed() {
        return cpuUsed;
    }

    public void setCpuUsed(Integer cpuUsed) {
        this.cpuUsed = cpuUsed;
    }

    public Integer getCpuAllocated() {
        return cpuAllocated;
    }

    public void setCpuAllocated(Integer cpuAllocated) {
        this.cpuAllocated = cpuAllocated;
    }

    public Integer getDiskUsed() {
        return diskUsed;
    }

    public void setDiskUsed(Integer diskUsed) {
        this.diskUsed = diskUsed;
    }

    public Integer getDiskAllocated() {
        return diskAllocated;
    }

    public void setDiskAllocated(Integer diskAllocated) {
        this.diskAllocated = diskAllocated;
    }
}
