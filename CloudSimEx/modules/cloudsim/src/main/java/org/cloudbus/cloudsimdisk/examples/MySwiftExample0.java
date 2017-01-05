package org.cloudbus.cloudsimdisk.examples;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHddHGSTUltrastarHUC109090CSS600;
import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHddSeagateEnterpriseST6000VN0001;
import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHddToshibaEnterpriseMG04SCA500E;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModeHddHGSTUltrastarHUC109090CSS600;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModeHddSeagateEnterpriseST6000VN0001;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModeHddToshibaEnterpriseMG04SCA500E;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import static org.cloudbus.cloudsimdisk.examples.Ring.buildRing;

/**
 * Created by sai on 15/10/16.
 */
public class MySwiftExample0 {

    public static void main(String[] args)
    {
        StorageModelHdd[] storageModelHdds = new StorageModelHdd[]{ new StorageModelHddSeagateEnterpriseST6000VN0001() , new StorageModelHddHGSTUltrastarHUC109090CSS600(), new StorageModelHddToshibaEnterpriseMG04SCA500E() };
        PowerModelHdd[] powerModelHdds = new PowerModelHdd[]{ new PowerModeHddSeagateEnterpriseST6000VN0001() , new PowerModeHddHGSTUltrastarHUC109090CSS600() , new PowerModeHddToshibaEnterpriseMG04SCA500E() };

        File file = new File("sources/org/cloudbus/cloudsimdisk/examples/rings.in");

        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            int Node_Count, Partition_Power, Replicas;
            String filePath = in.readLine().trim();
            String a[] = in.readLine().trim().split(" ");
            Node_Count = Integer.parseInt(a[0]);
            Partition_Power = Integer.parseInt(a[1]);
            Replicas = Integer.parseInt(a[2]);
            HashMap<Integer, Node> hm = new HashMap<Integer, Node>();
            for(int i=0; i<Node_Count; i++)
            {
                a = in.readLine().trim().split(" ");
                int id = Integer.parseInt(a[0]);
                int zone = Integer.parseInt(a[1]);
                double weight = Double.parseDouble(a[2]);

                StorageModelHdd hddModel = storageModelHdds[i%(storageModelHdds.length)]; // model of disks in the persistent storage
                PowerModelHdd hddPowerModel = powerModelHdds[i%(powerModelHdds.length)]; // power model of disks

                hm.put(id, new Node(id, zone, weight, hddModel, hddPowerModel));
            }
            Ring ring = buildRing(hm, Partition_Power, Replicas);
            for(Node n : ring.getNodes(filePath))
            {
                System.out.println(n);
            }

            ArrayList<Node> results = ring.getNodes(filePath);
            putOperation(results , filePath);

        }
        catch (Exception e)
        {
            new Exception("Main");
        }
    }

    public static void putOperation(ArrayList<Node> results , String filePath) throws Exception
    {
        double totalStorageEnergyAllReplicas = 0.0;
        for(Node n : results )
        {
            System.out.println(n);
            String nameOfTheSimulation = "My Swift Example 0"; // name of the simulation
            String requestArrivalRateType = "basic"; // type of the workload
            String requestArrivalTimesSource = "basic/example0/ex0RequestArrivalDistri.txt"; // time distribution
            int numberOfRequest = 1; // Number of requests
            String requiredFiles = ""; // No files required
            String dataFiles = "basic/example0/ex0DataFiles.txt"; // dataFile Name and Size
            String startingFilesList = ""; // No files to start
            int numberOfDisk = 1; // Number of disk in the persistent storage
            StorageModelHdd hddModel = n.getStorageModel(); // model of disks in the persistent storage
            PowerModelHdd hddPowerModel = n.getPowerModel(); // power model of disks

            // Execution
            MyRunner simulation = new MyRunner(nameOfTheSimulation, requestArrivalRateType, numberOfRequest, requestArrivalTimesSource,
                    requiredFiles, dataFiles, startingFilesList, numberOfDisk, hddModel, hddPowerModel);

            double totalStorageEnergy = simulation.getTotalStorageEnergyConsumed();
            //Log.formatLine("**Energy consumed by Persistent Storage: %.3f Joule(s)", totalStorageEnergy);
            totalStorageEnergyAllReplicas += totalStorageEnergy ;

        }

        Log.formatLine("Energy consumed by Persistent Storage for all replicas combined : %.3f Joule(s)", totalStorageEnergyAllReplicas);

    }

}
