package org.cloudbus.cloudsimdisk.examples.UI;

import static org.cloudbus.cloudsimdisk.examples.SimulationScenarios.FlushEntireStagingDiskContents.startSimulation;

/**
 * Created by spadigi on 4/4/17.
 */
public class InterfaceDriver {
    public static void main(String args[]) throws Exception{
        // node properties
        int totalNoOfNodes = 16;

        // staging disk properties
        boolean addStagingDisk = true;

        int numberOfOperations = 10;
        String distribution = "read intensive";

        // will have a set of predefined workloads , user selects one of them,
        // predefindedWorkloadNumber variable stores the workload id
        int predefindedWorkloadNumber = 1;

        int noOfReplicas = 3; //default 3
        String cachingMechanism = "LRU"; // FIFO also possible
        int diskType = 1; // basicallly this number is the id for storage and power model, will assign ids to them
        //Scenarios : this part is to be done in front end
        startSimulation(totalNoOfNodes, addStagingDisk, numberOfOperations, predefindedWorkloadNumber, noOfReplicas, cachingMechanism, diskType);
    }
}
