package org.cloudbus.cloudsimdisk.examples;

import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;

/**
 * Created by sai on 20/10/16.
 */
public class MyExample3 {

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     * @throws Exception
     */

    public static void main(String[] args) throws Exception {

        // Parameters
        String nameOfTheSimulation = "Basic Example 3"; // name of the simulation
        String requestArrivalRateType = "basic"; // type of the workload
        String requestArrivalTimesSource = "basic/example3/ex3RequestArrivalDistri.txt"; // time distribution
        int numberOfRequest = 10; // Number of requests (MAX: 9)
        String requiredFiles = "basic/example3/ex3RequiredFiles.txt"; // No files required
        String dataFiles = "basic/example3/ex3DataFiles.txt"; // dataFiles Names and Sizes
        String startingFilesList = ""; // No files to start
        int numberOfDisk = 1; // 1 HDD
        StorageModelHdd hddModel = MyConstants.STORAGE_MODEL_HDD; // model of disks in the persistent storage
        PowerModelHdd hddPowerModel = MyConstants.STORAGE_POWER_MODEL_HDD; // power model of disks

        // Execution
        new MyRunner(nameOfTheSimulation, requestArrivalRateType, numberOfRequest, requestArrivalTimesSource,
                requiredFiles, dataFiles, startingFilesList, numberOfDisk, hddModel, hddPowerModel);
    }


}
