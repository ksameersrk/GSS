package org.cloudbus.cloudsimdisk.examples;

import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;

/**
 * Created by sai on 20/10/16.
 */

/** to read a file (which is already stored on disk before the simulation
 * is started using startingFilesList argument)
 */

public class MyExample2 {


    /**
     * The main method.
     *
     * @param args
     *            the arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Parameters
        String nameOfTheSimulation = "Basic Example 2"; // name of the simulation
        String requestArrivalRateType = "basic"; // type of the workload
        String requestArrivalTimesSource = "basic/example0/ex0RequestArrivalDistri.txt"; // time distribution
        int numberOfRequest = 1; // Number of requests
        String requiredFiles = "basic/example2/ex2RequiredFiles.txt"; // No files required
        String dataFiles = ""; // dataFile Name and Size
        String startingFilesList = "basic/example2/ex2DataFiles.txt"; // No files to start
        int numberOfDisk = 1; // Number of disk in the persistent storage
        StorageModelHdd hddModel = MyConstants.STORAGE_MODEL_HDD; // model of disks in the persistent storage
        PowerModelHdd hddPowerModel = MyConstants.STORAGE_POWER_MODEL_HDD; // power model of disks

        // Execution
        new MyRunner(nameOfTheSimulation, requestArrivalRateType, numberOfRequest, requestArrivalTimesSource,
                requiredFiles, dataFiles, startingFilesList, numberOfDisk, hddModel, hddPowerModel);
    }


}
