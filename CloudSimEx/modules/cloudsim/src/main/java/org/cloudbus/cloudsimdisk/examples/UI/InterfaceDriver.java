package org.cloudbus.cloudsimdisk.examples.UI;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.cloudbus.cloudsimdisk.examples.MyRunner;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

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

        int scenario = 1;
        if(scenario == 1){
            addStagingDisk = false;
            MyRunner runner = startSimulation(totalNoOfNodes, addStagingDisk, numberOfOperations, predefindedWorkloadNumber, noOfReplicas, cachingMechanism,
                    HDDType,
                    SSDType,
                    percentageFlushAt, percentageFlushTill, realisticSSD, pathToWorkload, pathToStartingFileList, pathToInputLog, generateInputLog);

            List<Map<String,Object>> diskStats = runner.getDiskStats();
            //System.out.println(diskStats.toString());

            Collections.sort(diskStats, new Comparator<Map<String, Object>>() {
                public int compare(final Map<String, Object> o1, final Map<String, Object> o2) {
                    int s1 = Integer.parseInt(o1.get("disk name").toString());
                    int s2 = Integer.parseInt(o2.get("disk name").toString());
                    return s1 - s2;
                }
            });

            Map<String, Object> graphJson = new HashMap<>();
            List<String> xAxisLabels = new ArrayList<>();
            List<Double> yAxisLabel = new ArrayList<>();
            for(Map<String,Object> map : diskStats){

                if(Integer.parseInt(map.get("disk name").toString()) >= 1000){
                    map.put("disk name", "SSD" +map.get("disk name").toString());
                } else {
                    map.put("disk name", "HDD" +map.get("disk name").toString());
                }
                xAxisLabels.add(map.get("disk name").toString());
                yAxisLabel.add(Double.parseDouble(map.get("total energy").toString()));



            }

            List<List<Double>> data = new ArrayList<>();
            List<String> series = new ArrayList<>();
            series.add("with staging disk");
            data.add(yAxisLabel);
            graphJson.put("label", xAxisLabels);
            graphJson.put("data", yAxisLabel);
            graphJson.put("series", series);


            Gson gson = new Gson();
            //gson.toJson(diskStats, new FileWriter("/Users/spadigi/Desktop/greenSwiftSimulation/GSS/server/data/dataStat.json"));
            String jsonInString = gson.toJson(graphJson);
            //System.out.println(jsonInString);
            FileUtils.writeStringToFile(new File("/Users/spadigi/Desktop/greenSwiftSimulation/GSS/server/data/line_chart.json"), jsonInString);
        }
        else if(scenario == 2) {
            addStagingDisk = true;
            MyRunner runner = startSimulation(totalNoOfNodes, addStagingDisk, numberOfOperations, predefindedWorkloadNumber, noOfReplicas, cachingMechanism,
                    HDDType, SSDType,
                    percentageFlushAt, percentageFlushTill, realisticSSD, pathToWorkload, pathToStartingFileList, pathToInputLog, generateInputLog);
        }
        else if(scenario == 3){

        }




    }
}
