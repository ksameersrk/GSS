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

        // Create the ring
        Ring ring = getRing("modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/rings.in");
        HashMap<Node, Tasks> nodeToTaskMapping = new HashMap<>();
        String inputLog = "files/basic/operations/idealInputLog.txt";
        ArrayList<String> arrivalFile = new ArrayList<>();
        ArrayList<String> dataFile = new ArrayList<>();
        ArrayList<String> requiredFile = new ArrayList<>();
        ArrayList<String> updateFile = new ArrayList<>();
        ArrayList<String> deleteFile = new ArrayList<>();
        ArrayList<Node> nodeList = new ArrayList<>();
        // for PUT operation
        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputLog)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String data[] = line.split(",");

                if(data[0].equals("PUT")) {
                    arrivalFile.add(data[1]);
                    arrivalFile.add(data[1]);
                    arrivalFile.add(data[1]);
                    dataFile.add(data[2] + "," + data[3]);
                    dataFile.add(data[2] + "," + data[3]);
                    dataFile.add(data[2] + "," + data[3]);

                    for(Node n : ring.getNodes(data[2]))
                    {
                        nodeList.add(n);
                        if (nodeToTaskMapping.containsKey(n)) {
                            nodeToTaskMapping.get(n).addTask(line);
                        } else {
                            nodeToTaskMapping.put(n, new Tasks(n, line));
                        }
                    }
                }
            }
        }
        // for GET operation
        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputLog)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String data[] = line.split(",");

                if(data[0].equals("GET")) {
                    arrivalFile.add(data[1]);
                    requiredFile.add(data[2]);
                    ArrayList<Node> nodes = ring.getNodes(data[2]);
                    Node n = nodes.get(0);
                    nodeList.add(n);
                    if (nodeToTaskMapping.containsKey(n)) {
                        nodeToTaskMapping.get(n).addTask(line);
                    } else {
                        nodeToTaskMapping.put(n, new Tasks(n, line));
                    }
                }
            }
        }

        // for UPDATE operation
        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputLog)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String data[] = line.split(",");

                if(data[0].equals("UPDATE")) {
                    arrivalFile.add(data[1]);
                    arrivalFile.add(data[1]);
                    arrivalFile.add(data[1]);
                    updateFile.add(data[2] + "," + data[3]);
                    updateFile.add(data[2] + "," + data[3]);
                    updateFile.add(data[2] + "," + data[3]);

                    for(Node n : ring.getNodes(data[2]))
                    {
                        nodeList.add(n);
                        if (nodeToTaskMapping.containsKey(n)) {
                            nodeToTaskMapping.get(n).addTask(line);
                        } else {
                            nodeToTaskMapping.put(n, new Tasks(n, line));
                        }
                    }
                }
            }
        }

        // for DELETE operation
        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputLog)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String data[] = line.split(",");

                if(data[0].equals("DELETE")) {
                    arrivalFile.add(data[1]);
                    arrivalFile.add(data[1]);
                    arrivalFile.add(data[1]);
                    deleteFile.add(data[2]);
                    deleteFile.add(data[2]);
                    deleteFile.add(data[2]);

                    for(Node n : ring.getNodes(data[2]))
                    {
                        nodeList.add(n);
                        if (nodeToTaskMapping.containsKey(n)) {
                            nodeToTaskMapping.get(n).addTask(line);
                        } else {
                            nodeToTaskMapping.put(n, new Tasks(n, line));
                        }
                    }
                }
            }
        }
        ArrayList<Node> all = new ArrayList<Node>(nodeToTaskMapping.keySet());
        all.sort(Comparator.comparing(Node::getID));
        System.out.println("Check the mapping with assignment : ");
        for(Node n : all)
        {
            System.out.println(n.getID()+ " : "+nodeToTaskMapping.get(n).getFiles());
        }

        Double totalEnergyConsumed = 0.0;
        // ArrayList<Node> t = new ArrayList<>(nodeToTaskMapping.keySet());
        // t.sort(Comparator.comparing(Node::getID));
        totalEnergyConsumed = performOperations(nodeToTaskMapping, arrivalFile, dataFile, requiredFile, updateFile, deleteFile, nodeList);
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


    public static double performOperations(HashMap<Node, Tasks> nodeToTaskMapping, ArrayList<String> arrivalFile, ArrayList<String> dataFile, ArrayList<String> requiredFile, ArrayList<String> updateFile, ArrayList<String> deleteFile, ArrayList<Node> nodeList) throws Exception
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
        String putData = "basic/operations/putData.txt";
        String getData = "basic/operations/getData.txt";
        String updateData = "basic/operations/updateData.txt";
        String deleteData = "basic/operations/deleteData.txt";

        StringBuilder arrivalTimes = new StringBuilder();
        StringBuilder putOpData = new StringBuilder();
        StringBuilder getOpData = new StringBuilder();
        StringBuilder updateOpData = new StringBuilder();
        StringBuilder deleteOpData = new StringBuilder();
        /*
        for(String x : arrivalFile)
        {
            arrivalTimes.append(x+"\n");
        }
        arrivalTimes.setLength(arrivalTimes.length() - 1);
        for(String x : dataFile)
        {
            putOpData.append(x+"\n");
        }
        putOpData.setLength(putOpData.length() - 1);
        for(String x : requiredFile)
        {
            getOpData.append(x+"\n");
        }
        getOpData.setLength(getOpData.length() - 1);
        */

        for(int i = 0; i < arrivalFile.size(); i++){
            if(i > 0)
                arrivalTimes.append("\n");
            arrivalTimes.append(arrivalFile.get(i));
        }
        for(int i = 0; i < dataFile.size(); i++){
            if(i > 0)
                putOpData.append("\n");
            putOpData.append(dataFile.get(i));
        }
        for(int i = 0; i < requiredFile.size(); i++){
            if(i > 0)
                getOpData.append("\n");
            getOpData.append(requiredFile.get(i));
        }
        for(int i = 0; i < updateFile.size(); i++){
            if(i > 0)
                updateOpData.append("\n");
            updateOpData.append(updateFile.get(i));
        }
        for(int i = 0; i < deleteFile.size(); i++){
            if(i > 0)
                deleteOpData.append("\n");
            deleteOpData.append(deleteFile.get(i));
        }

        FileUtils.writeStringToFile(new File("files/"+arrival), arrivalTimes.toString());
        FileUtils.writeStringToFile(new File("files/"+putData), putOpData.toString());
        FileUtils.writeStringToFile(new File("files/"+getData), getOpData.toString());
        FileUtils.writeStringToFile(new File("files/"+updateData), updateOpData.toString());
        FileUtils.writeStringToFile(new File("files/"+deleteData), deleteOpData.toString());

        MyRunner run = new MyRunner(nodeToTaskMapping, arrival, putData, getData, updateData, deleteData, nodeList);
        System.out.println("Energy Consumed : "+run.getTotalStorageEnergyConsumed() + " Joules()");
        return run.getTotalStorageEnergyConsumed();
    }
}
