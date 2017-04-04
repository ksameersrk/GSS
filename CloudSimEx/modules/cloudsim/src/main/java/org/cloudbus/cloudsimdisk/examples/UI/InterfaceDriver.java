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

        startSimulation(totalNoOfNodes, addStagingDisk);
    }
}
