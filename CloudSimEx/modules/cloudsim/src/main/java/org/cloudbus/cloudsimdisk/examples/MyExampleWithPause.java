package org.cloudbus.cloudsimdisk.examples;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;

/**
 * Created by ksameersrk on 6/1/17.
 */
public class MyExampleWithPause {
    public static void main(String[] args) throws Exception {

        // IF THESE 2 VARIABLES ARE NOT INITIALIZED THEN SIMULATION WONT BE PAUSED
        CloudSim.lifeLength = 100;
        CloudSim.pauseInterval = 30;

        Runnable monitor = new Runnable() {
            @Override
            public void run() {
                while (CloudSim.clock() <= CloudSim.lifeLength ) {
                    if (CloudSim.isPaused())
                    {
                        double clock = CloudSim.clock();
                        System.out.println("Simulation Paused. Now resuming at " + clock);
                        CloudSim.resumeSimulation();
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        Thread resumingThread = new Thread(monitor);
        resumingThread.setDaemon(true);
        resumingThread.start();

        // Parameters
        String nameOfTheSimulation = "My Example with Pause"; // name of the simulation
        String requestArrivalRateType = "basic"; // type of the workload
        String requestArrivalTimesSource = "basic/exampleWithPause/ex1RequestArrivalDistri.txt"; // time distribution
        int numberOfRequest = 9; // Number of requests (MAX: 9)
        String requiredFiles = ""; // No files required
        String dataFiles = "basic/exampleWithPause/ex1DataFiles.txt"; // dataFiles Names and Sizes
        String startingFilesList = ""; // No files to start
        int numberOfDisk = 3; // 1 HDD
        StorageModelHdd hddModel = MyConstants.STORAGE_MODEL_HDD; // model of disks in the persistent storage
        PowerModelHdd hddPowerModel = MyConstants.STORAGE_POWER_MODEL_HDD; // power model of disks

        // Execution
        new MyRunner(nameOfTheSimulation, requestArrivalRateType, numberOfRequest, requestArrivalTimesSource,
                requiredFiles, dataFiles, startingFilesList, numberOfDisk, hddModel, hddPowerModel);
    }
}
