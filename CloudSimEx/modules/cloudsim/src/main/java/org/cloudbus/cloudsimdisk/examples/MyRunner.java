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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsimdisk.MyDatacenter;
import org.cloudbus.cloudsimdisk.examples.MyRing.MyNode;
import org.cloudbus.cloudsimdisk.examples.MyRing.MyRing;
import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;
import org.cloudbus.cloudsimdisk.util.WriteToLogFile;
import org.cloudbus.cloudsimdisk.util.WriteToResultFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		// WriteToLogFile.AddtoFile("Starting simulation \"" + name + "\"\n");
		//WriteToResultFile.init();

		init(NumberOfRequest, type, RequestArrivalDistri, requiredFiles, dataFiles, startingFilesList, NumberOfDisk,
				hddModel, hddPowerModel);
		start();
		print();

		//WriteToResultFile.end();
		Log.printLine("END !");
		// END
	}

	public MyRunner(HashMap<MyNode, Tasks> nodeToTaskMapping, String arrivalFile, String dataFile, ArrayList<MyNode> nodeList) throws Exception
    {
        Log.printLine("Starting simulation \n");
        // WriteToLogFile.AddtoFile("Starting simulation \n");
		//WriteToResultFile.init();

        //-----------------INIT-----------------//
        helper.initCloudSim();
        helper.createBroker("basic", arrivalFile);
        helper.createPeList(numberOfProcessingUnits);
        helper.createHostList(numberOfProcessingUnits);
        helper.createVmList(numberOfProcessingUnits);
        //helper.createPersistentStorage(nodeToTaskMapping.keySet());
        helper.createDatacenterCharacteristics();
        helper.createDatacenter();

        // Files
        //helper.addFiles("");
        helper.createRequiredFilesList("");
        helper.createDataFilesList(dataFile);

        // Cloudlets
        helper.createCloudletList(nodeList);

        // Logs
        helper.printPersistenStorageDetails();
        //--------------INIT END----------------//


        start();
        print();

		//WriteToResultFile.end();
        Log.printLine("END !");
    }

	public MyRunner(String arrivalFile, String putDataFile, String getDataFile, String updateDataFile, String
			deleteDataFile, ArrayList<MyNode> nodeList, String startingFileList, MyRing ring, List<MyNode> AllnodesList) throws Exception
	{
		Log.printLine("Starting simulation \n");
		// WriteToLogFile.AddtoFile("Starting simulation \n");
		//WriteToResultFile.init();

		//-----------------INIT-----------------//
		helper.initCloudSim();
		helper.createBroker("basic", arrivalFile);
		helper.createPeList(numberOfProcessingUnits);
		helper.createHostList(numberOfProcessingUnits);
		helper.createVmList(numberOfProcessingUnits);
		helper.createPersistentStorage(AllnodesList);
		helper.createDatacenterCharacteristics();
		helper.createDatacenter();

		// Files
		helper.addFiles(startingFileList, ring);
		helper.createRequiredFilesList(getDataFile);
		helper.createDataFilesList(putDataFile);
		helper.createUpdateFilesList(updateDataFile);
		helper.createDeleteFilesList(deleteDataFile);

		// Cloudlets
		//helper.createCloudletList(nodeList);
		MyDatacenter.myRunner = this;
		helper.setMyNodeListOne(nodeList);
		helper.createCloudletListOne();

		// Logs
		helper.printPersistenStorageDetails();
		//--------------INIT END----------------//


		start();
		//print();
		//print(new ArrayList<MyNode>(nodeToTaskMapping.keySet()));
		print(new ArrayList<MyNode>(AllnodesList));
		//WriteToResultFile.end();
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
		//helper.addFiles(startingFilesList);
		helper.createRequiredFilesList(requiredFiles);
		helper.createDataFilesList(dataFiles);

		// Cloudlets
		//helper.createCloudletList(NumberOfRequest);
		helper.createCloudletListOne();

		// Logs
		helper.printPersistenStorageDetails();
	}

	public List<Map<String,Object>> getDiskStats(){
	    return helper.getDiskStats();
    }

    public Map<String, Double> getScenarioStats(){
	    return helper.getScenarioStats();
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

	public void print(ArrayList<MyNode> nodeList) {
		helper.printResults(endTimeSimulation, nodeList);
	}

	public double getTotalStorageEnergyConsumed()
	{
		double TotalStorageEnergy = helper.getTotalStorageEnergyConsumed();
		return TotalStorageEnergy ;
	}

	public static void main(String args[]) throws Exception{
		String arrival = "basic/operations/arrival.txt";
		String putData = "basic/operations/putData.txt";
		String getData = "basic/operations/getData.txt";
		String updateData = "basic/operations/updateData.txt";
		String deleteData = "basic/operations/deleteData.txt";

		String pathToStartingFileList = "files/basic/operations/startingFileList.txt";
		String startingFilelist = pathToStartingFileList.split("files/")[1];

		MyRing myRing = null;
		ArrayList<MyNode> nodeList = new ArrayList<>();
        ArrayList<MyNode> allNodes = new ArrayList<>();
		ArrayList<MyNode> nodeList_dummy = new ArrayList<>();
		ArrayList<MyNode> allNodes_dummy = new ArrayList<>();
		try {

			byte data[] = FileUtils.readFileToByteArray(new File("files/basic/operations/myRing.json"));
			myRing = (MyRing) SerializationUtils.deserialize(data);

			ArrayList<MyNode> defaultNodeList = (ArrayList<MyNode>) myRing.getAllNodes();
			HashMap<String, MyNode> nodeNameToNodeMapping = new HashMap<>();
			for(MyNode n : defaultNodeList)
			{
				nodeNameToNodeMapping.put(n.getName(), n);
			}

            byte data_2[] = FileUtils.readFileToByteArray(new File("files/basic/operations/allNodes.json"));
            allNodes_dummy = (ArrayList<MyNode>) SerializationUtils.deserialize(data_2);
            for(MyNode n : allNodes_dummy)
            {
                if(nodeNameToNodeMapping.containsKey(n.getName()))
                    allNodes.add(nodeNameToNodeMapping.get(n.getName()));
                else
                {
                    nodeNameToNodeMapping.put(n.getName(), n);
                    allNodes.add(n);
                }
            }

			byte data_1[] = FileUtils.readFileToByteArray(new File("files/basic/operations/nodeList.json"));
			nodeList_dummy = (ArrayList<MyNode>) SerializationUtils.deserialize(data_1);

			for(MyNode n : nodeList_dummy)
			{
				nodeList.add(nodeNameToNodeMapping.get(n.getName()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		MyRunner runner = new MyRunner(arrival, putData, getData, updateData, deleteData, nodeList, startingFilelist, myRing, allNodes);
	}
}
