/*******************************************************************************
 * Title: CloudSimDisk
 * Description: a module for energy aware storage simulation in CloudSim
 * Author: Baptiste Louis
 * Date: June 2015
 *
 * Address: baptiste_louis@live.fr
 * Source: https://github.com/Udacity2048/CloudSimDisk
 * Website: http://baptistelouis.weebly.com/projects.html
 *
 * Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2015, Lule� University of Technology, Sweden.
 *******************************************************************************/
package org.cloudbus.cloudsimdisk.examples;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;
import org.cloudbus.cloudsimdisk.util.WriteToLogFile;
import org.cloudbus.cloudsimdisk.util.WriteToResultFile;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A Runner to run storage examples.
 * 
 * @author Baptiste Louis
 * 
 */
public class MyRunner {

	/**
	 * An Helper for the Runner.
	 */
	public Helper	helper				= new Helper();

	/**
	 * End Time of the simulation.
	 */
	public double	endTimeSimulation	= 0.0;

	/**
	 * Number of processing units [Host, VM, PE]
	 */
	public static int numberOfProcessingUnits = 1;

	/**
	 * set numberOfProcessingUnits
	 */
	public static void setNumberOfProcessingUnits(int num)
	{
		MyRunner.numberOfProcessingUnits = num;
	}

	/**
	 * Create a Runner to run a MyExampleX scenario.
	 * 
	 * @param name
	 * @param type
	 * @param NumberOfRequest
	 * @param RequestArrivalDistri
	 * @param requiredFiles
	 * @param dataFiles
	 * @param startingFilesList
	 * @param NumberOfDisk
	 * @param hddModel 
	 * @param hddPowerModel 
	 * @throws Exception
	 */
	public MyRunner(String name, String type, int NumberOfRequest, String RequestArrivalDistri, String requiredFiles,
			String dataFiles, String startingFilesList, int NumberOfDisk, StorageModelHdd hddModel,
			PowerModelHdd hddPowerModel) throws Exception {

		// BEGIN
		Log.printLine("Starting simulation \"" + name + "\"\n");
		WriteToLogFile.AddtoFile("Starting simulation \"" + name + "\"\n");
		WriteToResultFile.init();

		init(NumberOfRequest, type, RequestArrivalDistri, requiredFiles, dataFiles, startingFilesList, NumberOfDisk,
				hddModel, hddPowerModel);
		start();
		print();

		WriteToResultFile.end();
		Log.printLine("END !");
		// END
	}

	public MyRunner(HashMap<Node, Tasks> simulation, String arrivalFile, String dataFile, ArrayList<Node> seq) throws Exception
    {
        Log.printLine("Starting simulation \n");
        WriteToLogFile.AddtoFile("Starting simulation \n");
        WriteToResultFile.init();

        //-----------------INIT-----------------//
        helper.initCloudSim();
        helper.createBroker("basic", arrivalFile);
        helper.createPeList(numberOfProcessingUnits);
        helper.createHostList(numberOfProcessingUnits);
        helper.createVmList(numberOfProcessingUnits);
        helper.createPersistentStorage(simulation.keySet());
        helper.createDatacenterCharacteristics();
        helper.createDatacenter();

        // Files
        helper.addFiles("");
        helper.createRequiredFilesList("");
        helper.createDataFilesList(dataFile);

        // Cloudlets
        helper.createCloudletList(simulation, seq);

        // Logs
        helper.printPersistenStorageDetails();
        //--------------INIT END----------------//


        start();
        print();

        WriteToResultFile.end();
        Log.printLine("END !");
    }

	public MyRunner(HashMap<Node, Tasks> simulation, String arrivalFile, String putDataFile, String getDataFile, String updateDataFile, String deleteDataFile, ArrayList<Node> seq) throws Exception
	{
		Log.printLine("Starting simulation \n");
		WriteToLogFile.AddtoFile("Starting simulation \n");
		WriteToResultFile.init();

		//-----------------INIT-----------------//
		helper.initCloudSim();
		helper.createBroker("basic", arrivalFile);
		helper.createPeList(numberOfProcessingUnits);
		helper.createHostList(numberOfProcessingUnits);
		helper.createVmList(numberOfProcessingUnits);
		helper.createPersistentStorage(simulation.keySet());
		helper.createDatacenterCharacteristics();
		helper.createDatacenter();

		// Files
		helper.addFiles("");
		helper.createRequiredFilesList(getDataFile);
		helper.createDataFilesList(putDataFile);
		helper.createUpdateFilesList(updateDataFile);
		helper.createDeleteFilesList(deleteDataFile);

		// Cloudlets
		helper.createCloudletList(simulation, seq);

		// Logs
		helper.printPersistenStorageDetails();
		//--------------INIT END----------------//


		start();
		print();

		WriteToResultFile.end();
		Log.printLine("END !");
	}

	/**
	 * Initialize the simulation.
	 * 
	 * @param NumberOfRequest
	 *            the number of request
	 * @param type
	 *            type of distribution
	 * @param RequestArrivalDistri
	 *            the request distribution
	 * @param requiredFiles
	 * @param dataFiles
	 * @param startingFilesList
	 * @param NumberOfDisk
	 * @param hddModel 
	 * @param hddPowerModel 
	 * @throws Exception
	 */
	public void init(int NumberOfRequest, String type, String RequestArrivalDistri, String requiredFiles,
			String dataFiles, String startingFilesList, int NumberOfDisk, StorageModelHdd hddModel,
			PowerModelHdd hddPowerModel) throws Exception {

		// Entities
		helper.initCloudSim();
		helper.createBroker(type, RequestArrivalDistri);
		helper.createPeList(numberOfProcessingUnits);
		helper.createHostList(numberOfProcessingUnits);
		helper.createVmList(numberOfProcessingUnits);
		helper.createPersistentStorage(NumberOfDisk, hddModel, hddPowerModel);
		helper.createDatacenterCharacteristics();
		helper.createDatacenter();

		// Files
		helper.addFiles(startingFilesList);
		helper.createRequiredFilesList(requiredFiles);
		helper.createDataFilesList(dataFiles);

		// Cloudlets
		helper.createCloudletList(NumberOfRequest);

		// Logs
		helper.printPersistenStorageDetails();
	}

	/**
	 * Start the simulation.
	 */
	public void start() {
		endTimeSimulation = CloudSim.startSimulation();
	}

	/**
	 * Print the Results.
	 */
	public void print() {
		helper.printResults(endTimeSimulation);
	}

	public double getTotalStorageEnergyConsumed()
	{
		double TotalStorageEnergy = helper.getTotalStorageEnergyConsumed();
		return TotalStorageEnergy ;
	}
}
