package org.cloudbus.cloudsimdisk.examples.SimulationScenarios;

import org.apache.commons.io.FileUtils;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsimdisk.examples.GenerateDataset.COSBenchTypeWorkloadGenerator;
import org.cloudbus.cloudsimdisk.examples.MyRing.MyNode;
import org.cloudbus.cloudsimdisk.examples.MyRing.MyRing;
import org.cloudbus.cloudsimdisk.examples.MyRunner;
import org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms.MySpinDownRandomAlgorithm;
import org.cloudbus.cloudsimdisk.examples.Tasks;

import java.io.*;
import java.util.*;

/**
 * Created by spadigi on 2/28/17.
 */

/**
 * Add staging disk ring(set of staging disks whose file to disk mapping is similar to that of the Swift Ring involving consistent hashing )
 * with 5% storage capacity of entire Ring. Spin down nodes in ring just once, based on starting files present .
 * Spinning disk down algo : Spin down random disk. Mark which disks cannot be spun down. Spin down another random disk.
 */

public class FlushEntireStagingDiskContents {

    public static void main(String args[]) throws Exception{

        // ==================================================================================================
        // node properties
        int totalNoOfNodes = 32;
        int partitionPower = 4;
        int replicas = 3;
        double overloadPercent = 10.0;

        // staging disk properties
        boolean addStagingDisk = false;

        // node properties
        int noOfReplicas = 3;
        int noOfSpunDownDisks = 1;
        int noOfActiveAlwaysDisks;

        if(addStagingDisk == true) {
            noOfActiveAlwaysDisks = noOfReplicas - noOfSpunDownDisks;
        }
        else {
            noOfActiveAlwaysDisks = noOfReplicas;
        }

        int totalHddRingStorageCapacity = totalNoOfNodes * ((6000000 + 900000 + 5000000) / 3);
        int totalStagingDiskCapacity = (int) (0.05 * totalHddRingStorageCapacity); // 5% capacity
        int avgSSDCapacity = (int) ((800000 + 480000 + 512000) / 3);
        //int noOfStagingDisks =  (int)Math.ceil((double)totalStagingDiskCapacity / avgSSDCapacity);
        int noOfStagingDisks = 1;

        /*
        int totalStagingDiskCapacity = 100; // 5% capacity
        int avgSSDCapacity = 100;
        int noOfStagingDisks = 1;
        */
        MyRing stagingDiskRing = MyRing.buildRing("modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/MyRing/stagingDiskRings.txt",
                noOfStagingDisks
                , 1, 1, 10.0, true);


        // ==================================================================================================

        String ringInputPath = "modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/MyRing/rings.txt";
        MyRing myRing = MyRing.buildRing(ringInputPath, totalNoOfNodes, partitionPower, replicas, overloadPercent, false);

        if (addStagingDisk == true) {

            MySpinDownRandomAlgorithm spinDownRandomAlgorithm = new MySpinDownRandomAlgorithm();
            String startingOperationsInputPath = "modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/" +
                    "examples/SpinDownAlgorithms/smallDataset.txt";
            int numberOfInputLines = 400;
            Map<MyNode, List<String>> nodeToFileList = spinDownRandomAlgorithm.getNodeToFileList(startingOperationsInputPath, myRing, numberOfInputLines);
            spinDownRandomAlgorithm.display(nodeToFileList);

            List<List<MyNode>> result = spinDownRandomAlgorithm.simulate(startingOperationsInputPath, myRing, numberOfInputLines);
            List<String> tmp = new ArrayList<>();

            if(result.size() > 0) {

                for(MyNode n : result.get(result.size()-1))
                {
                    tmp.add(n.getName());
                }

                tmp.sort(Comparator.comparing(String::hashCode));

                System.out.println("Operation No : "+((result.size()-1)+1)+", No of Nodes spunDownAble : "+tmp.size()+" are : "+tmp);
            }

            List<MyNode> ringNodeList = myRing.getAllNodes();
            for (MyNode n : ringNodeList) {
                if (tmp.contains(n.getName())) {
                    n.setSpunDown(false);
                    n.addSpunDownAt(0);
                }
            }

        }

        String inputLog = "files/basic/SimulationScenarios/FlushEntireStagingDiskContentsInputLog.txt";
        COSBenchTypeWorkloadGenerator workloadGenerator = new COSBenchTypeWorkloadGenerator();
        workloadGenerator.generateWorkload("upload intensive", "large", inputLog, totalStagingDiskCapacity, 2 );

        ArrayList<String> arrivalFile = new ArrayList<>();
        ArrayList<MyNode> nodeList = new ArrayList<>();
        HashMap<MyNode, Tasks> nodeToTaskMapping = new HashMap<>();

        ArrayList<String> dataFile = new ArrayList<>();
        ArrayList<String> requiredFile = new ArrayList<>();
        ArrayList<String> updateFile = new ArrayList<>();
        ArrayList<String> deleteFile = new ArrayList<>();

        // if there is no staging disk
        if (addStagingDisk == false) {
            // pass the operation name to this getOperationFileList() method and it will return the op file to be passed to MyRunner
            dataFile = getOperationFileList(inputLog, "PUT", nodeToTaskMapping, arrivalFile, myRing, nodeList, noOfActiveAlwaysDisks);
            requiredFile = getOperationFileList(inputLog, "GET", nodeToTaskMapping, arrivalFile, myRing, nodeList, noOfActiveAlwaysDisks);
            updateFile = getOperationFileList(inputLog, "UPDATE", nodeToTaskMapping, arrivalFile, myRing, nodeList, noOfActiveAlwaysDisks);
            deleteFile = getOperationFileList(inputLog, "DELETE", nodeToTaskMapping, arrivalFile, myRing, nodeList, noOfActiveAlwaysDisks);
        } else {
            // if there a staging disk included
            stagingDiskSimulate(arrivalFile, dataFile, requiredFile, updateFile, deleteFile, inputLog, nodeToTaskMapping, nodeList,
                    noOfActiveAlwaysDisks, myRing, noOfSpunDownDisks, stagingDiskRing);

        }

        // list of all nodes on which operations are to be performed
        ArrayList<MyNode> allNodes = new ArrayList<MyNode>(nodeToTaskMapping.keySet());
        allNodes.sort(Comparator.comparing(MyNode::getName));
        System.out.println("Check the mapping with assignment : ");
        for (MyNode n : allNodes) {
            System.out.println(n.getName() + " : " + nodeToTaskMapping.get(n).getFiles());
        }

        Double totalEnergyConsumed = 0.0;
        // call performOperations() which performs all the CRUD operations as given in input and returns total power consumed
        performOperations(nodeToTaskMapping, arrivalFile, dataFile, requiredFile, updateFile, deleteFile, nodeList);
        //System.out.println("\n\nTotal Energy Consumed : " + totalEnergyConsumed);
    }

    public static void stagingDiskSimulate(ArrayList<String> arrivalFile, ArrayList<String> dataFile, ArrayList<String> requiredFile,
                                           ArrayList<String> updateFile, ArrayList<String> deleteFile, String inputLog,
                                           HashMap<MyNode, Tasks> nodeToTaskMapping, ArrayList<MyNode> nodeList, int noOfActiveAlwaysDisks,
                                           MyRing ring, int noOfSpunDownDisks,
                                           MyRing stagingDiskRing) throws Exception {
        List<MyNode> stagingDiskNodes = stagingDiskRing.getAllNodes();
        Map<MyNode, Integer> stagingDiskMemoryUsed = new LinkedHashMap<MyNode, Integer>();
        Map<MyNode, Integer> stagingDiskUpperThresholdMemory = new LinkedHashMap<MyNode, Integer>();
        Map<MyNode, Integer> stagingDiskLowerThresholdMemory = new LinkedHashMap<MyNode, Integer>();
        Map<MyNode, Map<String, Integer>> stagingDiskFileList = new LinkedHashMap<MyNode, Map<String, Integer>>();
        ArrayList<String> newOperationsSinceLastSpinDown = new ArrayList<>();
        // initialise used memory for all nodes as 0
        for (MyNode n : stagingDiskNodes) {
            stagingDiskMemoryUsed.put(n, 0);
            // initialise stagingDiskLowerThresholdMemory i.e during a flush we keep deleting files till we reach this lower threshold
            stagingDiskLowerThresholdMemory.put(n, (int) (n.getHddModel().getCapacity() * 0.0));
            // initialise stagingDiskUpperThresholdMemory i.e we start the flush when on adding the given file,
            // the storage capacity is going to exceed this upper threshold capacity
            stagingDiskUpperThresholdMemory.put(n, (int) (n.getHddModel().getCapacity() * 0.8));
            stagingDiskFileList.put(n, new HashMap<String, Integer>());
        }


        // get the entire list of operations in chronological order , time : list of operations to be performed at that time mapping
        Map<Integer, ArrayList<String>> chronologicallyOrderedOperations = getChronologicallyOrderedOperations(inputLog);
        ArrayList<String> tmpdataFile = new ArrayList<>();
        ArrayList<String> tmprequiredFile = new ArrayList<>();
        ArrayList<String> tmpupdateFile = new ArrayList<>();
        ArrayList<String> tmpdeleteFile = new ArrayList<>();

        Map<String, Integer> allFilesUploaded = new LinkedHashMap<String, Integer>();
        // those files which have been uploaded at any point of time and are
        // still in system(either on staging or background HDDS). This dictionary having filename as key and file size as value is required in particular
        // scenario when we do a get and file is not in staging disk, so when we get it from always active HDD we need to PUT it into staging disk, for this
        // we need to know the file size. Hence we use this dict to store it
        Map<String, Integer> tmpToBeDeletedList = new LinkedHashMap<String, Integer>();

        // iterate through the operations chronologically
        for (Integer key : chronologicallyOrderedOperations.keySet()) {
            ArrayList<String> operations = chronologicallyOrderedOperations.get(key);
            for (String op : operations) {
                op = op.trim();
                newOperationsSinceLastSpinDown.add(op);
                String data[] = op.split(",");

                MyNode stagingDisk = stagingDiskRing.getPrimaryNodes(data[2]).get(0);
                // for PUT operation
                if (data[0].equals("PUT")) {

                    // this method takes care of all scenarios involved in adding a file, which are :
                    //  1) if disk wont reach max threshold limit on adding this file, then :
                    //          a) add file to staging disk
                    //          b) add file immediately to active always nodes
                    // 2) else if disk will reach max threshold limit on adding this file, then :
                    //          a) retrieve list of all operations to be done on spun down disks, and carry them out(GET and UPDATE are straightforward but
                    // for DELETE we maintain separate list of files deleted since last time staging disk free up was done, and then do those
                    // DELETE operations on spun down disks)
                    //          b) free up staging disk(go on removing least recently used files till occupied space goes below staging disk lower threshold)
                    //          c) then add file to staging disk
                    int memToBeAdded = stagingDiskPutOperation(op, stagingDisk, noOfActiveAlwaysDisks, data, stagingDiskMemoryUsed.get
                                    (stagingDisk),
                            stagingDiskUpperThresholdMemory.get(stagingDisk),
                            tmpToBeDeletedList, stagingDiskFileList.get(stagingDisk), stagingDiskLowerThresholdMemory.get(stagingDisk), noOfSpunDownDisks,
                            tmpdataFile,
                            tmpdeleteFile,
                            nodeList,
                            nodeToTaskMapping, ring, allFilesUploaded, newOperationsSinceLastSpinDown);

                    stagingDiskMemoryUsed.put(stagingDisk, stagingDiskMemoryUsed.get(stagingDisk) + memToBeAdded);
                } else if (data[0].equals("GET")) {
                    // if file in staging disk then fetch from it
                    if (stagingDiskFileList.get(stagingDisk).containsKey(data[2])) {
                        tmprequiredFile.add(op);
                        nodeList.add(stagingDisk);
                        if (nodeToTaskMapping.containsKey(stagingDisk)) {
                            nodeToTaskMapping.get(stagingDisk).addTask(op);
                        } else {
                            nodeToTaskMapping.put(stagingDisk, new Tasks(stagingDisk, op));
                        }
                        int fileSize = stagingDiskFileList.get(stagingDisk).get(data[2]);
                        // in order to support the LRU algo for placement of files on staging disk, we need to keep updating the positioning a file in the
                        // stagingDiskFileList, so we remove this entry and put it in beginning now that we have called an operation on that file
                        // remove from file list
                        stagingDiskFileList.get(stagingDisk).remove(data[2]);
                        // update position of file in list
                        //stagingDiskFileList = addToBeginning(data[2], fileSize, stagingDiskFileList);
                        stagingDiskFileList.get(stagingDisk).put(data[2], fileSize);
                    } else {
                        // if file not on staging disk
                        // go to active always node
                        tmprequiredFile.add(op);
                        List<MyNode> nodes = ring.getPrimaryNodes(data[2]); // get needs only one node
                        MyNode n = nodes.get(0);
                        nodeList.add(n);
                        if (nodeToTaskMapping.containsKey(n)) {
                            nodeToTaskMapping.get(n).addTask(op);
                        } else {
                            nodeToTaskMapping.put(n, new Tasks(n, op));
                        }
                        /*
                        // THIS BLOCK OF CODE IS REQUIRED FOR LRU ONLY
                        // also when file not in stagingDisk and we get it from end node, should we add it to stagingDisk for future use
                        String putOp = "PUT," + data[1] + "," + data[2] + "," + allFilesUploaded.get(data[2]);
                        int memToBeAdded = stagingDiskPutOperation(putOp, stagingDisk, noOfActiveAlwaysDisks, putOp.split(","), stagingDiskMemoryUsed.get
                                        (stagingDisk),
                                stagingDiskUpperThresholdMemory.get(stagingDisk),
                                tmpToBeDeletedList, stagingDiskFileList.get(stagingDisk), stagingDiskLowerThresholdMemory.get(stagingDisk), noOfSpunDownDisks,
                                tmpdataFile,
                                tmpdeleteFile,
                                nodeList,
                                nodeToTaskMapping, ring, allFilesUploaded, newOperationsSinceLastSpinDown);

                        stagingDiskMemoryUsed.put(stagingDisk, stagingDiskMemoryUsed.get(stagingDisk) + memToBeAdded);
                        */
                    }
                } else if (data[0].equals("UPDATE")) {
                    // if old version also present, then remove old version before proceeding
                    if (stagingDiskFileList.get(stagingDisk).containsKey(data[2])) {
                        int memoryToBeFreed = stagingDiskFileList.get(stagingDisk).get(data[2]);
                        stagingDiskFileList.get(stagingDisk).remove(data[2]);
                        stagingDiskMemoryUsed.put(stagingDisk, stagingDiskMemoryUsed.get(stagingDisk) - memoryToBeFreed);

                    }
                    // now same as PUT

                    int memToBeAdded = stagingDiskPutOperation(op, stagingDisk, noOfActiveAlwaysDisks, data,
                            stagingDiskMemoryUsed.get(stagingDisk),
                            stagingDiskUpperThresholdMemory.get(stagingDisk),
                            tmpToBeDeletedList, stagingDiskFileList.get(stagingDisk), stagingDiskLowerThresholdMemory.get(stagingDisk), noOfSpunDownDisks,
                            tmpupdateFile,
                            tmpdeleteFile,
                            nodeList,
                            nodeToTaskMapping, ring, allFilesUploaded, newOperationsSinceLastSpinDown);
                } else if (data[0].equals("DELETE")) {
                    // delete everywhere
                    // if file in staging disk, then remove it and make note that it has been removed so that we can remove it from spun Down disk as well
                    // during the next staging disk free up
                    if (stagingDiskFileList.get(stagingDisk).containsKey(data[2])) {
                        int memoryToBeFreed = stagingDiskFileList.get(stagingDisk).get(data[2]);
                        stagingDiskFileList.get(stagingDisk).remove(data[2]);
                        stagingDiskMemoryUsed.put(stagingDisk, stagingDiskMemoryUsed.get(stagingDisk) - memoryToBeFreed);
                        // remove from staging disk
                        tmpdeleteFile.add(op);
                        nodeList.add(stagingDisk);
                        if (nodeToTaskMapping.containsKey(stagingDisk)) {
                            nodeToTaskMapping.get(stagingDisk).addTask(op);
                        } else {
                            nodeToTaskMapping.put(stagingDisk, new Tasks(stagingDisk, op));
                        }
                    }
                    // remove from active always disks
                    for (int i = 0; i < noOfActiveAlwaysDisks; i++)
                        tmpdeleteFile.add(op);
                    for (MyNode n : ring.getPrimaryNodes(data[2]).subList(0,noOfActiveAlwaysDisks)) {
                        nodeList.add(n);
                        if (nodeToTaskMapping.containsKey(n)) {
                            nodeToTaskMapping.get(n).addTask(op);
                        } else {
                            nodeToTaskMapping.put(n, new Tasks(n, op));
                        }
                    }

                    // how to take care of delete on spunDown disks coz file will be removed from stagingDisk and old version could continue existing on
                    // spunDown disk
                    tmpToBeDeletedList.put(data[2], Integer.parseInt(data[1]));

                }
            }
        }

        // finally now that we hav all tmp op files, we send them and get the op files which can be passed to MyRunner
        createOperationFileList(arrivalFile, dataFile, requiredFile, updateFile, deleteFile, tmpdataFile, tmprequiredFile, tmpupdateFile, tmpdeleteFile);
    }

    // this method takes care of all actions to be taken when there is a PUT operation required to be done when there is a staging disk

    public static int stagingDiskPutOperation(String op, MyNode stagingDisk, int noOfActiveAlwaysDisks, String data[], int stagingDiskMemoryUsed,
                                              int stagingDiskThresholdMemory, Map<String, Integer> tmpToBeDeletedList,
                                              Map<String, Integer> stagingDiskFileList, int stagingDiskLowerThreshold,
                                              int noOfSpunDownDisks, ArrayList<String> tmpOpFile, ArrayList<String> tmpdeleteFile,
                                              ArrayList<MyNode> nodeList, HashMap<MyNode, Tasks> nodeToTaskMapping, MyRing ring, Map<String, Integer>
                                                      allFilesUploaded, ArrayList<String> newOperationsSinceLastSpinDown) {
        int stagingDiskMemoryToBeAdded = 0;
        // if staging disk occupied more the stagingDiskMemoryUsed upper threshold
        if (stagingDiskMemoryUsed + Integer.parseInt(data[3]) > stagingDiskThresholdMemory) {
            stagingDiskMemoryToBeAdded += freeUpStagingDiskMemory(data, stagingDiskMemoryUsed, stagingDiskThresholdMemory, tmpToBeDeletedList,
                    stagingDiskFileList,
                    stagingDiskLowerThreshold, noOfSpunDownDisks, tmpOpFile, tmpdeleteFile, nodeList, nodeToTaskMapping, ring, newOperationsSinceLastSpinDown);
        }
        // enough space in staging disk now
        tmpOpFile.add(op);
        nodeList.add(stagingDisk);

        stagingDiskMemoryToBeAdded += Integer.parseInt(data[3]);

        //stagingDiskFileList.put()
        // add to stagingDiskFileList
        stagingDiskFileList.put(data[2], Integer.parseInt(data[3])); // key : name of file, value : size  of file
        // add file to staging disk and activeAlways nodes

        for (int i = 0; i < noOfActiveAlwaysDisks; i++)
            tmpOpFile.add(op);
        // need to do nodeTask mapping for these tasks
        if (nodeToTaskMapping.containsKey(stagingDisk)) {
            nodeToTaskMapping.get(stagingDisk).addTask(op);
        } else {
            nodeToTaskMapping.put(stagingDisk, new Tasks(stagingDisk, op));
        }

        for (MyNode n : ring.getPrimaryNodes(data[2]).subList(0,noOfActiveAlwaysDisks)) {
            nodeList.add(n);
            if (nodeToTaskMapping.containsKey(n)) {
                nodeToTaskMapping.get(n).addTask(op);
            } else {
                nodeToTaskMapping.put(n, new Tasks(n, op));
            }
        }

        // add file to allFilesUploaded file list
        allFilesUploaded.put(data[2], Integer.parseInt(data[3])); // this takes care of both operations(update and add)
        return stagingDiskMemoryToBeAdded;

    }

    public static int freeUpStagingDiskMemory(String data[], int stagingDiskMemoryUsed, int stagingDiskThresholdMemory,
                                              Map<String, Integer> tmpToBeDeletedList, Map<String, Integer> stagingDiskFileList,
                                              int stagingDiskLowerThreshold, int noOfSpunDownDisks, ArrayList<String> tmpOpFile,
                                              ArrayList<String> tmpdeleteFile, ArrayList<MyNode> nodeList, HashMap<MyNode, Tasks> nodeToTaskMapping,
                                              MyRing ring, ArrayList<String> newOperationsSinceLastSpinDown) {
        // remove oldest unused files such after removing them only 60% of stagingDisk mem is occupied
        // before removing add them to respective spun down disks
        Map<String, Integer> tmpToBeAddedToSpunDownFiles = new LinkedHashMap<String, Integer>();
        int memoryToBeFreed = 0;
        for (String file : stagingDiskFileList.keySet()) {
            tmpToBeAddedToSpunDownFiles.put(file, stagingDiskFileList.get(file));
            memoryToBeFreed += stagingDiskFileList.get(file);
            if (stagingDiskMemoryUsed - memoryToBeFreed <= stagingDiskLowerThreshold) {
                break;
            }
        }

        for (String file : tmpToBeAddedToSpunDownFiles.keySet()) {
            stagingDiskFileList.remove(file);
        }

        // update stagingDiskMemoryUsed after removing old files
        int stagingDiskMemoryToBeAdded = 0;
        stagingDiskMemoryToBeAdded -= memoryToBeFreed;

        // now add those files to spun down disks
        for (String file : tmpToBeAddedToSpunDownFiles.keySet()) {
            for (int i = 0; i < noOfSpunDownDisks; i++)
                tmpOpFile.add("PUT," + data[1] + "," + file + "," + tmpToBeAddedToSpunDownFiles.get(file)); // tmpOpFile is tmpdataFile for PUT and
            // tmpupdateFile for Update

            for (MyNode n : ring.getPrimaryNodes(data[2]).subList(ring.getPrimaryNodes(data[2]).size() - noOfSpunDownDisks,ring.getPrimaryNodes(data[2]).size())) {
                nodeList.add(n);
                if (nodeToTaskMapping.containsKey(n)) {
                    nodeToTaskMapping.get(n).addTask("PUT," + data[1] + "," + file + "," + tmpToBeAddedToSpunDownFiles.get(file));
                } else {
                    nodeToTaskMapping.put(n, new Tasks(n, "PUT," + data[1] + "," + file + "," + tmpToBeAddedToSpunDownFiles.get(file)));
                }
            }
        }

/*
        // delete on handoff
        for (String file : tmpToBeAddedToSpunDownFiles.keySet()) {
            ArrayList<MyNode> handOffNodes = new ArrayList<>(ring.getHandOffNodes(file));
            if (handOffNodes.size()>0){
                for(MyNode handOffNode : handOffNodes){
                    nodeList.add(handOffNode);
                    if(nodeToTaskMapping.containsKey(handOffNode)){
                        nodeToTaskMapping.get(handOffNode).addTask("DELETE,"+data[1]+","+file);
                    } else {
                        nodeToTaskMapping.put(handOffNode, new Tasks(handOffNode, "DELETE," + data[1] + "," + file));
                    }
                }
            }
        }*/

        // update the spunDown disks of the delete operations done here
        for (String file : tmpToBeDeletedList.keySet()) {
            for (int i = 0; i < noOfSpunDownDisks; i++)
                tmpdeleteFile.add("DELETE," + data[1] + "," + file);
            for (MyNode n : ring.getPrimaryNodes(data[2]).subList(ring.getPrimaryNodes(data[2]).size() - noOfSpunDownDisks,ring.getPrimaryNodes(data[2]).size())) {
                nodeList.add(n);
                if (nodeToTaskMapping.containsKey(n)) {
                    nodeToTaskMapping.get(n).addTask("DELETE," + data[1] + "," + file);
                } else {
                    nodeToTaskMapping.put(n, new Tasks(n, "DELETE," + data[1] + "," + file));
                }
            }
        }

        /*
        // THIS BLOCK OF CODE IS REQUIRED ONLY WHEN WE NEED TO SPIN UP THE DISKS AFTER EACH FLUSH, AND REDISTRIBUTE THE FILES AND SPIN DOWN AGAIN
        // THIS WAS REQUIRED ONLY WHEN HANDOFF NODES WERE THERE, MIGHT NOT BE REQUIRED NOW, SO JUST COMMENTING IT OUT
        // spin all up
        // note the time at which they were spun up
        for(MyNode n : ring.getAllNodes()){
            n.setSpunDown(false);
            n.setSpunUpAt(Integer.parseInt(data[1]));
        }

        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            // write all operations performed since last spin down into new start file list file
            File file = new File("files/basic/StagingDiskRingAndSpinDownRandomAlgo1/startingFileList.txt");
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            for(String opToBePerformed : newOperationsSinceLastSpinDown)
                bw.write(opToBePerformed + "\n");
        }catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }
        }
        */

        return stagingDiskMemoryToBeAdded;

    }

    // simply returns the op files in required format
    public static void createOperationFileList(ArrayList<String> arrivalFile, ArrayList<String> dataFile, ArrayList<String> requiredFile, ArrayList<String>
            updateFile, ArrayList<String> deleteFile, ArrayList<String> tmpdataFile, ArrayList<String> tmprequiredFile, ArrayList<String> tmpupdateFile,
                                               ArrayList<String> tmpdeleteFile) {

        for (String op : tmpdataFile) {
            op = op.trim();
            String data[] = op.split(",");
            arrivalFile.add(data[1]);
            dataFile.add(data[2] + "," + data[3]);
        }

        for (String op : tmprequiredFile) {
            op = op.trim();
            String data[] = op.split(",");
            arrivalFile.add(data[1]);
            requiredFile.add(data[2]);
        }

        for (String op : tmpupdateFile) {
            op = op.trim();
            String data[] = op.split(",");
            arrivalFile.add(data[1]);
            updateFile.add(data[2] + "," + data[3]);
        }

        for (String op : tmpdeleteFile) {
            op = op.trim();
            String data[] = op.split(",");
            arrivalFile.add(data[1]);
            deleteFile.add(data[2]);
        }
    }


    // return map of time to operation mapping where keys indicating time are chronologically ordered
    public static Map<Integer, ArrayList<String>> getChronologicallyOrderedOperations(String inputLog) throws Exception {
        Map<Integer, ArrayList<String>> chronologicallyOrderedOperations = new LinkedHashMap<Integer, ArrayList<String>>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputLog)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String data[] = line.split(",");
                if (chronologicallyOrderedOperations.containsKey(Integer.parseInt(data[1]))) {
                    chronologicallyOrderedOperations.get(Integer.parseInt(data[1])).add(line);
                } else {
                    chronologicallyOrderedOperations.put(Integer.parseInt(data[1]), new ArrayList<String>(Arrays.asList(line)));
                }



            }
        }
        return chronologicallyOrderedOperations;
    }

    // get op list when staging disk is not involved
    public static ArrayList<String> getOperationFileList(String inputLog, String operation, HashMap<MyNode, Tasks> nodeToTaskMapping, ArrayList<String>
            arrivalFile, MyRing ring, ArrayList<MyNode> nodeList, int noOfActiveAlwaysDisks) throws Exception {
        ArrayList<String> operationFileList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputLog)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String data[] = line.split(",");

                if (operation.equals("GET")) {
                    if (data[0].equals("GET")) {
                        arrivalFile.add(data[1]);
                        operationFileList.add(data[2]);
                        List<MyNode> nodes = ring.getPrimaryNodes(data[2]);
                        MyNode n = nodes.get(0);
                        nodeList.add(n);
                        if (nodeToTaskMapping.containsKey(n)) {
                            nodeToTaskMapping.get(n).addTask(line);
                        } else {
                            nodeToTaskMapping.put(n, new Tasks(n, line));
                        }
                    }
                } else {
                    if ((operation.equals("PUT") && data[0].equals("PUT")) || (operation.equals("UPDATE") && data[0].equals("UPDATE"))) {
                        for (int i = 0; i < noOfActiveAlwaysDisks; i++)
                            arrivalFile.add(data[1]);
                        for (int i = 0; i < noOfActiveAlwaysDisks; i++)
                            operationFileList.add(data[2] + "," + data[3]);
                        for (MyNode n : ring.getPrimaryNodes(data[2])) {
                            nodeList.add(n);
                            if (nodeToTaskMapping.containsKey(n)) {
                                nodeToTaskMapping.get(n).addTask(line);
                            } else {
                                nodeToTaskMapping.put(n, new Tasks(n, line));
                            }
                        }
                    } else if (operation.equals("DELETE") && data[0].equals("DELETE")) {
                        for (int i = 0; i < noOfActiveAlwaysDisks; i++)
                            arrivalFile.add(data[1]);
                        for (int i = 0; i < noOfActiveAlwaysDisks; i++)
                            operationFileList.add(data[2]);
                        for (MyNode n : ring.getPrimaryNodes(data[2])) {
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
        return operationFileList;
    }


    // does the task of send the cloudlets and starting the simulation
    public static void performOperations(HashMap<MyNode, Tasks> nodeToTaskMapping, ArrayList<String> arrivalFile, ArrayList<String> dataFile,
                                           ArrayList<String> requiredFile, ArrayList<String> updateFile, ArrayList<String> deleteFile, ArrayList<MyNode>
                                                   nodeList) throws Exception {
        Runnable monitor = new Runnable() {
            @Override
            public void run() {
                while (CloudSim.clock() <= CloudSim.lifeLength) {
                    if (CloudSim.isPaused()) {
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

        for (int i = 0; i < arrivalFile.size(); i++) {
            if (i > 0)
                arrivalTimes.append("\n");
            arrivalTimes.append(arrivalFile.get(i));
        }
        for (int i = 0; i < dataFile.size(); i++) {
            if (i > 0)
                putOpData.append("\n");
            putOpData.append(dataFile.get(i));
        }
        for (int i = 0; i < requiredFile.size(); i++) {
            if (i > 0)
                getOpData.append("\n");
            getOpData.append(requiredFile.get(i));
        }
        for (int i = 0; i < updateFile.size(); i++) {
            if (i > 0)
                updateOpData.append("\n");
            updateOpData.append(updateFile.get(i));
        }
        for (int i = 0; i < deleteFile.size(); i++) {
            if (i > 0)
                deleteOpData.append("\n");
            deleteOpData.append(deleteFile.get(i));
        }

        FileUtils.writeStringToFile(new File("files/" + arrival), arrivalTimes.toString());
        FileUtils.writeStringToFile(new File("files/" + putData), putOpData.toString());
        FileUtils.writeStringToFile(new File("files/" + getData), getOpData.toString());
        FileUtils.writeStringToFile(new File("files/" + updateData), updateOpData.toString());
        FileUtils.writeStringToFile(new File("files/" + deleteData), deleteOpData.toString());

        MyRunner run = new MyRunner(nodeToTaskMapping, arrival, putData, getData, updateData, deleteData, nodeList);
        //System.out.println("Energy Consumed : " + run.getTotalStorageEnergyConsumed() + " Joules()");
        //return run.getTotalStorageEnergyConsumed();
    }
}
