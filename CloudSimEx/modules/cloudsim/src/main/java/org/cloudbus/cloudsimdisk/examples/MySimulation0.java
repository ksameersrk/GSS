package org.cloudbus.cloudsimdisk.examples;

import org.apache.commons.io.FileUtils;
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
import java.util.Comparator;
import java.util.HashMap;

import static org.cloudbus.cloudsimdisk.examples.Ring.buildRing;


/**
 * Created by ksameersrk on 21/10/16.
 */
public class MySimulation0 {
    public static void main(String[] args) throws Exception {
        Ring ring = getRing("sources/org/cloudbus/cloudsimdisk/examples/rings.in");
        HashMap<Node, Tasks> simulation = new HashMap<>();
        String inputLog = "files/basic/MySimulation0/inputLog.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputLog)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String data[] = line.split(",");
                for(Node n : ring.getNodes(data[1]))
                {
                    if(simulation.containsKey(n))
                    {
                        simulation.get(n).addTask(line);
                    }
                    else
                    {
                        simulation.put(n,new Tasks(n, line));
                    }
                }
            }
        }
        Double totalEnergyConsumed = 0.0;
        ArrayList<Node> t = new ArrayList<>(simulation.keySet());
        t.sort(Comparator.comparing(Node::getID));
        for(Node n : t)
        {
            totalEnergyConsumed += putOperation(simulation.get(n));
        }
        System.out.println("\n\nTotal Energy Consumed : "+totalEnergyConsumed);
    }

    public static Ring getRing(String filename)
    {
        Ring ring = null;
        StorageModelHdd[] storageModelHdds = new StorageModelHdd[]{ new StorageModelHddSeagateEnterpriseST6000VN0001() , new StorageModelHddHGSTUltrastarHUC109090CSS600(), new StorageModelHddToshibaEnterpriseMG04SCA500E() };
        PowerModelHdd[] powerModelHdds = new PowerModelHdd[]{ new PowerModeHddSeagateEnterpriseST6000VN0001() , new PowerModeHddHGSTUltrastarHUC109090CSS600() , new PowerModeHddToshibaEnterpriseMG04SCA500E() };

        File file = new File(filename);

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
            ring = buildRing(hm, Partition_Power, Replicas);
        }
        catch (Exception e)
        {
            new Exception("getRing");
        }
        return ring;
    }


    public static double putOperation(Tasks task) throws Exception
    {
        Node n = task.getNode();
        String nameOfTheSimulation = "My Swift Example 0"; // name of the simulation
        String requestArrivalRateType = "basic"; // type of the workload
        String requestArrivalTimesSource = "basic/MySimulation0/arrival.txt"; // time distribution
        int numberOfRequest = 1; // Number of requests
        String requiredFiles = ""; // No files required
        String dataFiles = "basic/MySimulation0/dataFile.txt"; // dataFile Name and Size
        String startingFilesList = ""; // No files to start
        int numberOfDisk = 1; // Number of disk in the persistent storage
        StorageModelHdd hddModel = n.getStorageModel(); // model of disks in the persistent storage
        PowerModelHdd hddPowerModel = n.getPowerModel(); // power model of disks

        FileUtils.writeStringToFile(new File("files/"+requestArrivalTimesSource), task.getArrivalFile());
        FileUtils.writeStringToFile(new File("files/"+dataFiles), task.getDataFile());

        // Execution
        MyRunner simulation = new MyRunner(nameOfTheSimulation, requestArrivalRateType, numberOfRequest, requestArrivalTimesSource,
                requiredFiles, dataFiles, startingFilesList, numberOfDisk, hddModel, hddPowerModel);

        System.out.println(n + " : Energy Consumed : "+simulation.getTotalStorageEnergyConsumed() + " Joule(s)");
        return simulation.getTotalStorageEnergyConsumed();
    }
}
