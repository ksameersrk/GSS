package org.cloudbus.cloudsimdisk.examples.UI;

/**
 * Created by skulkarni9 on 4/9/17.
 */
public class InputJSONObject
{
    Integer totalNoOfNodes;
    Integer scenario;
    Integer numberOfOperations;
    Integer predefindedWorkloadNumber;
    Integer noOfReplicas;
    String cachingMechanism;
    Integer hddDiskType;
    Integer ssdDiskType;
    String workloadType;
    String manualTextarea;

    public Integer getTotalNoOfNodes() {
        return totalNoOfNodes;
    }

    public void setTotalNoOfNodes(Integer totalNoOfNodes) {
        this.totalNoOfNodes = totalNoOfNodes;
    }

    public Integer getScenario() {
        return scenario;
    }

    public void setScenario(Integer scenario) {
        this.scenario = scenario;
    }

    public Integer getNumberOfOperations() {
        return numberOfOperations;
    }

    public void setNumberOfOperations(Integer numberOfOperations) {
        this.numberOfOperations = numberOfOperations;
    }

    public Integer getPredefindedWorkloadNumber() {
        return predefindedWorkloadNumber;
    }

    public void setPredefindedWorkloadNumber(Integer predefindedWorkloadNumber) {
        this.predefindedWorkloadNumber = predefindedWorkloadNumber;
    }

    public Integer getNoOfReplicas() {
        return noOfReplicas;
    }

    public void setNoOfReplicas(Integer noOfReplicas) {
        this.noOfReplicas = noOfReplicas;
    }

    public String getCachingMechanism() {
        return cachingMechanism;
    }

    public void setCachingMechanism(String cachingMechanism) {
        this.cachingMechanism = cachingMechanism;
    }

    public Integer getHddDiskType() {
        return hddDiskType;
    }

    public void setHddDiskType(Integer hddDiskType) {
        this.hddDiskType = hddDiskType;
    }

    public Integer getSsdDiskType() {
        return ssdDiskType;
    }

    public void setSsdDiskType(Integer sddDiskType) {
        this.ssdDiskType = sddDiskType;
    }

    public String getWorkloadType() {
        return workloadType;
    }

    public void setWorkloadType(String workloadType) {
        this.workloadType = workloadType;
    }

    public String getManualTextarea() {
        return manualTextarea;
    }

    public void setManualTextarea(String manualTextarea) {
        this.manualTextarea = manualTextarea;
    }
}
