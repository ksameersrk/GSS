package org.cloudbus.cloudsimdisk.examples.SimulationScenarios;

import org.apache.commons.io.FileUtils;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsimdisk.examples.GenerateDataset.COSBenchTypeWorkloadGenerator;
import org.cloudbus.cloudsimdisk.examples.MyRing.MyNode;
import org.cloudbus.cloudsimdisk.examples.MyRing.MyRing;
import org.cloudbus.cloudsimdisk.examples.MyRunner;
import org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms.MySpinDownOptimalAlgorithm;
import org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms.OptimalHelper;
import org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms.MySpinDownRandomAlgorithm;
import org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms.StartingFileListGenerator;
import org.cloudbus.cloudsimdisk.examples.Tasks;
import org.cloudbus.cloudsimdisk.util.WriteToLogFile;

import java.io.*;
import java.util.*;

import static org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms.MySpinDownOptimalAlgorithm.*;

/**
 * Created by spadigi on 2/28/17.
 */

/**
 * Add staging disk ring(set of staging disks whose file to disk mapping is similar to that of the Swift Ring involving consistent hashing )
 * with 5% storage capacity of entire Ring. Spin down nodes in ring just once, based on starting files present .
 * Spinning disk down algo : Spin down random disk. Mark which disks cannot be spun down. Spin down another random disk.
 */

public class FlushEntireStagingDiskContents {
    public static boolean debug = false;
    //public static void main(String args[]) throws Exception{
    public static MyRunner startSimulation(int totalNoOfNodes, boolean addStagingDisk, int numberOfOperations, int predefindedWorkloadNumber, int noOfReplicas,
                                       String cachingMechanism, int HDDType, int SSDType,
                                       int percentageFlushAt, int percentageFlushTill, boolean realisticSSD, String pathToWorkload, String pathToStartingFileList,
                                       String pathToInputLog, boolean generateInputLog) throws Exception{

        // ==================================================================================================
        // node properties
        //int totalNoOfNodes = 16;
        int partitionPower = 4;
        int replicas = noOfReplicas;
        double overloadPercent = 10.0;

        // staging disk properties
        //boolean addStagingDisk = true;

        // node properties
        //int noOfReplicas = 3;
        int noOfSpunDownDisks = 1;
        int noOfActiveAlwaysDisks;

        if(generateInputLog == true) {
            StartingFileListGenerator startingFileListGenerator = new StartingFileListGenerator();
            startingFileListGenerator.generateStartingFile(pathToWorkload, pathToStartingFileList, pathToInputLog);
        }
        if(addStagingDisk == true) {
            noOfActiveAlwaysDisks = noOfReplicas - noOfSpunDownDisks;
        }
        else {
            noOfActiveAlwaysDisks = noOfReplicas;
        }

        int totalHddRingStorageCapacity, totalStagingDiskCapacity, avgSSDCapacity, noOfStagingDisks = 0;
        if(realisticSSD) {
            int[] HDDCapacities = {6000000, 900000, 5000000};
            int[] SSDCapacities = {512000, 480000, 800000};
            totalHddRingStorageCapacity = totalNoOfNodes * (HDDCapacities[HDDType%3]);
            totalStagingDiskCapacity = (int) (0.05 * totalHddRingStorageCapacity); // 5% capacity
            avgSSDCapacity = SSDCapacities[SSDType%3];
            noOfStagingDisks =  (int)Math.ceil((double)totalStagingDiskCapacity / avgSSDCapacity);
            //int noOfStagingDisks = 1;
        }
        else {
            // this section requires us to go and set the capacity of that type of SSD in StorageModelSSD* classes to be equal to totalStagingDiskCapacity
            int[] HDDCapacities = {6000000, 900000, 5000000};
            int[] SSDCapacities = { 512000, 480000,  800000};
            totalHddRingStorageCapacity = totalNoOfNodes * (HDDCapacities[HDDType%3]);
            totalStagingDiskCapacity = (int) (0.05 * totalHddRingStorageCapacity); // 5% capacity

            // for quick execution
            totalStagingDiskCapacity = 100; // 5% capacity
            avgSSDCapacity = 100;

            noOfStagingDisks = 1;
        }

        MyRing stagingDiskRing = MyRing.buildRing("modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/MyRing/stagingDiskRings.txt",
                noOfStagingDisks
                , 1, 1, 10.0, true, SSDType);

        WriteToLogFile.AddtoFile(String.format("%8sTotal no. of HDDs = %10d ", "", totalNoOfNodes));
        // ==================================================================================================

        String ringInputPath = "modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/MyRing/rings.txt";
        MyRing myRing = MyRing.buildRing(ringInputPath, totalNoOfNodes, partitionPower, replicas, overloadPercent, false, HDDType);

        if (addStagingDisk == true) {
            /*
            MySpinDownRandomAlgorithm spinDownRandomAlgorithm = new MySpinDownRandomAlgorithm();
            String startingOperationsInputPath = "files/basic/operations/startingFileList.txt";

            int numberOfInputLines = 99;
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
                WriteToLogFile.AddtoFile(String.format("%8s Operation No. : %10d, No of Nodes spunDownAble :  %10d . They are : %s", "", ((result.size()-1)
                        +1), tmp.size(), tmp));
            }

            List<MyNode> ringNodeList = myRing.getAllNodes();
            for (MyNode n : ringNodeList) {
                if (tmp.contains(n.getName())) {
                    n.setSpunDown(true);
                    //n.addSpunDownAt(0);
                }
            }
            */

            MySpinDownOptimalAlgorithm spinDownOptimalAlgorithm = new MySpinDownOptimalAlgorithm();
            int numberOfPartition = (int)myRing.getNumberOfPartitions();
            spinDownOptimalAlgorithm.displayNodeMap(myRing.getNodeToPartition());
            Map<MyNode, List<Integer>> newMap = spinDownOptimalAlgorithm.getSortedNodeMap(myRing.getNodeToPartition(), numberOfPartition);
//            for(MyNode myNode : newMap.keySet())
//            {
//                System.out.println(myNode+" : "+newMap.get(myNode));
//            }

            OptimalHelper optimalHelper = new OptimalHelper();
            optimalHelper.setMaxNodes(0);
            optimalHelper.setNodes(new ArrayList<>());
            System.out.println("Results : ");
            spinDownOptimalAlgorithm.findOptimalSolution(powerSet(newMap.keySet()), optimalHelper, newMap, numberOfPartition);
            System.out.println("Spun Down Disks : " + optimalHelper);

            List<MyNode> spunDownNodes = optimalHelper.getNodes();

            for (MyNode n : myRing.getAllNodes()) {
                if (spunDownNodes.contains(n)) {
                    n.setSpunDown(true);
                    //n.addSpunDownAt(0);
                }
            }
        }

        //String inputLog = "files/basic/SimulationScenarios/FlushEntireStagingDiskContentsInputLog.txt";
        //String inputLog = pathToInputLog;
//        COSBenchTypeWorkloadGenerator workloadGenerator = new COSBenchTypeWorkloadGenerator();
//        workloadGenerator.generateWorkload("download intensive", "large", inputLog, totalStagingDiskCapacity, 0.2 );

        ArrayList<String> arrivalFile = new ArrayList<>();
        ArrayList<MyNode> nodeList = new ArrayList<>();
        HashMap<MyNode, Tasks> nodeToTaskMapping = new HashMap<>();

        ArrayList<String> dataFile = new ArrayList<>();
        ArrayList<String> requiredFile = new ArrayList<>();
        ArrayList<String> updateFile = new ArrayList<>();
        ArrayList<String> deleteFile = new ArrayList<>();

        // if there is no staging disk
        if (addStagingDisk == false) {
            WriteToLogFile.AddtoFile("Staging Disk : False");
            // pass the operation name to this getOperationFileList() method and it will return the op file to be passed to MyRunner
            dataFile = getOperationFileList(pathToInputLog, "PUT", nodeToTaskMapping, arrivalFile, myRing, nodeList, noOfActiveAlwaysDisks, numberOfOperations);
            requiredFile = getOperationFileList(pathToInputLog, "GET", nodeToTaskMapping, arrivalFile, myRing, nodeList, noOfActiveAlwaysDisks, numberOfOperations);
            updateFile = getOperationFileList(pathToInputLog, "UPDATE", nodeToTaskMapping, arrivalFile, myRing, nodeList, noOfActiveAlwaysDisks, numberOfOperations);
            deleteFile = getOperationFileList(pathToInputLog, "DELETE", nodeToTaskMapping, arrivalFile, myRing, nodeList, noOfActiveAlwaysDisks,
                    numberOfOperations);
        } else {
            WriteToLogFile.AddtoFile("Staging Disk : True");
            // if there a staging disk included
            stagingDiskSimulate(arrivalFile, dataFile, requiredFile, updateFile, deleteFile, pathToInputLog, nodeToTaskMapping, nodeList,
                    noOfActiveAlwaysDisks, myRing, noOfSpunDownDisks, stagingDiskRing, percentageFlushAt, percentageFlushTill, cachingMechanism,
                    numberOfOperations, pathToStartingFileList);

        }

        // list of all nodes on which operations are to be performed
        ArrayList<MyNode> allNodes = new ArrayList<MyNode>(myRing.getAllNodes());
        if(addStagingDisk == true)
            allNodes.addAll(stagingDiskRing.getAllNodes());
        allNodes.sort(Comparator.comparing(MyNode::getName));
        System.out.println("Check the mapping with assignment : ");
        for (MyNode n : allNodes) {
            if (nodeToTaskMapping.containsKey(n)) {
                System.out.println(n.getName() + " : " + nodeToTaskMapping.get(n).getFiles());
            }
        }

        Double totalEnergyConsumed = 0.0;
        // call performOperations() which performs all the CRUD operations as given in input and returns total power consumed
        MyRunner runner = performOperations(arrivalFile, dataFile, requiredFile, updateFile, deleteFile, nodeList, myRing, stagingDiskRing,
                pathToStartingFileList, addStagingDisk);

        return runner;
        //System.out.println("\n\nTotal Energy Consumed : " + totalEnergyConsumed);


    }

    public static void updateAllFilesUploadedWithStartingFileList(Map<String, Double> allFilesUploaded, String pathToStartingFileList, int numberOfOperations)
            throws IOException{
        // key : filenaem, value : filesize
        try (BufferedReader br = new BufferedReader(new FileReader(new File(pathToStartingFileList)))) {
            int count = 0;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String data[] = line.split(",");
                allFilesUploaded.put(data[0], Double.parseDouble(data[1]));
                if(numberOfOperations > 0){
                    if(count == numberOfOperations){
                        break;
                    }
                    else {
                        count = count + 1;
                    }
                }
            }
        }

    }

    public static void stagingDiskSimulate(ArrayList<String> arrivalFile, ArrayList<String> dataFile, ArrayList<String> requiredFile,
                                           ArrayList<String> updateFile, ArrayList<String> deleteFile, String inputLog,
                                           HashMap<MyNode, Tasks> nodeToTaskMapping, ArrayList<MyNode> nodeList, int noOfActiveAlwaysDisks,
                                           MyRing ring, int noOfSpunDownDisks,
                                           MyRing stagingDiskRing, int percentageToFlushAt, int percentageToFlushTill, String cachingMechanism, int
                                                   noOfOperations, String pathToStartingFileList) throws
            Exception {
        List<MyNode> stagingDiskNodes = stagingDiskRing.getAllNodes();
        Map<MyNode, Double> stagingDiskMemoryUsed = new LinkedHashMap<MyNode, Double>();
        Map<MyNode, Double> stagingDiskUpperThresholdMemory = new LinkedHashMap<MyNode, Double>();
        Map<MyNode, Double> stagingDiskLowerThresholdMemory = new LinkedHashMap<MyNode, Double>();
        Map<MyNode, Map<String, Double>> stagingDiskFileList = new LinkedHashMap<MyNode, Map<String, Double>>();
        ArrayList<String> newOperationsSinceLastSpinDown = new ArrayList<>();
        // initialise used memory for all nodes as 0
        for (MyNode n : stagingDiskNodes) {
            stagingDiskMemoryUsed.put(n, 0.0);
            // initialise stagingDiskLowerThresholdMemory i.e during a flush we keep deleting files till we reach this lower threshold
            Double lowerThreshold = (percentageToFlushTill*1.0)/10;
            stagingDiskLowerThresholdMemory.put(n, (Double) (n.getHddModel().getCapacity() * lowerThreshold));
            // initialise stagingDiskUpperThresholdMemory i.e we start the flush when on adding the given file,
            // the storage capacity is going to exceed this upper threshold capacity
            Double upperThreshold = (percentageToFlushAt*1.0)/10;
            stagingDiskUpperThresholdMemory.put(n, (Double) (n.getHddModel().getCapacity() * upperThreshold));
            stagingDiskFileList.put(n, new HashMap<String, Double>());

            //System.out.println("Flushing when upper threshold of " + upperThreshold.toString() + " is reached.");
            WriteToLogFile.AddtoFile(String.format("%8sFlushing when upper threshold of %9.3f is reached", "", upperThreshold));
            WriteToLogFile.AddtoFile(String.format("%8sFlushing till lower threshold of %9.3f is reached", "", lowerThreshold));

        }


        // get the entire list of operations in chronological order , time : list of operations to be performed at that time mapping
        Map<Double, ArrayList<String>> chronologicallyOrderedOperations = getChronologicallyOrderedOperations(inputLog, noOfOperations);
        ArrayList<String> tmpdataFile = new ArrayList<>();
        ArrayList<String> tmprequiredFile = new ArrayList<>();
        ArrayList<String> tmpupdateFile = new ArrayList<>();
        ArrayList<String> tmpdeleteFile = new ArrayList<>();

        Map<String, Double> allFilesUploaded = new LinkedHashMap<String, Double>();
        // those files which have been uploaded at any point of time and are
        // still in system(either on staging or background HDDS). This dictionary having filename as key and file size as value is required in particular
        // scenario when we do a get and file is not in staging disk, so when we get it from always active HDD we need to PUT it into staging disk, for this
        // we need to know the file size. Hence we use this dict to store it
        Map<String, Double> tmpToBeDeletedList = new LinkedHashMap<String, Double>();

        //update allFilesUploaded with startingFileList
        updateAllFilesUploadedWithStartingFileList(allFilesUploaded, pathToStartingFileList, noOfOperations);

        // iterate through the operations chronologically
        for (Double key : chronologicallyOrderedOperations.keySet()) {
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
                    Double memToBeAdded = stagingDiskPutOperation(op, stagingDisk, noOfActiveAlwaysDisks, data, stagingDiskMemoryUsed.get
                                    (stagingDisk),
                            stagingDiskUpperThresholdMemory.get(stagingDisk),
                            tmpToBeDeletedList, stagingDiskFileList.get(stagingDisk), stagingDiskLowerThresholdMemory.get(stagingDisk), noOfSpunDownDisks,
                            tmpdataFile,
                            tmpdeleteFile,
                            nodeList,
                            nodeToTaskMapping, ring, allFilesUploaded, newOperationsSinceLastSpinDown, tmprequiredFile);

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
                        Double fileSize = stagingDiskFileList.get(stagingDisk).get(data[2]);

                        if(cachingMechanism.equals("LRU")) {
                            // in order to support the LRU algo for placement of files on staging disk, we need to keep updating the positioning a file in the
                            // stagingDiskFileList, so we remove this entry and put it in beginning now that we have called an operation on that file
                            // remove from file list
                            stagingDiskFileList.get(stagingDisk).remove(data[2]);
                            // update position of file in list
                            //stagingDiskFileList = addToBeginning(data[2], fileSize, stagingDiskFileList);
                            stagingDiskFileList.get(stagingDisk).put(data[2], fileSize);
                        }
                    } else {
                        // if file not on staging disk
                        // go to active always node
                        tmprequiredFile.add(op);
                        List<MyNode> nodes = ring.getPrimaryNodes(data[2]); // get needs only one node
                        for(MyNode n : nodes){
                            if(n.isSpunDown() == false)
                            {
                                nodeList.add(n);
                                if (nodeToTaskMapping.containsKey(n)) {
                                    nodeToTaskMapping.get(n).addTask(op);
                                } else {
                                    nodeToTaskMapping.put(n, new Tasks(n, op));
                                }
                                break;
                            }
                        }



                        // bcoz file was not in staging disk, we now add it to staging disk

                        String putOp = "PUT," + data[1] + "," + data[2] + "," + allFilesUploaded.get(data[2]);
                        Double memToBeAdded = stagingDiskPutOperation(putOp, stagingDisk, noOfActiveAlwaysDisks, putOp.split(","), stagingDiskMemoryUsed.get
                                        (stagingDisk),
                                stagingDiskUpperThresholdMemory.get(stagingDisk),
                                tmpToBeDeletedList, stagingDiskFileList.get(stagingDisk), stagingDiskLowerThresholdMemory.get(stagingDisk), noOfSpunDownDisks,

                                tmpdataFile,
                                tmpdeleteFile,
                                nodeList,
                                nodeToTaskMapping, ring, allFilesUploaded, newOperationsSinceLastSpinDown, tmprequiredFile);

                        stagingDiskMemoryUsed.put(stagingDisk, stagingDiskMemoryUsed.get(stagingDisk) + memToBeAdded);


                    }
                } else if (data[0].equals("UPDATE")) {
                    // if old version also present, then remove old version before proceeding
                    if (stagingDiskFileList.get(stagingDisk).containsKey(data[2])) {
                        Double memoryToBeFreed = stagingDiskFileList.get(stagingDisk).get(data[2]);
                        stagingDiskFileList.get(stagingDisk).remove(data[2]);
                        stagingDiskMemoryUsed.put(stagingDisk, stagingDiskMemoryUsed.get(stagingDisk) - memoryToBeFreed);

                    }
                    // now same as PUT

                    Double memToBeAdded = stagingDiskPutOperation(op, stagingDisk, noOfActiveAlwaysDisks, data,
                            stagingDiskMemoryUsed.get(stagingDisk),
                            stagingDiskUpperThresholdMemory.get(stagingDisk),
                            tmpToBeDeletedList, stagingDiskFileList.get(stagingDisk), stagingDiskLowerThresholdMemory.get(stagingDisk), noOfSpunDownDisks,
                            tmpupdateFile, tmpdeleteFile, nodeList, nodeToTaskMapping, ring, allFilesUploaded, newOperationsSinceLastSpinDown, tmprequiredFile);
                } else if (data[0].equals("DELETE")) {
                    // delete everywhere
                    // if file in staging disk, then remove it and make note that it has been removed so that we can remove it from spun Down disk as well
                    // during the next staging disk free up
                    if (stagingDiskFileList.get(stagingDisk).containsKey(data[2])) {
                        Double memoryToBeFreed = stagingDiskFileList.get(stagingDisk).get(data[2]);
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
                    /*
                    for (int i = 0; i < noOfActiveAlwaysDisks; i++)
                        tmpdeleteFile.add(op);
                    */
                    for (MyNode n : ring.getPrimaryNodes(data[2])) {
                        if(n.isSpunDown() == false){
                            tmpdeleteFile.add(op);
                            nodeList.add(n);
                            if (nodeToTaskMapping.containsKey(n)) {
                                nodeToTaskMapping.get(n).addTask(op);
                            } else {
                                nodeToTaskMapping.put(n, new Tasks(n, op));
                            }
                        }
                    }

                    // how to take care of delete on spunDown disks coz file will be removed from stagingDisk and old version could continue existing on
                    // spunDown disk
                    tmpToBeDeletedList.put(data[2], Double.parseDouble(data[1]));

                }
            }
        }

        // finally now that we hav all tmp op files, we send them and get the op files which can be passed to MyRunner
        createOperationFileList(arrivalFile, dataFile, requiredFile, updateFile, deleteFile, tmpdataFile, tmprequiredFile, tmpupdateFile, tmpdeleteFile);
    }

    // this method takes care of all actions to be taken when there is a PUT operation required to be done when there is a staging disk

    public static Double stagingDiskPutOperation(String op, MyNode stagingDisk, int noOfActiveAlwaysDisks, String data[], Double stagingDiskMemoryUsed,
                                                 Double stagingDiskThresholdMemory, Map<String, Double> tmpToBeDeletedList,
                                              Map<String, Double> stagingDiskFileList, Double stagingDiskLowerThreshold,
                                              int noOfSpunDownDisks, ArrayList<String> tmpOpFile, ArrayList<String> tmpdeleteFile,
                                              ArrayList<MyNode> nodeList, HashMap<MyNode, Tasks> nodeToTaskMapping, MyRing ring, Map<String, Double>
                                                      allFilesUploaded, ArrayList<String> newOperationsSinceLastSpinDown, ArrayList<String> tmpRequiredFile) {
        Double stagingDiskMemoryToBeAdded = 0.0;
        // if staging disk occupied more the stagingDiskMemoryUsed upper threshold
        if (data[3].equals(null))
        {
            System.out.println(op);
            System.exit(1);
        }
        if (data[0].equals("PUT") && data[3]!=null && stagingDiskMemoryUsed + Double.parseDouble(data[3]) > stagingDiskThresholdMemory) {
            stagingDiskMemoryToBeAdded += freeUpStagingDiskMemory(data, stagingDiskMemoryUsed, stagingDisk, tmpToBeDeletedList,
                    stagingDiskFileList,
                    stagingDiskLowerThreshold, noOfSpunDownDisks, tmpOpFile, tmpdeleteFile, nodeList, nodeToTaskMapping, ring,
                    newOperationsSinceLastSpinDown, tmpRequiredFile);
        }
        // enough space in staging disk now
        tmpOpFile.add(op);
        nodeList.add(stagingDisk);

        stagingDiskMemoryToBeAdded += Double.parseDouble(data[3]);

        //stagingDiskFileList.put()
        // add to stagingDiskFileList
        stagingDiskFileList.put(data[2], Double.parseDouble(data[3])); // key : name of file, value : size  of file
        // add file to staging disk and activeAlways nodes
        /*
        for (int i = 0; i < noOfActiveAlwaysDisks; i++)
            tmpOpFile.add(op);
        */
        // need to do nodeTask mapping for these tasks
        if (nodeToTaskMapping.containsKey(stagingDisk)) {
            nodeToTaskMapping.get(stagingDisk).addTask(op);
        } else {
            nodeToTaskMapping.put(stagingDisk, new Tasks(stagingDisk, op));
        }

        for (MyNode n : ring.getPrimaryNodes(data[2])) {
            if(n.isSpunDown() == false){
                tmpOpFile.add(op);
                nodeList.add(n);
                if (nodeToTaskMapping.containsKey(n)) {
                    nodeToTaskMapping.get(n).addTask(op);
                } else {
                    nodeToTaskMapping.put(n, new Tasks(n, op));
                }
            }
        }

        // add file to allFilesUploaded file list
        allFilesUploaded.put(data[2], Double.parseDouble(data[3])); // this takes care of both operations(update and add)
        return stagingDiskMemoryToBeAdded;

    }

    public static Double freeUpStagingDiskMemory(String data[], Double stagingDiskMemoryUsed, MyNode stagingDisk,
                                              Map<String, Double> tmpToBeDeletedList, Map<String, Double> stagingDiskFileList,
                                              Double stagingDiskLowerThreshold, int noOfSpunDownDisks, ArrayList<String> tmpOpFile,
                                              ArrayList<String> tmpdeleteFile, ArrayList<MyNode> nodeList, HashMap<MyNode, Tasks> nodeToTaskMapping,
                                              MyRing ring, ArrayList<String> newOperationsSinceLastSpinDown, ArrayList<String> tmpRequiredFile) {
        System.out.println("Flushing staging disk contents at time = " + data[1]);
        WriteToLogFile.AddtoFile(String.format("%8sFlushing staging disk contents at time = %8s ", "", data[1]));
        // remove oldest unused files such after removing them only 60% of stagingDisk mem is occupied
        // before removing add them to respective spun down disks
        Map<String, Double> tmpToBeAddedToSpunDownFiles = new LinkedHashMap<String, Double>();
        Double memoryToBeFreed = 0.0;
        for (String file : stagingDiskFileList.keySet()) {
            tmpToBeAddedToSpunDownFiles.put(file, stagingDiskFileList.get(file));
            memoryToBeFreed += stagingDiskFileList.get(file);
            if (stagingDiskMemoryUsed - memoryToBeFreed <= stagingDiskLowerThreshold) {
                break;
            }
        }
        // READ from staging disk
        for (String file : tmpToBeAddedToSpunDownFiles.keySet()) {
            tmpRequiredFile.add("READ,"+data[1]+","+file);
            nodeToTaskMapping.get(stagingDisk).addTask("READ,"+data[1]+","+file);
            if (nodeToTaskMapping.containsKey(stagingDisk)) {
                nodeToTaskMapping.get(stagingDisk).addTask("READ,"+data[1]+","+file);
            } else {
                nodeToTaskMapping.put(stagingDisk, new Tasks(stagingDisk, "READ,"+data[1]+","+file));
            }
        }
        // now add those files to spun down disks
        for (String file : tmpToBeAddedToSpunDownFiles.keySet()) {
            for (int i = 0; i < noOfSpunDownDisks; i++)
                tmpOpFile.add("PUT," + data[1] + "," + file + "," + tmpToBeAddedToSpunDownFiles.get(file)); // tmpOpFile is tmpdataFile for PUT and
            // tmpupdateFile for Update

            for (MyNode n : ring.getPrimaryNodes(data[2])) {
                if(n.isSpunDown()){
                    nodeList.add(n);
                    if (nodeToTaskMapping.containsKey(n)) {
                        nodeToTaskMapping.get(n).addTask("PUT," + data[1] + "," + file + "," + tmpToBeAddedToSpunDownFiles.get(file));
                    } else {
                        nodeToTaskMapping.put(n, new Tasks(n, "PUT," + data[1] + "," + file + "," + tmpToBeAddedToSpunDownFiles.get(file)));
                    }
                }
            }
        }
        // DELETE from staging disk
        for (String file : tmpToBeAddedToSpunDownFiles.keySet()) {
            //tmpOpFile.add("DELETE,"+data[1]+","+file);
            tmpdeleteFile.add("DELETE,"+data[1]+","+file);
            nodeList.add(stagingDisk);
            if (nodeToTaskMapping.containsKey(stagingDisk)) {
                nodeToTaskMapping.get(stagingDisk).addTask("DELETE,"+data[1]+","+file);
            } else {
                nodeToTaskMapping.put(stagingDisk, new Tasks(stagingDisk, "DELETE,"+data[1]+","+file));
            }
        }
        // update stagingDiskMemoryUsed after removing old files
        Double stagingDiskMemoryToBeAdded = 0.0;
        stagingDiskMemoryToBeAdded -= memoryToBeFreed;


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
            for (MyNode n : ring.getPrimaryNodes(data[2])) {
                if(n.isSpunDown()){
                    nodeList.add(n);
                    if (nodeToTaskMapping.containsKey(n)) {
                        nodeToTaskMapping.get(n).addTask("DELETE," + data[1] + "," + file);
                    } else {
                        nodeToTaskMapping.put(n, new Tasks(n, "DELETE," + data[1] + "," + file));
                    }

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
    public static Map<Double, ArrayList<String>> getChronologicallyOrderedOperations(String inputLog, int numberOfOperations) throws Exception {
        Map<Double, ArrayList<String>> chronologicallyOrderedOperations = new LinkedHashMap<Double, ArrayList<String>>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputLog)))) {
            int count = 0;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String data[] = line.split(",");
                if (chronologicallyOrderedOperations.containsKey(Double.parseDouble(data[1]))) {
                    chronologicallyOrderedOperations.get(Double.parseDouble(data[1])).add(line);
                } else {
                    chronologicallyOrderedOperations.put(Double.parseDouble(data[1]), new ArrayList<String>(Arrays.asList(line)));
                }

                if(numberOfOperations > 0){
                    if(count == numberOfOperations){
                        break;
                    }
                    else {
                        count = count + 1;
                    }
                }

            }
        }
        return chronologicallyOrderedOperations;
    }

    // get op list when staging disk is not involved
    public static ArrayList<String> getOperationFileList(String inputLog, String operation, HashMap<MyNode, Tasks> nodeToTaskMapping, ArrayList<String>
            arrivalFile, MyRing ring, ArrayList<MyNode> nodeList, int noOfActiveAlwaysDisks, int numberOfOperations) throws Exception {
        ArrayList<String> operationFileList = new ArrayList<>();

        int count = 0;
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

                if(numberOfOperations > 0){
                    if(count == numberOfOperations){
                        break;
                    }
                    else {
                        count = count + 1;
                    }
                }
            }
        }
        return operationFileList;
    }


    // does the task of send the cloudlets and starting the simulation
    public static MyRunner performOperations(ArrayList<String> arrivalFile, ArrayList<String> dataFile,
                                           ArrayList<String> requiredFile, ArrayList<String> updateFile, ArrayList<String> deleteFile, ArrayList<MyNode>
                                                   nodeList, MyRing myRing, MyRing stagingDiskRing, String pathToStartingFileList, boolean addstagingDisk) throws
            Exception {
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

        // clearing contents of these files
        FileUtils.writeStringToFile(new File("files/" + arrival), "");
        FileUtils.writeStringToFile(new File("files/" + putData), "");
        FileUtils.writeStringToFile(new File("files/" + getData), "");
        FileUtils.writeStringToFile(new File("files/" + updateData), "");
        FileUtils.writeStringToFile(new File("files/" + deleteData), "");

        StringBuilder arrivalTimes = new StringBuilder();
        StringBuilder putOpData = new StringBuilder();
        StringBuilder getOpData = new StringBuilder();
        StringBuilder updateOpData = new StringBuilder();
        StringBuilder deleteOpData = new StringBuilder();

        for (int i = 0; i < arrivalFile.size(); i++) {
            if (i > 0)
                arrivalTimes.append("\n");
            arrivalTimes.append(arrivalFile.get(i));
            if(i%100000 == 0){
                FileUtils.writeStringToFile(new File("files/" + arrival), arrivalTimes.toString(),true);
                arrivalTimes = new StringBuilder();
            }
        }
        for (int i = 0; i < dataFile.size(); i++) {
            if (i > 0)
                putOpData.append("\n");
            putOpData.append(dataFile.get(i));

            if(i%100000 == 0){
                FileUtils.writeStringToFile(new File("files/" + putData), putOpData.toString(), true);
                putOpData = new StringBuilder();
            }
        }

        for (int i = 0; i < requiredFile.size(); i++) {
            if (i > 0)
                getOpData.append("\n");
            getOpData.append(requiredFile.get(i));

            if(i%100000 == 0){
                FileUtils.writeStringToFile(new File("files/" + getData), getOpData.toString(),true);
                getOpData = new StringBuilder();
            }
        }

        for (int i = 0; i < updateFile.size(); i++) {
            if (i > 0)
                updateOpData.append("\n");
            updateOpData.append(updateFile.get(i));

            if(i%100000 == 0){
                FileUtils.writeStringToFile(new File("files/" + updateData), updateOpData.toString(), true);
                updateOpData = new StringBuilder();
            }
        }

        for (int i = 0; i < deleteFile.size(); i++) {
            if (i > 0)
                deleteOpData.append("\n");
            deleteOpData.append(deleteFile.get(i));

            if(i%100000 == 0){
                FileUtils.writeStringToFile(new File("files/" + deleteData), deleteOpData.toString(), true);
                deleteOpData = new StringBuilder();
            }
        }

        FileUtils.writeStringToFile(new File("files/" + arrival), arrivalTimes.toString(),true);
        FileUtils.writeStringToFile(new File("files/" + putData), putOpData.toString(),true);
        FileUtils.writeStringToFile(new File("files/" + getData), getOpData.toString(),true);
        FileUtils.writeStringToFile(new File("files/" + updateData), updateOpData.toString(),true);
        FileUtils.writeStringToFile(new File("files/" + deleteData), deleteOpData.toString(),true);

        String startingFilelist ;
        if(debug == true)
            startingFilelist = "basic/operations/emptyFile.txt";
        else
            startingFilelist = pathToStartingFileList.split("files/")[1];

        ArrayList<MyNode> allNodes = new ArrayList<MyNode>(myRing.getAllNodes());
        if(addstagingDisk)
            allNodes.addAll(stagingDiskRing.getAllNodes());
        MyRunner runner = new MyRunner(arrival, putData, getData, updateData, deleteData, nodeList, startingFilelist, myRing, allNodes);

        return runner;
        //System.out.println("Energy Consumed : " + run.getTotalStorageEnergyConsumed() + " Joules()");
        //return run.getTotalStorageEnergyConsumed();
    }

    public static void main(String args[]) throws Exception{
        // node properties
        int totalNoOfNodes = 16;

        // staging disk properties
        boolean addStagingDisk = false;

        int numberOfOperations = -1;
        String distribution = "read intensive";

        // will have a set of predefined workloads , user selects one of them,
        // predefindedWorkloadNumber variable stores the workload id
        int predefindedWorkloadNumber = 1;

        int noOfReplicas = 3; //default 3
        String cachingMechanism = "LRU"; // FIFO also possible
        int HDDType = 0; // basicallly this number is the id for storage and power model, will assign ids to them
        //Scenarios : this part is to be done in front end
        int SSDType = 1;
        int percentageFlushAt = 90;
        int percentageFlushTill = 0;
        boolean realisticSSD = true; // if true the capacity split across reqd no of SSDs, if false single SSD with full capacity
        String pathToWorkload = "files/basic/operations/workload.txt";
        String pathToStartingFileList = "files/basic/operations/startingFileList.txt";
        String pathToInputLog = "files/basic/operations/idealInputLog.txt";
        boolean generateInputLog = false;




        startSimulation(totalNoOfNodes, addStagingDisk, numberOfOperations, predefindedWorkloadNumber, noOfReplicas, cachingMechanism, HDDType, SSDType,
                percentageFlushAt, percentageFlushTill, realisticSSD, pathToWorkload, pathToStartingFileList, pathToInputLog, generateInputLog);

    }
}
