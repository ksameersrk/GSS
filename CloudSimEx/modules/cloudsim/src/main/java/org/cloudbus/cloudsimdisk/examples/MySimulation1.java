package org.cloudbus.cloudsimdisk.examples;

import org.apache.commons.io.FileUtils;
import org.cloudbus.cloudsim.core.CloudSim;
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



public class MySimulation1
{
    public static void main(String[] args) throws Exception
    {
        // IF THESE 2 VARIABLES ARE NOT INITIALIZED THEN SIMULATION WONT BE PAUSED
        CloudSim.lifeLength = 100;
        CloudSim.pauseInterval = 30;

        Ring ring = getRing("modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/rings.in");
        HashMap<Node, Tasks> simulation = new HashMap<>();
        String inputLog = "files/basic/MySimulation1/idealInputLog.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputLog)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String data[] = line.split(",");
                int i = 0;
                for(Node n : ring.getNodes(data[2]))
                {
                    // for main remote node
                    if (i == 0 ) {
                        if (simulation.containsKey(n)) {
                            simulation.get(n).addTask(line);
                        } else {
                            simulation.put(n, new Tasks(n, line));
                        }
                    }
                    // for replicas
                    else
                    {
                        if(data[0].equals("PUT"))
                        {
                            double value = Double.parseDouble(data[1]);
                            value = Math.round(value) / 2;
                            if (simulation.containsKey(n)) {
                                simulation.get(n).addTask(data[0] + "," + Double.toString(value) + "," + data[2] + "," + data[3]);
                            } else {
                                simulation.put(n, new Tasks(n, data[0] + "," + Double.toString(value) + "," + data[2] + "," + data[3]));
                            }

                        }
                    }
                    i = i + 1;
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
        String requestArrivalTimesSource = "files/basic/MySimulation1/arrival.txt"; // time distribution
        int numberOfRequest = 1; // Number of requests
        String reqdFiles = "files/basic/MySimulation1/reqdFile.txt";
        String dataFiles = "files/basic/MySimulation1/dataFile.txt"; // dataFile Name and Size
        String startingFilesList = ""; // No files to start
        int numberOfDisk = 1; // Number of disk in the persistent storage
        StorageModelHdd hddModel = n.getStorageModel(); // model of disks in the persistent storage
        PowerModelHdd hddPowerModel = n.getPowerModel(); // power model of disks

        FileUtils.writeStringToFile(new File(requestArrivalTimesSource), task.getArrivalFile());
        FileUtils.writeStringToFile(new File(dataFiles), task.getDataFile());
        FileUtils.writeStringToFile(new File(reqdFiles), task.getReqdFile());

        String tmp = task.getArrivalFile();
        //int no_of_req = tmp.split("\n").length + 1;
        if(tmp.contains("\n")) {
            numberOfRequest = tmp.split("\n").length ;
        }
        // Execution
        MyRunner simulation = new MyRunner(nameOfTheSimulation, requestArrivalRateType, numberOfRequest, requestArrivalTimesSource,
                reqdFiles, dataFiles, startingFilesList, numberOfDisk, hddModel, hddPowerModel);

        System.out.println(n + " : Energy Consumed : "+simulation.getTotalStorageEnergyConsumed() + " Joules()");
        return simulation.getTotalStorageEnergyConsumed();
    }

    public static double putOperationWithPause(ArrayList<Tasks> tasks) throws Exception
    {
        // Temporary, to build. Just Calculating for First Node from the list.
        Tasks task = tasks.get(0);

        Node n = task.getNode();
        String nameOfTheSimulation = "My Swift Example 0"; // name of the simulation
        String requestArrivalRateType = "basic"; // type of the workload
        String requestArrivalTimesSource = "basic/MySimulation1/arrival.txt"+"_"+n.getID(); // time distribution
        int numberOfRequest = 1; // Number of requests
        String reqdFiles = "basic/MySimulation1/reqdFile.txt"+"_"+n.getID();
        String dataFiles = "basic/MySimulation1/dataFile.txt"+"_"+n.getID(); // dataFile Name and Size
        String startingFilesList = ""; // No files to start
        int numberOfDisk = 1; // Number of disk in the persistent storage
        StorageModelHdd hddModel = n.getStorageModel(); // model of disks in the persistent storage
        PowerModelHdd hddPowerModel = n.getPowerModel(); // power model of disks

        FileUtils.writeStringToFile(new File("files/"+requestArrivalTimesSource), task.getArrivalFile());
        FileUtils.writeStringToFile(new File("files/"+dataFiles), task.getDataFile());
        FileUtils.writeStringToFile(new File("files/"+reqdFiles), task.getReqdFile());

        String tmp = task.getArrivalFile();
        //int no_of_req = tmp.split("\n").length + 1;
        if(tmp.contains("\n")) {
            numberOfRequest = tmp.split("\n").length ;
        }

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
                }
            }
        };
        Thread resumingThread = new Thread(monitor);
        resumingThread.setDaemon(true);
        resumingThread.start();

        // Execution
        MyRunner simulation = new MyRunner(nameOfTheSimulation, requestArrivalRateType, numberOfRequest, requestArrivalTimesSource,
                reqdFiles, dataFiles, startingFilesList, numberOfDisk, hddModel, hddPowerModel);

        return simulation.getTotalStorageEnergyConsumed();
    }
}
