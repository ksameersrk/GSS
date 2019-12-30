package org.cloudbus.cloudsim.ex.examples;


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
 * while creating variable number of VMs and cloudlets using createVM() and createCloudlet() methods
 */
public class DelayExample2 {

    /** The cloudlet list. */
    private static List<Cloudlet> cloudletList;

    /** The vmlist. */
    private static List<Vm> vmlist;

    private static List<Vm> createVM(int userId, int vms, int idShift) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<Vm>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 1000;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        Vm[] vm = new Vm[vms];

        for(int i=0;i<vms;i++){
            vm[i] = new Vm(idShift + i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }

        return list;
    }


    private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift){
        // Creates a container to store Cloudlets
        LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

        //cloudlet parameters
        long length = 40000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet[] cloudlet = new Cloudlet[cloudlets];

        for(int i=0;i<cloudlets;i++){
            cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            // setting the owner of these Cloudlets
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }

        return list;
    }

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
        final DatacenterBrokerEX broker = new DatacenterBrokerEX("Broker", 10000);
        final int brokerId = broker.getId();

        vmlist = createVM(brokerId, 1, 0); //creating 1 vms
        cloudletList = createCloudlet(brokerId, 10, 0); // creating 10 cloudlets

        // submit vm to the broker after 2 seconds
        broker.createVmsAfter(vmlist, 2);
        // destroy VM after 500 seconds
        broker.destroyVMsAfter(vmlist, 10000);

        // Step 5: Create a Cloudlet

        broker.submitCloudletList(cloudletList, 10);
        // Sixth step: Starts the simulation
        CloudSim.startSimulation();
        // ...
        CloudSim.stopSimulation();
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