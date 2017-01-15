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

/**
 * Created by skulkarni9 on 1/14/17.
 */
public class Operations
{
    public static void main(String[] args) throws Exception
    {
        // IF THESE 2 VARIABLES ARE NOT INITIALIZED THEN SIMULATION WONT BE PAUSED
        CloudSim.lifeLength = 100;
        CloudSim.pauseInterval = 30;

        Ring ring = getRing("modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/rings.in");
        HashMap<Node, Tasks> simulation = new HashMap<>();
        String inputLog = "files/basic/operations/idealInputLog.txt";
        ArrayList<String> arrivalFile = new ArrayList<>();
        ArrayList<String> dataFile = new ArrayList<>();
        ArrayList<Node> seq = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputLog)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String data[] = line.split(",");
                arrivalFile.add(data[1]);
                arrivalFile.add(data[1]);
                arrivalFile.add(data[1]);
                dataFile.add(data[2]+","+data[3]);
                dataFile.add(data[2]+","+data[3]);
                dataFile.add(data[2]+","+data[3]);
                for(Node n : ring.getNodes(data[2]))
                {
                    seq.add(n);
                    if (simulation.containsKey(n)) {
                        simulation.get(n).addTask(line);
                    } else {
                        simulation.put(n, new Tasks(n, line));
                    }
                }
            }
        }
        ArrayList<Node> all = new ArrayList<Node>(simulation.keySet());
        all.sort(Comparator.comparing(Node::getID));
        System.out.println("Check the mapping with assignment : ");
        for(Node n : all)
        {
            System.out.println(n.getID()+ " : "+simulation.get(n).getFiles());
        }

        Double totalEnergyConsumed = 0.0;
        // ArrayList<Node> t = new ArrayList<>(simulation.keySet());
        // t.sort(Comparator.comparing(Node::getID));
        totalEnergyConsumed = putOperation(simulation, arrivalFile, dataFile, seq);
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


    public static double putOperation(HashMap<Node, Tasks> simulation, ArrayList<String> arrivalFile, ArrayList<String> dataFile, ArrayList<Node> seq) throws Exception
    {
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
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        Thread resumingThread = new Thread(monitor);
        resumingThread.setDaemon(true);
        resumingThread.start();


        String arrival = "basic/operations/arrival.txt";
        String data = "basic/operations/data.txt";

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for(String x : arrivalFile)
        {
            sb1.append(x+"\n");
        }
        for(String x : dataFile)
        {
            sb2.append(x+"\n");
        }

        FileUtils.writeStringToFile(new File("files/"+arrival), sb1.toString());
        FileUtils.writeStringToFile(new File("files/"+data), sb2.toString());

        MyRunner run = new MyRunner(simulation, arrival, data, seq);
        System.out.println("Energy Consumed : "+run.getTotalStorageEnergyConsumed() + " Joules()");
        return run.getTotalStorageEnergyConsumed();
    }
}
