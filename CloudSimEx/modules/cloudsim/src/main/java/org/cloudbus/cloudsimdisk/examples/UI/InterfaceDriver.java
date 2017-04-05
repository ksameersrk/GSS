package org.cloudbus.cloudsimdisk.examples.UI;

import static org.cloudbus.cloudsimdisk.examples.SimulationScenarios.FlushEntireStagingDiskContents.startSimulation;

/**
 * Created by spadigi on 4/4/17.
 */
public class InterfaceDriver {
    public static void main(String args[]) throws Exception{
        // node properties
        int totalNoOfNodes = Integer.parseInt(args[0]);

        // staging disk properties
        boolean addStagingDisk = Boolean.parseBoolean(args[1]);

        startSimulation(totalNoOfNodes, addStagingDisk);
    }
}
