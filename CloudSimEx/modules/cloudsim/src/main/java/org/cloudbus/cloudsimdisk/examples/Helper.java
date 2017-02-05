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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsimdisk.MyCloudlet;
import org.cloudbus.cloudsimdisk.MyDatacenter;
import org.cloudbus.cloudsimdisk.MyPowerDatacenterBroker;
import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.MyPowerDatacenter;
import org.cloudbus.cloudsimdisk.power.MyPowerHarddriveStorage;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;
import org.cloudbus.cloudsimdisk.util.WriteToLogFile;

/**
 * Helper for the CloudSim examples of cloudsim.examples.storage package.
 * 
 * @author Baptiste Louis
 * 
 */
public class Helper {

	/**
	 * the cloudlet list.
	 */
	public List<MyCloudlet>						cloudletList	= new ArrayList<MyCloudlet>();

	/**
	 * the cloudlet required FileNames list.
	 */
	public List<String>							requiredFiles	= new ArrayList<String>();

	/**
	 * the cloudlet data Files list.
	 */
	public List<File>							dataFiles		= new ArrayList<File>();

	public List<File>							updateFiles		= new ArrayList<File>();

	public List<String>							deleteFiles		= new ArrayList<String>();

	/**
	 * the Power-VM List.
	 */
	public List<PowerVm>						vmlist			= new ArrayList<PowerVm>();

	/**
	 * the Power-host List.
	 */
	public List<PowerHost>						hostList		= new ArrayList<PowerHost>();

	/**
	 * the Pe List.
	 */
	public List<Pe>								peList			= new ArrayList<Pe>();

	/**
	 * the persistent storage List.
	 */
	public LinkedList<MyPowerHarddriveStorage>	storageList		= new LinkedList<MyPowerHarddriveStorage>();

	/**
	 * the Broker.
	 */
	public MyPowerDatacenterBroker					broker;

	/**
	 * the Datacenter.
	 */
	public MyPowerDatacenter					datacenter;

	/**
	 * the Datacenter Characteristics
	 */
	public DatacenterCharacteristics			datacenterCharacteristics;

	public HashMap<Node, MyPowerHarddriveStorage> nmmap = new HashMap<>();

	// Methods
	/**
	 * Initialize CloudSim.
	 */
	public void initCloudSim() {
		CloudSim.init(1, Calendar.getInstance(), false);
	}

	/**
	 * Creates a Power-aware broker named "Broker".
	 * 
	 * @param typeOfDistribution
	 * @param sourceOfDistribution
	 * 
	 */
	public void createBroker(String typeOfDistribution, String sourceOfDistribution) {
		try {
			broker = new MyPowerDatacenterBroker("Broker", typeOfDistribution, sourceOfDistribution);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * create Pe List.
	 * 
	 * @param PesNumber
	 */
	public void createPeList(int PesNumber) {
		for (int i = 1; i <= PesNumber; i++) {
			peList.add(new Pe(i, new PeProvisionerSimple(MyConstants.HOST_MIPS)));
		}
	}

    /**
     * get Pe List.
     *
     * @param PesNumber
     */
    public ArrayList<Pe> getPeList(int PesNumber, int seed) {
        ArrayList<Pe> tmp = new ArrayList<>();
        for (int i = 1; i <= PesNumber; i++) {
            tmp.add(new Pe(seed+i, new PeProvisionerSimple(MyConstants.HOST_MIPS)));
        }
        return tmp;
    }

	/**
	 * Creates the host list.
	 * 
	 * @param hostsNumber
	 *            the hosts number
	 */
	public void createHostList(int hostsNumber) {
		for (int i = 1; i <= hostsNumber; i++) {
			hostList.add(new PowerHost(i, new RamProvisionerSimple(MyConstants.HOST_RAM), new BwProvisionerSimple(
					MyConstants.HOST_BW), MyConstants.HOST_STORAGE, peList, new VmSchedulerTimeSharedOverSubscription(
					peList), MyConstants.HOST_POWER_MODEL));
		}
	}

	/**
	 * Creates the vm list.
	 * 
	 * @param vmsNumber
	 *            the vms number
	 */
	public void createVmList(int vmsNumber) {
		for (int i = 1; i <= vmsNumber; i++) {
			vmlist.add(new PowerVm(i, broker.getId(), MyConstants.VM_MIPS, MyConstants.VM_PES_NUMBER,
					MyConstants.VM_RAM, MyConstants.VM_BW, MyConstants.VM_SIZE, MyConstants.VM_PRIORITY,
					MyConstants.VM_VMM, MyConstants.VM_CLOUDLET_SCHEDULER, MyConstants.VM_SCHEDULING_INTERVVAL));
		}
		broker.submitVmList(vmlist);
	}

	/**
	 * create a defined number of defined storage type to the persistent storage of a power-aware datacenter.
	 * 
	 * @param storageNumber
	 * @throws ParameterException
	 */
	public void createPersistentStorage(int storageNumber, StorageModelHdd hddModel, PowerModelHdd hddPowerModel)
			throws ParameterException {
		for (int i = 1; i <= storageNumber; i++) {
			storageList.add(new MyPowerHarddriveStorage(i, "hdd" + i, hddModel, hddPowerModel));
		}
	}

    public void createPersistentStorage(Set<Node> nodes) throws ParameterException {
		MyPowerHarddriveStorage tmp = null;
        for (Node n : nodes) {
        	tmp = new MyPowerHarddriveStorage(n.getID(), "Node HDD" + n.getID(), n.getStorageModel(), n.getPowerModel(), n.getIsSpunDown());
            storageList.add(tmp);

            // creating a hashmap where key is node and value is Disk, so assigning a disk to each node
            nmmap.put(n, tmp);
        }
    }

    /**
     *  Create a Node
     *      1 host
     *      1 VM
     *      1 PE
     *      N HDD's
     */
    public void createNode(int zone_count)
    {
        for (int i = 1; i <= zone_count; i++) {
            hostList.add(new PowerHost(i, new RamProvisionerSimple(MyConstants.HOST_RAM), new BwProvisionerSimple(
                    MyConstants.HOST_BW), MyConstants.HOST_STORAGE, getPeList(1, i*100), new VmSchedulerTimeSharedOverSubscription(
                    peList), MyConstants.HOST_POWER_MODEL));
        }
    }

	/**
	 *  Create a setup for DataCenter of Nodes
	 */
	public void createDataCenterNodes(int node_count, int zone_count, HashMap<Node, Tasks> nodeToTaskMapping)
	{
        this.createHostList(zone_count);
        this.createPeList(zone_count);
        this.createVmList(zone_count);
	}

	/**
	 * Creates a power-aware Datacenter.
	 */
	public void createDatacenterCharacteristics() {
		datacenterCharacteristics = new DatacenterCharacteristics(MyConstants.DATACENTER_ARCHITECTURE,
				MyConstants.DATACENTER_OS, MyConstants.DATACENTER_VMM, hostList, MyConstants.DATACENTER_TIME_ZONE,
				MyConstants.DATACENTER_COST_PER_SEC, MyConstants.DATACENTER_COST_PER_MEM,
				MyConstants.DATACENTER_COST_PER_STORAGE, MyConstants.DATACENTER_COST_PER_BW);
	}

	/**
	 * Creates a power-aware Datacenter.
	 * 
	 * @throws Exception
	 */
	public void createDatacenter() throws Exception {
		datacenter = new MyPowerDatacenter(MyConstants.DATACENTER_NAME, datacenterCharacteristics,
				new VmAllocationPolicySimple(hostList), storageList, MyConstants.DATACENTER_SCHEDULING_INTERVAL);
	}

	/**
	 * Create a list of data Files.
	 * 
	 * @param source
	 *            name of the file in the default files folder.
	 */
	public void createDataFilesList(String source) {
		String path = "files/" + source;

		try {
			// instantiate a reader
			BufferedReader input = new BufferedReader(new FileReader(path));

			// read line by line
			String line;
			String[] lineSplited;
			String fileName;
			String fileSize;
			while ((line = input.readLine()) != null) {

				// retrieve fileName and fileSize
				lineSplited = line.split(",");
				fileName = lineSplited[0];
				fileSize = lineSplited[1];

				// add file to the List
				dataFiles.add(new File(fileName, Integer.parseInt(fileSize)));
			}

			// close the reader
			input.close();

		} catch (IOException | NumberFormatException | ParameterException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a list of required FileNames.
	 * 
	 * @param source
	 *            name of the file in the default files folder.
	 */
	public void createRequiredFilesList(String source) {

		if (source != "") {

			String path = "files/" + source;

			try {
				// instantiates reader
				BufferedReader input = new BufferedReader(new FileReader(path));

				// instantiates local variable
				String fileName;

				// read line by line
				while ((fileName = input.readLine()) != null) {

					// add fileName to the List
					requiredFiles.add(fileName);
				}

				// close the reader
				input.close();

			} catch (IOException | NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	public void createUpdateFilesList(String source) {
		String path = "files/" + source;

		try {
			// instantiate a reader
			BufferedReader input = new BufferedReader(new FileReader(path));

			// read line by line
			String line;
			String[] lineSplited;
			String fileName;
			String fileSize;
			while ((line = input.readLine()) != null) {

				// retrieve fileName and fileSize
				lineSplited = line.split(",");
				fileName = lineSplited[0];
				fileSize = lineSplited[1];

				// add file to the List
				updateFiles.add(new File(fileName, Integer.parseInt(fileSize)));
			}

			// close the reader
			input.close();

		} catch (IOException | NumberFormatException | ParameterException e) {
			e.printStackTrace();
		}
	}

	public void createDeleteFilesList(String source) {

		if (source != "") {

			String path = "files/" + source;

			try {
				// instantiates reader
				BufferedReader input = new BufferedReader(new FileReader(path));

				// instantiates local variable
				String fileName;

				// read line by line
				while ((fileName = input.readLine()) != null) {

					// add fileName to the List
					deleteFiles.add(fileName);
				}

				// close the reader
				input.close();

			} catch (IOException | NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param CloudlerNumber
	 * @throws ParameterException
	 */
	public void createCloudletList(int CloudlerNumber) throws ParameterException {

        // local variable
        ArrayList<String> tempRequiredFilesList = null;
        ArrayList<File> tempDataFilesList = null;

        for (int i = 1; i <= CloudlerNumber; i++) {

			/*
				// handle dataFiles
				if (i <= dataFiles.size()) {
					tempDataFilesList = new ArrayList<File>(Arrays.asList(dataFiles.get(i - 1)));
				} else {
					tempDataFilesList = null;
				}

				// handle requiredFiles
				if (i <= requiredFiles.size()) {
					tempRequiredFilesList = new ArrayList<String>(Arrays.asList(requiredFiles.get(i - 1)));
				} else {
					tempRequiredFilesList = null;
				}
			*/

            if (i <= dataFiles.size()) {
                tempDataFilesList = new ArrayList<File>(Arrays.asList(dataFiles.get(i - 1)));
                tempRequiredFilesList = null;
            }
            else if (i > dataFiles.size() && i <= (requiredFiles.size() + dataFiles.size())) {
                tempRequiredFilesList = new ArrayList<String>(Arrays.asList(requiredFiles.get(i - dataFiles.size() - 1)));
                tempDataFilesList = null;
            } else {
                tempRequiredFilesList = null;
                tempDataFilesList = null;
            }

            // create cloudlet
            cloudletList.add(new MyCloudlet(i, MyConstants.CLOUDLET_LENGHT, MyConstants.CLOUDLET_PES_NUMBER,
                    MyConstants.CLOUDLET_FILE_SIZE, MyConstants.CLOUDLET_OUTPUT_SIZE,
                    MyConstants.CLOUDLET_UTILIZATION_MODEL_CPU, MyConstants.CLOUDLET_UTILIZATION_MODEL_RAM,
                    MyConstants.CLOUDLET_UTILIZATION_MODEL_BW, tempRequiredFilesList, tempDataFilesList));
            cloudletList.get(i - 1).setUserId(broker.getId());
            //bind cloudlet to vm
            cloudletList.get(i - 1).setVmId(vmlist.get(0).getId());
            // cloudletList.get(i - 1).setVmId(vmlist.get((i - 1)%2).getId());
        }

        // submit the list to the broker
        broker.submitCloudletList(cloudletList);
    }


    public void createCloudletList(HashMap<Node, Tasks> nodeToTaskMapping, ArrayList<Node> nodeList) throws ParameterException {

        // local variable
        ArrayList<String> tempRequiredFilesList = null;
        ArrayList<File> tempDataFilesList = null;
        ArrayList<File> tempUpdateFileList = null;
        ArrayList<String> tempDeleteFileList = null;

        HashMap<Cloudlet, MyPowerHarddriveStorage> myMap = new HashMap<>();

        for (int i = 1; i <= nodeList.size(); i++) // nodeList size same as arriveFile that's y we use it
        {

            if (i <= dataFiles.size()) {
                tempDataFilesList = new ArrayList<File>(Arrays.asList(dataFiles.get(i - 1)));
                tempRequiredFilesList = null;
                tempUpdateFileList = null;
                tempDeleteFileList = null;
            }
            else if (i > dataFiles.size() && i <= (requiredFiles.size() + dataFiles.size())) {
                tempRequiredFilesList = new ArrayList<String>(Arrays.asList(requiredFiles.get(i - dataFiles.size() - 1)));
                tempDataFilesList = null;
				tempUpdateFileList = null;
				tempDeleteFileList = null;
            }
            else if(i > (requiredFiles.size() + dataFiles.size()) && i <= (requiredFiles.size() + dataFiles.size() + updateFiles.size())){
				tempUpdateFileList = new ArrayList<File>(Arrays.asList(updateFiles.get(i - dataFiles.size() - requiredFiles.size() - 1)));
				tempDeleteFileList = null;
				tempDataFilesList = null;
				tempRequiredFilesList = null;
			}
			else if(i > (requiredFiles.size() + dataFiles.size() + updateFiles.size()) && i<= (requiredFiles.size() + dataFiles.size() + updateFiles.size() + deleteFiles.size()))
			{
				tempDeleteFileList = new ArrayList<String>(Arrays.asList(deleteFiles.get(i - dataFiles.size() - requiredFiles.size() - updateFiles.size() - 1)));
				tempRequiredFilesList = null;
				tempDataFilesList = null;
				tempUpdateFileList = null;
			}
			else {
                tempRequiredFilesList = null;
                tempDataFilesList = null;
				tempUpdateFileList = null;
				tempDeleteFileList = null;
            }

            // create cloudlet
            cloudletList.add(new MyCloudlet(i, MyConstants.CLOUDLET_LENGHT, MyConstants.CLOUDLET_PES_NUMBER,
                    MyConstants.CLOUDLET_FILE_SIZE, MyConstants.CLOUDLET_OUTPUT_SIZE,
                    MyConstants.CLOUDLET_UTILIZATION_MODEL_CPU, MyConstants.CLOUDLET_UTILIZATION_MODEL_RAM,
                    MyConstants.CLOUDLET_UTILIZATION_MODEL_BW, tempRequiredFilesList, tempDataFilesList, tempUpdateFileList, tempDeleteFileList));
            cloudletList.get(i - 1).setUserId(broker.getId());
            //bind cloudlet to vm
            cloudletList.get(i - 1).setVmId(vmlist.get(0).getId());
            // cloudletList.get(i - 1).setVmId(vmlist.get((i - 1)%2).getId());

			// creating a hashmap where key is cloudlet and value is Disk, so assigning a disk to each cloudlet
            myMap.put(cloudletList.get(i-1), nmmap.get(nodeList.get(i-1)));
        }

        // passing all the mappings to MyDatacenter.java
        //MyDatacenter.csmap = myMap;
        MyDatacenter.csmap.putAll(myMap);

        // submit the list to the broker
        broker.submitCloudletList(cloudletList);
    }

	/**
	 * @param startingFilesList
	 */
	public void addFiles(String startingFilesList) {

		if (startingFilesList != "") {
			try {
				// instantiate a reader
				BufferedReader input = new BufferedReader(new FileReader("files/" + startingFilesList));

				// read line by line
				String line;
				String[] lineSplited;
				String fileName;
				String fileSize;
				while ((line = input.readLine()) != null) {

					// retrieve fileName and fileSize
					lineSplited = line.split("\\s+"); // regular expression
														// quantifiers for
														// whitespace
					fileName = lineSplited[0];
					fileSize = lineSplited[1];

					// add file to datacenter
					datacenter.addFile(new File(fileName, Integer.parseInt(fileSize)));
				}

				// close the reader
				input.close();

			} catch (IOException | NumberFormatException | ParameterException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * print the persistent storage details of a power-aware datacenter.
	 */
	public void printPersistenStorageDetails() {
		List<MyPowerHarddriveStorage> tempList = datacenter.getStorageList();
		String msg = "";

		for (int i = 0; i < tempList.size(); i++) {
			msg += String
					.format("OBSERVATION>> Initial persistent storage \n%d/%d %s\n\t%-16s-> %10.0f MB\n\t%-16s-> %10.0f MB\n\t%-16s-> %10.0f MB\n\t%-16s-> %10.6f s\n\t%-16s-> %10.6f s\n\t%-16s-> %10.3f MB/s\n",
							(i + 1), tempList.size(), tempList.get(i).getName(), "Capacity", tempList.get(i)
									.getCapacity(), "UsedSpace", (tempList.get(i).getCapacity() - tempList.get(i)
									.getFreeSpace()), "FreeSpave", tempList.get(i).getFreeSpace(), "Latency", tempList
									.get(i).getAvgRotLatency(), "avgSeekTime", tempList.get(i).getAvgSeekTime(),
							"maxTransferRate", tempList.get(i).getMaxInternalDataTransferRate());
		}

		WriteToLogFile.AddtoFile(msg);
	}

	/**
	 * Prints a summary of the simulation.
	 * 
	 * @param endTimeSimulation
	 */
	public void printResults(double endTimeSimulation) {
		double TotalStorageEnergy = getTotalStorageEnergyConsumed();
		List<MyPowerHarddriveStorage> tempList = datacenter.getStorageList();

		// PRINTOUT -----------------------------------------------------------------------
		Log.printLine();
		Log.printLine("*************************** RESULTS ***************************");
		Log.printLine();

		Log.printLine("TIME SPENT IN IDLE/ACTIVE MODE FOR EACH STORAGE");
		for (int i = 0; i < tempList.size(); i++) {
			Log.printLine("Storage \"" + tempList.get(i).getName() + "\"");
            Log.formatLine("\tDisk behaviour (is spun down)	: " + tempList.get(i).getIsSpunDown());
			for (Double interval : tempList.get(i).getIdleIntervalsHistory()) {
				Log.formatLine("%8sIdle intervale: %9.3f second(s)", "", interval);
			}
			Log.printLine();
			Log.formatLine("%8sTime in    Idle   mode: %9.3f second(s)", "", endTimeSimulation
					- tempList.get(i).getInActiveDuration());
			Log.formatLine("%8sTime in   Active  mode: %9.3f second(s)", "", tempList.get(i).getInActiveDuration());
			Log.formatLine("%8sTime of the simulation: %9.3f second(s)", "", endTimeSimulation);
			Log.printLine();
			Log.formatLine("%8sEnergy consumed in  Idle   mode: %9.3f Joule(s)", "", tempList.get(i)
					.getTotalEnergyIdle());
			Log.formatLine("%8sEnergy consumed in Active  mode: %9.3f Joule(s)", "", tempList.get(i)
					.getTotalEnergyActive());
			Log.formatLine("%8sEnergy consumed in  total      : %9.3f Joule(s)", "", tempList.get(i)
					.getTotalEnergyIdle() + tempList.get(i).getTotalEnergyActive());
			Log.printLine();
			Log.formatLine("%8sMaximum Queue size    : %10d operation(s)", "",
					Collections.max(tempList.get(i).getQueueLengthHistory()));
			Log.printLine();
			Log.printLine();
		}
		Log.printLine();
		Log.formatLine("Energy consumed by Always Active Disks : %.3f Joule(s)", getTotalStorageEnergyConsumedByActiveAlwaysDisks());
		Log.formatLine("Energy consumed by Spun Down Disks : %.3f Joule(s)", getTotalStorageEnergyConsumedBySpunDownDisks());
		Log.formatLine("Energy consumed by Entire Persistent Storage: %.3f Joule(s)", TotalStorageEnergy);
		Log.printLine();
		// -----------------------------------------------------------------------

		// LOGS -----------------------------------------------------------------------
		WriteToLogFile.AddtoFile("\n");
		WriteToLogFile.AddtoFile("*************************** RESULTS ***************************");
		WriteToLogFile.AddtoFile("\n");
		WriteToLogFile.AddtoFile("TIME SPENT IN IDLE/ACTIVE MODE FOR EACH STORAGE");
		for (int i = 0; i < tempList.size(); i++) {
			WriteToLogFile.AddtoFile("Storage \"" + tempList.get(i).getName() + "\"");
			WriteToLogFile.AddtoFile("\tDisk behaviour (is spun down)	: " + tempList.get(i).getIsSpunDown());
			for (Double interval : tempList.get(i).getIdleIntervalsHistory()) {
				WriteToLogFile.AddtoFile(String.format("%8sIdle intervale: %9.3f second(s)", "", interval));
			}
			WriteToLogFile.AddtoFile("\n");
			WriteToLogFile.AddtoFile(String.format("%8sTime in    Idle   mode: %9.3f second(s)", "", endTimeSimulation
					- tempList.get(i).getInActiveDuration()));
			WriteToLogFile.AddtoFile(String.format("%8sTime in   Active  mode: %9.3f second(s)", "", tempList.get(i)
					.getInActiveDuration()));
			WriteToLogFile
					.AddtoFile(String.format("%8sTime of the simulation: %9.3f second(s)", "", endTimeSimulation));
			WriteToLogFile.AddtoFile("\n");
			WriteToLogFile.AddtoFile(String.format("%8sEnergy consumed in  Idle   mode: %9.3f Joule(s)", "", tempList
					.get(i).getTotalEnergyIdle()));
			WriteToLogFile.AddtoFile(String.format("%8sEnergy consumed in Active  mode: %9.3f Joule(s)", "", tempList
					.get(i).getTotalEnergyActive()));
			WriteToLogFile.AddtoFile(String.format("%8sEnergy consumed in  total      : %9.3f Joule(s)", "", tempList
					.get(i).getTotalEnergyIdle() + tempList.get(i).getTotalEnergyActive()));
			WriteToLogFile.AddtoFile("\n");
			WriteToLogFile.AddtoFile(String.format("%8sMaximum Queue size    : %10d operation(s)", "",
					Collections.max(tempList.get(i).getQueueLengthHistory())));
			WriteToLogFile.AddtoFile("\n");
		}
		WriteToLogFile.AddtoFile("\n");
		WriteToLogFile.AddtoFile(String.format("Energy consumed by Always Active Disks : %.3f Joule(s)", getTotalStorageEnergyConsumedByActiveAlwaysDisks()));
		WriteToLogFile.AddtoFile(String.format("Energy consumed by Spun Down Disks : %.3f Joule(s)", getTotalStorageEnergyConsumedBySpunDownDisks()));
		WriteToLogFile.AddtoFile(String.format("Energy consumed by Persistent Storage: %.3f Joule(s)",
				TotalStorageEnergy));
		WriteToLogFile.AddtoFile("\n");

		/* // queue size WriteToLogFile.AddtoFile("QUEUE SIZE in Operation(s) (not sorted)"); for (int i = 0; i <
		 * tempList.size(); i++) { WriteToLogFile.AddtoFile("For Disk" + tempList.get(i).getName()); for (int queue :
		 * tempList.get(i).getQueueLengthHistory()) { WriteToLogFile.AddtoFile(String.format("%4d", queue)); } } //
		 * ----------------------------------------------------------------------- */
	}

	public double getTotalStorageEnergyConsumed()
	{
		/*
		// old way : deprecated, applicable only when all disks are always active
		double TotalStorageEnergy = datacenter.getTotalStorageEnergy();
		*/
		double TotalStorageEnergy = 0;


		List<MyPowerHarddriveStorage> tempList = datacenter.getStorageList();
		for (int i = 0; i < tempList.size(); i++) {
			TotalStorageEnergy += tempList.get(i).getTotalEnergyActive();
			// only if disk not of is spun down type then add idle state power consumption
			if(!tempList.get(i).getIsSpunDown()){
				TotalStorageEnergy += tempList.get(i).getTotalEnergyIdle();
			}
		}
		return TotalStorageEnergy ;
	}

	public double getTotalStorageEnergyConsumedByActiveAlwaysDisks()
	{
		/*
		// old way : deprecated, applicable only when all disks are always active
		double TotalStorageEnergy = datacenter.getTotalStorageEnergy();
		*/
		double TotalStorageEnergy = 0;


		List<MyPowerHarddriveStorage> tempList = datacenter.getStorageList();
		for (int i = 0; i < tempList.size(); i++) {

			// only if disk not of is spun down type then add idle state power consumption
			if(!tempList.get(i).getIsSpunDown()){
				TotalStorageEnergy += tempList.get(i).getTotalEnergyActive();
				TotalStorageEnergy += tempList.get(i).getTotalEnergyIdle();
			}
		}
		return TotalStorageEnergy ;
	}

	public double getTotalStorageEnergyConsumedBySpunDownDisks()
	{
		/*
		// old way : deprecated, applicable only when all disks are always active
		double TotalStorageEnergy = datacenter.getTotalStorageEnergy();
		*/
		double TotalStorageEnergy = 0;


		List<MyPowerHarddriveStorage> tempList = datacenter.getStorageList();
		for (int i = 0; i < tempList.size(); i++) {

			// only if disk not of is spun down type then add idle state power consumption
			if(tempList.get(i).getIsSpunDown()){
				TotalStorageEnergy += tempList.get(i).getTotalEnergyActive();
				TotalStorageEnergy += tempList.get(i).getTotalEnergyIdle();
			}
		}
		return TotalStorageEnergy ;
	}
}
