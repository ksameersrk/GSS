package org.cloudbus.cloudsimdisk.examples;

import org.apache.commons.io.FileUtils;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsimdisk.models.hdd.*;
import org.cloudbus.cloudsimdisk.power.models.hdd.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import static org.cloudbus.cloudsimdisk.examples.Ring.buildRing;

/**
 * Created by SaiVishwas on 1/26/17.
 */
public class StagingDiskAndSpinDown {

    public static void main(String[] args) throws Exception {
        // IF THESE 2 VARIABLES ARE NOT INITIALIZED THEN SIMULATION WONT BE PAUSED
        CloudSim.lifeLength = 100;
        CloudSim.pauseInterval = 30;

        // staging disk properties
        boolean addStagingDisk = true;
        StorageModelHdd stagingDiskStorageModel = new StorageModelSsdSeagate600ProEnterpriseST480FP0021();
        PowerModelHdd stagingDiskPowerModel = new PowerModelSsdSeagate600ProEnterpriseST480FP0021();
        int stagingDiskThresholdMemory = (int)(stagingDiskStorageModel.getCapacity()*0.8);
        int stagingDiskLowerThreshold = (int)(stagingDiskStorageModel.getCapacity()*0.5);
        int stagingDiskUsedMemory = 0;
        Node stagingDisk = new Node(999, 99, 99, stagingDiskStorageModel, stagingDiskPowerModel, false);
        System.out.println(stagingDiskThresholdMemory);

        // node properties
        int noOfSpunDownDisks = 1;
        int noOfActiveAlwaysDisks = 2;

        // Create the ring
        Ring ringOfActiveAlwaysDisks = getRing("files/basic/StagingDiskAndSpinDown/rings.in", 170, noOfActiveAlwaysDisks, false);
        Ring ringOfSpunDownDisks = getRing("files/basic/StagingDiskAndSpinDown/rings.in", 86, noOfSpunDownDisks, true);

        String inputLog = "files/basic/StagingDiskAndSpinDown/idealInputLog.txt";
        ArrayList<String> arrivalFile = new ArrayList<>();
        ArrayList<Node> nodeList = new ArrayList<>();
        HashMap<Node, Tasks> nodeToTaskMapping = new HashMap<>();

        ArrayList<String> dataFile = new ArrayList<>();
        ArrayList<String> requiredFile = new ArrayList<>();
        ArrayList<String> updateFile = new ArrayList<>();
        ArrayList<String> deleteFile = new ArrayList<>();

        if(addStagingDisk == false){
            dataFile = getOperationFileList(inputLog, "PUT", nodeToTaskMapping, arrivalFile, ringOfActiveAlwaysDisks, nodeList, noOfActiveAlwaysDisks);
            requiredFile = getOperationFileList(inputLog, "GET", nodeToTaskMapping, arrivalFile, ringOfActiveAlwaysDisks, nodeList, noOfActiveAlwaysDisks);
            updateFile = getOperationFileList(inputLog, "UPDATE", nodeToTaskMapping, arrivalFile, ringOfActiveAlwaysDisks, nodeList, noOfActiveAlwaysDisks);
            deleteFile = getOperationFileList(inputLog, "DELETE", nodeToTaskMapping, arrivalFile, ringOfActiveAlwaysDisks, nodeList, noOfActiveAlwaysDisks);
        }
        else{

            stagingDiskSimulate(arrivalFile, dataFile, requiredFile, updateFile, deleteFile, inputLog, nodeToTaskMapping, nodeList, noOfActiveAlwaysDisks, ringOfActiveAlwaysDisks, noOfSpunDownDisks, ringOfSpunDownDisks, stagingDiskThresholdMemory, stagingDisk, stagingDiskLowerThreshold);

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

    public  static void stagingDiskSimulate(ArrayList<String> arrivalFile, ArrayList<String> dataFile, ArrayList<String> requiredFile, ArrayList<String> updateFile, ArrayList<String> deleteFile, String inputLog, HashMap<Node, Tasks> nodeToTaskMapping, ArrayList<Node> nodeList, int noOfActiveAlwaysDisks, Ring ringOfActiveAlwaysDisks, int noOfSpunDownDisks, Ring ringOfSpunDownDisks, int stagingDiskThresholdMemory, Node stagingDisk, int stagingDiskLowerThreshold) throws Exception {
        int stagingDiskMemoryUsed = 0;
        Map<String, Integer> stagingDiskFileList = new LinkedHashMap<String, Integer>();
        Map<Integer, ArrayList<String>> chronologicallyOrderedOperations = getChronologicallyOrderedOperations(inputLog);
        ArrayList<String> tmpdataFile = new ArrayList<>();
        ArrayList<String> tmprequiredFile = new ArrayList<>();
        ArrayList<String> tmpupdateFile = new ArrayList<>();
        ArrayList<String> tmpdeleteFile = new ArrayList<>();

        Map<String, Integer> tmpToBeDeletedList = new LinkedHashMap<String, Integer>();

        for(Integer key : chronologicallyOrderedOperations.keySet()){
            ArrayList<String> operations = chronologicallyOrderedOperations.get(key);
            for(String op : operations){
                op = op.trim();
                String data[] = op.split(",");
                if(data[0].equals("PUT")){
                    /*if(stagingDiskMemoryUsed + Integer.parseInt(data[3]) > stagingDiskThresholdMemory){
                        // remove oldest unused files such after removing them only 60% of stagingDisk mem is occupied
                        // before removing add them to respective spun down disks
                        Map<String, Integer>  tmpToBeAddedToSpunDownFiles = new LinkedHashMap<String, Integer>();
                        int memoryToBeFreed = 0;
                        for(String file : stagingDiskFileList.keySet()){
                            if(stagingDiskMemoryUsed - memoryToBeFreed - stagingDiskFileList.get(file) > stagingDiskLowerThreshold){
                                tmpToBeAddedToSpunDownFiles.put(file, stagingDiskFileList.get(file));
                                memoryToBeFreed += stagingDiskFileList.get(file);
                            }
                            else {
                                break;
                            }
                        }

                        for(String file : tmpToBeAddedToSpunDownFiles.keySet()){
                            stagingDiskFileList.remove(file);
                        }

                        // update stagingDiskMemoryUsed after removing old files
                        stagingDiskMemoryUsed -= memoryToBeFreed;
                        memoryToBeFreed = 0;

                        // now add those files to spun down disks
                        for(String file : tmpToBeAddedToSpunDownFiles.keySet()){
                            for(int i=0; i < noOfSpunDownDisks; i++)
                                tmpdataFile.add("PUT,"+data[1]+","+file+","+tmpToBeAddedToSpunDownFiles.get(file));

                            for(Node n : ringOfSpunDownDisks.getNodes(data[2]))
                            {
                                nodeList.add(n);
                                if (nodeToTaskMapping.containsKey(n)) {
                                    nodeToTaskMapping.get(n).addTask("PUT,"+data[1]+","+file+","+tmpToBeAddedToSpunDownFiles.get(file));
                                } else {
                                    nodeToTaskMapping.put(n, new Tasks(n, "PUT,"+data[1]+","+file+","+tmpToBeAddedToSpunDownFiles.get(file)));
                                }
                            }
                        }


                        // how will you update the spunDown disks of the delete operations done here
                        for (String file : tmpToBeDeletedList.keySet()){
                            for(int i=0; i < noOfSpunDownDisks; i++)
                                tmpdeleteFile.add("DELETE,"+data[1]+","+file);
                            for(Node n : ringOfSpunDownDisks.getNodes(data[2]))
                            {
                                nodeList.add(n);
                                if (nodeToTaskMapping.containsKey(n)) {
                                    nodeToTaskMapping.get(n).addTask("DELETE,"+data[1]+","+file);
                                } else {
                                    nodeToTaskMapping.put(n, new Tasks(n, "DELETE,"+data[1]+","+file));
                                }
                            }
                        }
                        // (could keep another queue which will be updated on every delete operation and we execute all of them when this case is encountered)


                    }

                    // enough space in staging disk now
                    tmpdataFile.add(op);
                    nodeList.add(stagingDisk);
                    stagingDiskMemoryUsed += Integer.parseInt(data[3]);
                    //stagingDiskFileList.put()
                    // add to stagingDiskFileList
                    stagingDiskFileList.put(data[2], Integer.parseInt(data[3])); // key : name of file, value : size  of file
                    // add file to staging disk and activeAlways nodes

//                    for(int i=0; i < noOfActiveAlwaysDisks; i++)
//                        arrivalFile.add(data[1]);

                    for(int i=0; i < noOfActiveAlwaysDisks; i++)
                        tmpdataFile.add(op);
                    // need to to do nodeTask mapping for these tasks
                    if (nodeToTaskMapping.containsKey(stagingDisk)) {
                        nodeToTaskMapping.get(stagingDisk).addTask(op);
                    } else {
                        nodeToTaskMapping.put(stagingDisk, new Tasks(stagingDisk, op));
                    }

                    for(Node n : ringOfActiveAlwaysDisks.getNodes(data[2]))
                    {
                        nodeList.add(n);
                        if (nodeToTaskMapping.containsKey(n)) {
                            nodeToTaskMapping.get(n).addTask(op);
                        } else {
                            nodeToTaskMapping.put(n, new Tasks(n, op));
                        }
                    }*/

                    stagingDiskPutOperation(op, stagingDisk, noOfActiveAlwaysDisks, data, stagingDiskMemoryUsed, stagingDiskThresholdMemory, tmpToBeDeletedList, stagingDiskFileList, stagingDiskLowerThreshold, noOfSpunDownDisks, tmpdataFile, tmpdeleteFile, nodeList, nodeToTaskMapping, ringOfActiveAlwaysDisks, ringOfSpunDownDisks);

                }
                else if(data[0].equals("GET")){
                    // if in staging disk add operation to staging disk also somehow coz this file has been used move it up the order in stagingFile disk
                    if (stagingDiskFileList.containsKey(data[2])){
                        tmprequiredFile.add(op);
                        nodeList.add(stagingDisk);
                        if (nodeToTaskMapping.containsKey(stagingDisk)) {
                            nodeToTaskMapping.get(stagingDisk).addTask(op);
                        } else {
                            nodeToTaskMapping.put(stagingDisk, new Tasks(stagingDisk, op));
                        }
                        int fileSize = stagingDiskFileList.get(data[2]);
                        // remove from file list
                        stagingDiskFileList.remove(data[2]);
                        // update position of file in list
                        stagingDiskFileList = addToBeginning(data[2], fileSize, stagingDiskFileList);
                    }
                    else{
                        // else go to active always node
                        tmprequiredFile.add(op);
                        ArrayList<Node> nodes = ringOfActiveAlwaysDisks.getNodes(data[2]);
                        Node n = nodes.get(0);
                        nodeList.add(n);
                        if (nodeToTaskMapping.containsKey(n)) {
                            nodeToTaskMapping.get(n).addTask(op);
                        } else {
                            nodeToTaskMapping.put(n, new Tasks(n, op));
                        }
                        // need to see when to get from spunDown disks
                        // also should when file not in stagingDisk and we get it from end node, should we add it to stagingDisk for future use
                    }
                }
                else if(data[0].equals("UPDATE")){
                    // if old version also present, then remove old version before proceeding
                    if(stagingDiskFileList.containsKey(data[2])){
                        int memoryToBeFreed = stagingDiskFileList.get(data[2]);
                        stagingDiskFileList.remove(data[2]);
                        stagingDiskMemoryUsed -= memoryToBeFreed;

//                        tmpupdateFile.add(op);
//                        nodeList.add(stagingDisk);
//                        if (nodeToTaskMapping.containsKey(stagingDisk)) {
//                            nodeToTaskMapping.get(stagingDisk).addTask(op);
//                        } else {
//                            nodeToTaskMapping.put(stagingDisk, new Tasks(stagingDisk, op));
//                        }
                    }
                    // now same as PUT
                    stagingDiskPutOperation(op, stagingDisk, noOfActiveAlwaysDisks, data, stagingDiskMemoryUsed, stagingDiskThresholdMemory, tmpToBeDeletedList, stagingDiskFileList, stagingDiskLowerThreshold, noOfSpunDownDisks, tmpupdateFile, tmpdeleteFile, nodeList, nodeToTaskMapping, ringOfActiveAlwaysDisks, ringOfSpunDownDisks);
                }
                else if(data[0].equals("DELETE")) {
                    // delete everywhere
                    if (stagingDiskFileList.containsKey(data[2])) {
                        int memoryToBeFreed = stagingDiskFileList.get(data[2]);
                        stagingDiskFileList.remove(data[2]);
                        stagingDiskMemoryUsed -= memoryToBeFreed;

                        tmpdeleteFile.add(op);
                        nodeList.add(stagingDisk);
                        if (nodeToTaskMapping.containsKey(stagingDisk)) {
                            nodeToTaskMapping.get(stagingDisk).addTask(op);
                        } else {
                            nodeToTaskMapping.put(stagingDisk, new Tasks(stagingDisk, op));
                        }
                    }
                    for (int i = 0; i < noOfActiveAlwaysDisks; i++)
                        tmpdeleteFile.add(op);
                    for (Node n : ringOfActiveAlwaysDisks.getNodes(data[2])) {
                        nodeList.add(n);
                        if (nodeToTaskMapping.containsKey(n)) {
                            nodeToTaskMapping.get(n).addTask(op);
                        } else {
                            nodeToTaskMapping.put(n, new Tasks(n, op));
                        }
                    }

                    // how to take care of delete on spunDown disks coz file will be removed from stagingDisk and old version could continue existing on spunDown disk
                    tmpToBeDeletedList.put(data[2], Integer.parseInt(data[1]));

                }
            }
        }

        createOperationFileList(arrivalFile, dataFile, requiredFile, updateFile, deleteFile, tmpdataFile, tmprequiredFile, tmpupdateFile, tmpdeleteFile);
    }

    public static void stagingDiskPutOperation(String op, Node stagingDisk, int noOfActiveAlwaysDisks, String data[], int stagingDiskMemoryUsed, int stagingDiskThresholdMemory, Map<String, Integer> tmpToBeDeletedList,  Map<String, Integer> stagingDiskFileList, int stagingDiskLowerThreshold, int noOfSpunDownDisks, ArrayList<String> tmpOpFile, ArrayList<String> tmpdeleteFile, ArrayList<Node> nodeList, HashMap<Node, Tasks> nodeToTaskMapping, Ring ringOfActiveAlwaysDisks, Ring ringOfSpunDownDisks)
    {
        if(stagingDiskMemoryUsed + Integer.parseInt(data[3]) > stagingDiskThresholdMemory){
            freeUpStagingDiskMemory(data, stagingDiskMemoryUsed, stagingDiskThresholdMemory, tmpToBeDeletedList, stagingDiskFileList, stagingDiskLowerThreshold, noOfSpunDownDisks, tmpOpFile, tmpdeleteFile, nodeList, nodeToTaskMapping, ringOfActiveAlwaysDisks, ringOfSpunDownDisks);
        }
        // enough space in staging disk now
        tmpOpFile.add(op);
        nodeList.add(stagingDisk);
        stagingDiskMemoryUsed += Integer.parseInt(data[3]);
        //stagingDiskFileList.put()
        // add to stagingDiskFileList
        stagingDiskFileList.put(data[2], Integer.parseInt(data[3])); // key : name of file, value : size  of file
        // add file to staging disk and activeAlways nodes

        for(int i=0; i < noOfActiveAlwaysDisks; i++)
            tmpOpFile.add(op);
        // need to to do nodeTask mapping for these tasks
        if (nodeToTaskMapping.containsKey(stagingDisk)) {
            nodeToTaskMapping.get(stagingDisk).addTask(op);
        } else {
            nodeToTaskMapping.put(stagingDisk, new Tasks(stagingDisk, op));
        }

        for(Node n : ringOfActiveAlwaysDisks.getNodes(data[2]))
        {
            nodeList.add(n);
            if (nodeToTaskMapping.containsKey(n)) {
                nodeToTaskMapping.get(n).addTask(op);
            } else {
                nodeToTaskMapping.put(n, new Tasks(n, op));
            }
        }
    }

    public static void freeUpStagingDiskMemory(String data[], int stagingDiskMemoryUsed, int stagingDiskThresholdMemory, Map<String, Integer> tmpToBeDeletedList,  Map<String, Integer> stagingDiskFileList, int stagingDiskLowerThreshold, int noOfSpunDownDisks, ArrayList<String> tmpOpFile, ArrayList<String> tmpdeleteFile, ArrayList<Node> nodeList, HashMap<Node, Tasks> nodeToTaskMapping, Ring ringOfActiveAlwaysDisks, Ring ringOfSpunDownDisks){
        // remove oldest unused files such after removing them only 60% of stagingDisk mem is occupied
        // before removing add them to respective spun down disks
        Map<String, Integer>  tmpToBeAddedToSpunDownFiles = new LinkedHashMap<String, Integer>();
        int memoryToBeFreed = 0;
        for(String file : stagingDiskFileList.keySet()){
            if(stagingDiskMemoryUsed - memoryToBeFreed - stagingDiskFileList.get(file) > stagingDiskLowerThreshold){
                tmpToBeAddedToSpunDownFiles.put(file, stagingDiskFileList.get(file));
                memoryToBeFreed += stagingDiskFileList.get(file);
            }
            else {
                break;
            }
        }

        for(String file : tmpToBeAddedToSpunDownFiles.keySet()){
            stagingDiskFileList.remove(file);
        }

        // update stagingDiskMemoryUsed after removing old files
        stagingDiskMemoryUsed -= memoryToBeFreed;
        memoryToBeFreed = 0;

        // now add those files to spun down disks
        for(String file : tmpToBeAddedToSpunDownFiles.keySet()){
            for(int i=0; i < noOfSpunDownDisks; i++)
                tmpOpFile.add("PUT,"+data[1]+","+file+","+tmpToBeAddedToSpunDownFiles.get(file)); // tmpOpFile is tmpdataFile for PUT and tmpupdateFile for Update

            for(Node n : ringOfSpunDownDisks.getNodes(data[2]))
            {
                nodeList.add(n);
                if (nodeToTaskMapping.containsKey(n)) {
                    nodeToTaskMapping.get(n).addTask("PUT,"+data[1]+","+file+","+tmpToBeAddedToSpunDownFiles.get(file));
                } else {
                    nodeToTaskMapping.put(n, new Tasks(n, "PUT,"+data[1]+","+file+","+tmpToBeAddedToSpunDownFiles.get(file)));
                }
            }
        }


        // how will you update the spunDown disks of the delete operations done here
        for (String file : tmpToBeDeletedList.keySet()){
            for(int i=0; i < noOfSpunDownDisks; i++)
                tmpdeleteFile.add("DELETE,"+data[1]+","+file);
            for(Node n : ringOfSpunDownDisks.getNodes(data[2]))
            {
                nodeList.add(n);
                if (nodeToTaskMapping.containsKey(n)) {
                    nodeToTaskMapping.get(n).addTask("DELETE,"+data[1]+","+file);
                } else {
                    nodeToTaskMapping.put(n, new Tasks(n, "DELETE,"+data[1]+","+file));
                }
            }
        }
        // (could keep another queue which will be updated on every delete operation and we execute all of them when this case is encountered)


    }

    public static void createOperationFileList(  ArrayList<String> arrivalFile,  ArrayList<String> dataFile,  ArrayList<String> requiredFile,  ArrayList<String> updateFile,  ArrayList<String> deleteFile,  ArrayList<String> tmpdataFile,  ArrayList<String> tmprequiredFile,  ArrayList<String> tmpupdateFile,  ArrayList<String> tmpdeleteFile){

        for(String op : tmpdataFile){
            op = op.trim();
            String data[] = op.split(",");
            arrivalFile.add(data[1]);
            dataFile.add(data[2] + "," + data[3]);
        }

        for(String op : tmprequiredFile){
            op = op.trim();
            String data[] = op.split(",");
            arrivalFile.add(data[1]);
            requiredFile.add(data[2]);
        }

        for(String op : tmpupdateFile){
            op = op.trim();
            String data[] = op.split(",");
            arrivalFile.add(data[1]);
            updateFile.add(data[2] + "," + data[3]);
        }

        for(String op : tmpdeleteFile){
            op = op.trim();
            String data[] = op.split(",");
            arrivalFile.add(data[1]);
            deleteFile.add(data[2]);
        }
    }

    public static Map<String, Integer> addToBeginning( String fileName, int fileSize, Map<String, Integer> fileList){
        Map<String, Integer> stagingDiskFileList = new LinkedHashMap<String, Integer>();
        stagingDiskFileList.put(fileName, fileSize);

        for(String file : fileList.keySet())
        {
            stagingDiskFileList.put(file, fileList.get(file));
        }

        return  stagingDiskFileList;
    }
    public static Map<Integer, ArrayList<String>> getChronologicallyOrderedOperations(String inputLog) throws Exception
    {
        Map<Integer, ArrayList<String>> chronologicallyOrderedOperations = new LinkedHashMap<Integer, ArrayList<String>>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputLog)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String data[] = line.split(",");
                if(chronologicallyOrderedOperations.containsKey(Integer.parseInt(data[1]))){
                    chronologicallyOrderedOperations.get(Integer.parseInt(data[1])).add(line);
                } else {
                    chronologicallyOrderedOperations.put(Integer.parseInt(data[1]), new ArrayList<String>(Arrays.asList(line)));
                }

            }
        }
        return  chronologicallyOrderedOperations;
    }

    public static ArrayList<String> getOperationFileList(String inputLog, String operation, HashMap<Node, Tasks> nodeToTaskMapping, ArrayList<String> arrivalFile, Ring ring, ArrayList<Node> nodeList, int noOfActiveAlwaysDisks) throws Exception
    {
        ArrayList<String> operationFileList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputLog)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String data[] = line.split(",");

                if(operation.equals("GET")){
                    if(data[0].equals("GET")){
                        arrivalFile.add(data[1]);
                        operationFileList.add(data[2]);
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
                else {
                    if((operation.equals("PUT") && data[0].equals("PUT")) || (operation.equals("UPDATE") && data[0].equals("UPDATE"))){
                        for(int i=0; i < noOfActiveAlwaysDisks; i++)
                            arrivalFile.add(data[1]);
                        for(int i=0; i < noOfActiveAlwaysDisks; i++)
                            operationFileList.add(data[2] + "," + data[3]);
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
                    else if(operation.equals("DELETE") && data[0].equals("DELETE")){
                        for(int i=0; i < noOfActiveAlwaysDisks; i++)
                            arrivalFile.add(data[1]);
                        for(int i=0; i < noOfActiveAlwaysDisks; i++)
                            operationFileList.add(data[2]);
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
        }
        return  operationFileList;
    }

    public static Ring getRing(String filename, int noOfNodes, int noOfReplicas, boolean isSpunDown)
    {
        Ring ring = null;

        // HDD specs
        StorageModelHdd[] storageModelHdds = new StorageModelHdd[]{ new StorageModelHddSeagateEnterpriseST6000VN0001() , new StorageModelHddHGSTUltrastarHUC109090CSS600(), new StorageModelHddToshibaEnterpriseMG04SCA500E() };
        PowerModelHdd[] powerModelHdds = new PowerModelHdd[]{ new PowerModeHddSeagateEnterpriseST6000VN0001() , new PowerModeHddHGSTUltrastarHUC109090CSS600() , new PowerModeHddToshibaEnterpriseMG04SCA500E() };

        /*
        // SSD specs
        StorageModelHdd[] storageModelHdds = new StorageModelHdd[]{ new StorageModelSsdToshibaHG6EnterpriseTHNSNJ512GCSU() , new StorageModelSsdSeagate600ProEnterpriseST480FP0021(), new StorageModelSsdIntelDCS3500EnterpriseSC2BB800G401() };
        PowerModelHdd[] powerModelHdds = new PowerModelHdd[]{ new PowerModelSsdToshibaHG6EnterpriseTHNSNJ512GCSU() , new PowerModelSsdSeagate600ProEnterpriseST480FP0021() , new PowerModelSsdIntelDCS3500EnterpriseSC2BB800G401() };
        */
        File file = new File(filename);

        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            int Node_Count, Partition_Power, Replicas;
            Node_Count = noOfNodes;
            Partition_Power = 16;
            Replicas = noOfReplicas;
            HashMap<Integer, Node> hm = new HashMap<Integer, Node>();
            String a[];
            for(int i=0; i<Node_Count; i++)
            {
                a = in.readLine().trim().split(" ");
                int id = Integer.parseInt(a[0]);
                int zone = Integer.parseInt(a[1]);
                double weight = Double.parseDouble(a[2]);

                StorageModelHdd hddModel = storageModelHdds[i%(storageModelHdds.length)]; // model of disks in the persistent storage
                PowerModelHdd hddPowerModel = powerModelHdds[i%(powerModelHdds.length)]; // power model of disks

                hm.put(id, new Node(id, zone, weight, hddModel, hddPowerModel, isSpunDown));
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
