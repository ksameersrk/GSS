package org.cloudbus.cloudsim.ex.examples;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
//import org.cloudbus.cloudsim.examples.CloudSimExample1;
import org.cloudbus.cloudsim.ex.DatacenterBrokerEX;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * A simple example showing how to submit and destroy VMs and cloudlets with a delay.
 * Based on {@link DynamicCloudlets0}.
 */
public class DynamicCloudlets0{

    public static void main(String[] args) throws Exception {

        // Step1: Initialize the CloudSim package. It should be called
        // before creating any entities.
        int numBrokers = 1; // number of brokers we'll be using
        boolean trace_flag = false; // mean trace events

        // Initialize CloudSim
        CloudSim.init(numBrokers, Calendar.getInstance(), trace_flag);

        // Step 2: Create Datacenter
        // Datacenters are the resource providers in CloudSim. We need at
        // list one of them to run a CloudSim simulation
        @SuppressWarnings("unused")
        Datacenter datacenter0 = createDatacenter("Datacenter_0");

        // Step 3: Create Broker
        final DatacenterBrokerEX broker = new DatacenterBrokerEX("Broker", 1000);

        // Step 4: Create one virtual machine
        final int vmid = 0;
        int mips = 1000;
        long size = 10000; // image size (MB)
        int ram = 512; // vm memory (MB)
        long bw = 1000;
        final int pesNumber = 1; // number of cpus
        String vmm = "Xen"; // VMM name
        Vm vm = new Vm(vmid, broker.getId(), mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());

        // submit vm to the broker after 2 seconds
        broker.createVmsAfter(Arrays.asList(vm), 1);
        // broker.submitVmList(Arrays.asList(vm));
        // destroy VM after 500 seconds
        broker.destroyVMsAfter(Arrays.asList(vm), 10000);

        // Step 5: Create a Cloudlet
        // Cloudlet properties
        final int id = 0;
        final long length = 40000;
        final long fileSize = 300;
        final long outputSize = 300;
        final UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel,
                utilizationModel, utilizationModel);
        cloudlet.setUserId(broker.getId());
        cloudlet.setVmId(vmid);

        // submit cloudlet list to the broker with 10 sec Delay
        broker.submitCloudletList(Arrays.asList(cloudlet), 2);
        
     // A thread that will create a new broker at 200 clock time
     			Runnable monitor = new Runnable() {
     				@Override
     				public void run() {
     					CloudSim.pauseSimulation(200);
     					while (true) {
     						if (CloudSim.isPaused()) {
     							break;
     						}
     						try {
     							Thread.sleep(100);
     						} catch (InterruptedException e) {
     							e.printStackTrace();
     						}
     					}

     					Log.printLine("\n\n\n" + CloudSim.clock() + ": The simulation is paused for 5 sec \n\n");

     					try {
     						Thread.sleep(5000);
     					} catch (InterruptedException e) {
     						e.printStackTrace();
     					}

     					Cloudlet cloudlet_1 = new Cloudlet(id + 1, length*2, pesNumber, fileSize, outputSize, utilizationModel,
     			                utilizationModel, utilizationModel);
     			        cloudlet_1.setUserId(broker.getId());
     			        cloudlet_1.setVmId(vmid);

     			        // submit cloudlet list to the broker with 10 sec Delay
     			        broker.submitCloudletList(Arrays.asList(cloudlet_1), 2);
     					
     					CloudSim.resumeSimulation();
     				}
     			};

     			new Thread(monitor).start();
     			Thread.sleep(1000);

        // Sixth step: Starts the simulation
        CloudSim.startSimulation();
        // ...
        // Final step: Print results when simulation is over
		List<Cloudlet> newList = broker.getCloudletReceivedList();
        CloudSim.stopSimulation();
        printCloudletList(newList);

		Log.printLine("CloudSimExample7 finished!");
    }

    private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");

				Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
						indent + indent + dft.format(cloudlet.getExecStartTime())+ indent + indent + indent + dft.format(cloudlet.getFinishTime()));
			}
		}

	}
    
    /**
     * Creates the datacenter.
     * 
     * @param name
     *            the name
     * 
     * @return the datacenter
     * @throws Exception 
     */
    private static Datacenter createDatacenter(String name) throws Exception {

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        // our machine
        List<Host> hostList = new ArrayList<Host>();

        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<Pe>();

        int mips = 1000;

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store
                                                              // Pe id and MIPS
                                                              // Rating

        // 4. Create Host with its id and list of PEs and add them to the list
        // of machines
        int hostId = 0;
        int ram = 2048; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerTimeShared(peList)
                )
                ); // This is our machine

        // 5. Create a DatacenterCharacteristics object that stores the
        // properties of a data center: architecture, OS, list of
        // Machines, allocation policy: time- or space-shared, time zone
        // and its price (G$/Pe time unit).
        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this
                                       // resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are
                                                                     // not
                                                                     // adding
                                                                     // SAN
        // devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        // 6. Finally, we need to create a PowerDatacenter object.
        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
    }

}