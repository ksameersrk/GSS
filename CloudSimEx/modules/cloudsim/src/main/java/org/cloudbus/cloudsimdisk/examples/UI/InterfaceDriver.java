package org.cloudbus.cloudsimdisk.examples.UI;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;

import static org.cloudbus.cloudsimdisk.examples.SimulationScenarios.FlushEntireStagingDiskContents.startSimulation;

/**
 * Created by spadigi on 4/4/17.
 */
public class InterfaceDriver {
    public static void main(String args[]) throws Exception
    {
        Gson jsonParser = new Gson();
        String filePathToJson = "/Users/spadigi/Desktop/greenSwiftSimulation/GSS/server/data/input_data.json";
        String jsonData = FileUtils.readFileToString(new File(filePathToJson));
        InputJSONObject inputObject = jsonParser.fromJson(jsonData, InputJSONObject.class);


        /*
        int numberOfOperations = 10;
        String distribution = "read intensive";

        // will have a set of predefined workloads , user selects one of them,
        // predefindedWorkloadNumber variable stores the workload id
        int predefindedWorkloadNumber = 1;

        int noOfReplicas = 3; //default 3
        String cachingMechanism = "LRU"; // FIFO also possible
        int diskType = 1; // basicallly this number is the id for storage and power model, will assign ids to them
        //Scenarios : this part is to be done in front end
        */
        /*
        int numberOfOperations = inputObject.getNumberOfOperations();
        String distribution = inputObject.getDistribution();

        // will have a set of predefined workloads , user selects one of them,
        // predefindedWorkloadNumber variable stores the workload id
        int predefindedWorkloadNumber = inputObject.getPredefindedWorkloadNumber();

        int noOfReplicas = inputObject.getNoOfReplicas(); //default 3
        String cachingMechanism = inputObject.getCachingMechanism(); // FIFO also possible
        int diskType = inputObject.getDiskType(); // basicallly this number is the id for storage and power model, will assign ids to them
        */
        // node properties
        int totalNoOfNodes = 16;

        // staging disk properties
        boolean addStagingDisk = true;

        int numberOfOperations = 10;
        String distribution = "read intensive";

        // will have a set of predefined workloads , user selects one of them,
        // predefindedWorkloadNumber variable stores the workload id
        int predefindedWorkloadNumber = 1;

        int noOfReplicas = 3; //default 3
        String cachingMechanism = "FIFO"; // FIFO also possible
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
