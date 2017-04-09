package org.cloudbus.cloudsimdisk.examples.UI;

/**
 * Created by skulkarni9 on 4/9/17.
 */
public class InputJSONObject
{
    Integer totalNoOfNodes;
    Boolean addStagingDisk;
    Integer numberOfOperations;
    String distribution;
    Integer predefindedWorkloadNumber;
    Integer noOfReplicas;
    String cachingMechanism;
    Integer diskType;

    public Integer getTotalNoOfNodes() {
        return totalNoOfNodes;
    }

    public void setTotalNoOfNodes(Integer totalNoOfNodes) {
        this.totalNoOfNodes = totalNoOfNodes;
    }

    public Boolean getAddStagingDisk() {
        return addStagingDisk;
    }

    public void setAddStagingDisk(Boolean addStagingDisk) {
        this.addStagingDisk = addStagingDisk;
    }

    public Integer getNumberOfOperations() {
        return numberOfOperations;
    }

    public void setNumberOfOperations(Integer numberOfOperations) {
        this.numberOfOperations = numberOfOperations;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
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

    public Integer getDiskType() {
        return diskType;
    }

    public void setDiskType(Integer diskType) {
        this.diskType = diskType;
    }
}
