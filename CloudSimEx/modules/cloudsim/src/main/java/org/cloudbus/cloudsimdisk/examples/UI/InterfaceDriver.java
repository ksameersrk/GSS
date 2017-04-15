package org.cloudbus.cloudsimdisk.examples.UI;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.cloudbus.cloudsimdisk.examples.MyRunner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

        int scenario = 3;
        if(scenario == 1){
            addStagingDisk = false;
            MyRunner runner = startSimulation(totalNoOfNodes, addStagingDisk, numberOfOperations, predefindedWorkloadNumber, noOfReplicas, cachingMechanism,
                    HDDType,
                    SSDType,
                    percentageFlushAt, percentageFlushTill, realisticSSD, pathToWorkload, pathToStartingFileList, pathToInputLog, generateInputLog);

            List<Map<String,Object>> diskStats = runner.getDiskStats();
            //System.out.println(diskStats.toString());

            getSortedAndDiskNameChangedDiskStats(diskStats);

            drawLineGraph(diskStats, "without staging disk");
        }
        else if(scenario == 2) {
            addStagingDisk = true;
            MyRunner runner = startSimulation(totalNoOfNodes, addStagingDisk, numberOfOperations, predefindedWorkloadNumber, noOfReplicas, cachingMechanism,
                    HDDType, SSDType,
                    percentageFlushAt, percentageFlushTill, realisticSSD, pathToWorkload, pathToStartingFileList, pathToInputLog, generateInputLog);

            List<Map<String,Object>> diskStats = runner.getDiskStats();
            //System.out.println(diskStats.toString());
            drawLineGraph(diskStats, "with staging disk");

        }
        else if(scenario == 3){

            addStagingDisk = false;
            MyRunner runner = startSimulation(totalNoOfNodes, addStagingDisk, numberOfOperations, predefindedWorkloadNumber, noOfReplicas, cachingMechanism,
                    HDDType,
                    SSDType,
                    percentageFlushAt, percentageFlushTill, realisticSSD, pathToWorkload, pathToStartingFileList, pathToInputLog, generateInputLog);

            List<Map<String,Object>> diskStats = runner.getDiskStats();
            //System.out.println(diskStats.toString());
            getSortedAndDiskNameChangedDiskStats(diskStats);

            addStagingDisk = true;
            MyRunner runnerSSD = startSimulation(totalNoOfNodes, addStagingDisk, numberOfOperations, predefindedWorkloadNumber, noOfReplicas, cachingMechanism,
                    HDDType, SSDType,
                    percentageFlushAt, percentageFlushTill, realisticSSD, pathToWorkload, pathToStartingFileList, pathToInputLog, generateInputLog);

            List<Map<String,Object>> diskStatsSSD = runnerSSD.getDiskStats();
            //System.out.println(diskStats.toString());

            getSortedAndDiskNameChangedDiskStats(diskStatsSSD);

            //List<String> xAxisLabels = getXaxisLabels(diskStats, "disk name");
            List<Double> yAxisLabelTotalPowerSSD = getYaxisLabels(diskStats, "total energy");
            List<Double> yAxisLabelIdleTimeSSD = getYaxisLabels(diskStats, "idle time");
            List<Double> yAxisLabelActiveTimeSSD = getYaxisLabels(diskStats,"active time" );
            List<Double> yAxisLabelIdleEnergySSD = getYaxisLabels(diskStats, "idle energy");
            List<Double> yAxisLabelActiveEnergySSD = getYaxisLabels(diskStats, "active energy");

            List<String> xAxisLabels = getXaxisLabels(diskStats, "disk name");
            List<Double> yAxisLabelTotalPower = getYaxisLabels(diskStats, "total energy");
            List<Double> yAxisLabelIdleTime = getYaxisLabels(diskStats, "idle time");
            List<Double> yAxisLabelActiveTime = getYaxisLabels(diskStats,"active time" );
            List<Double> yAxisLabelIdleEnergy = getYaxisLabels(diskStats, "idle energy");
            List<Double> yAxisLabelActiveEnergy = getYaxisLabels(diskStats, "active energy");

            Map<String, Object> graphJsonTotalPower = getLineGraphJSON(xAxisLabels,yAxisLabelTotalPower,yAxisLabelTotalPowerSSD,"without staging disk",
                    "with staging disk");
            Map<String, Object> graphJsonIdleTime = getLineGraphJSON(xAxisLabels,yAxisLabelIdleTime,yAxisLabelIdleTimeSSD,"without staging disk","with staging disk");
            Map<String, Object> graphJsonActiveTime = getLineGraphJSON(xAxisLabels,yAxisLabelActiveTime,yAxisLabelActiveTimeSSD,"without staging disk", "with staging disk");
            Map<String, Object> graphJsonIdleEnergy = getLineGraphJSON(xAxisLabels,yAxisLabelIdleEnergy,yAxisLabelIdleEnergySSD,"without staging disk", "with staging disk");
            Map<String, Object> graphJsonActiveEnergy = getLineGraphJSON(xAxisLabels,yAxisLabelActiveEnergy,yAxisLabelActiveEnergySSD,"without staging disk", "with staging disk");

            dumpToFile(graphJsonTotalPower, "line_chart_total_power");
            dumpToFile(graphJsonIdleTime,"line_chart_idle_time");
            dumpToFile(graphJsonActiveTime,"line_chart_active_time");
            dumpToFile(graphJsonIdleEnergy,"line_chart_idle_energy");
            dumpToFile(graphJsonActiveEnergy,"line_chart_active_energy");



        }




    }

    public static void dumpToFile(Map<String, Object> graphJson, String filename) throws IOException{
        Gson gson = new Gson();
        String jsonInString = gson.toJson(graphJson);
        String path = "/Users/spadigi/Desktop/greenSwiftSimulation/GSS/server/data/" + filename + ".json";
        FileUtils.writeStringToFile(new File(path), jsonInString);
    }

    public static void drawLineGraph(List<Map<String,Object>> diskStats, String seriesName) throws IOException{

        List<String> xAxisLabels = getXaxisLabels(diskStats, "disk name");
        List<Double> yAxisLabelTotalPower = getYaxisLabels(diskStats, "total energy");
        List<Double> yAxisLabelIdleTime = getYaxisLabels(diskStats, "idle time");
        List<Double> yAxisLabelActiveTime = getYaxisLabels(diskStats,"active time" );
        List<Double> yAxisLabelIdleEnergy = getYaxisLabels(diskStats, "idle energy");
        List<Double> yAxisLabelActiveEnergy = getYaxisLabels(diskStats, "active energy");

        Map<String, Object> graphJsonTotalPower = getLineGraphJSON(xAxisLabels,yAxisLabelTotalPower,seriesName);
        Map<String, Object> graphJsonIdleTime = getLineGraphJSON(xAxisLabels,yAxisLabelIdleTime,seriesName);
        Map<String, Object> graphJsonActiveTime = getLineGraphJSON(xAxisLabels,yAxisLabelActiveTime,seriesName);
        Map<String, Object> graphJsonIdleEnergy = getLineGraphJSON(xAxisLabels,yAxisLabelIdleEnergy,seriesName);
        Map<String, Object> graphJsonActiveEnergy = getLineGraphJSON(xAxisLabels,yAxisLabelActiveEnergy,seriesName);

        dumpToFile(graphJsonTotalPower, "line_chart_total_power");
        dumpToFile(graphJsonIdleTime,"line_chart_idle_time");
        dumpToFile(graphJsonActiveTime,"line_chart_active_time");
        dumpToFile(graphJsonIdleEnergy,"line_chart_idle_energy");
        dumpToFile(graphJsonActiveEnergy,"line_chart_active_energy");
    }

    public static Map<String, Object> getLineGraphJSON(List<String> xAxisLabels, List<Double> yAxisLabels, String series_name){
        Map<String, Object> graphJson = new HashMap<>();
        List<List<Double>> data = new ArrayList<>();
        List<String> series = new ArrayList<>();
        series.add(series_name);
        data.add(yAxisLabels);
        graphJson.put("label", xAxisLabels);
        graphJson.put("data", data);
        graphJson.put("series", series);

        return graphJson;
    }

    public static Map<String, Object> getLineGraphJSON(List<String> xAxisLabels, List<Double> yAxisLabels0, List<Double> yAxisLabels1, String series_name0,
                                                       String series_name1){
        Map<String, Object> graphJson = new HashMap<>();
        List<List<Double>> data = new ArrayList<>();
        List<String> series = new ArrayList<>();
        series.add(series_name0);
        series.add(series_name1);
        data.add(yAxisLabels0);
        data.add(yAxisLabels1);
        graphJson.put("label", xAxisLabels);
        graphJson.put("data", data);
        graphJson.put("series", series);

        return graphJson;
    }


    public static void getSortedAndDiskNameChangedDiskStats(List<Map<String,Object>> diskStats){
        Collections.sort(diskStats, new Comparator<Map<String, Object>>() {
            public int compare(final Map<String, Object> o1, final Map<String, Object> o2) {
                int s1 = Integer.parseInt(o1.get("disk name").toString());
                int s2 = Integer.parseInt(o2.get("disk name").toString());
                return s1 - s2;
            }
        });

        for(Map<String,Object> map : diskStats){

            if(Integer.parseInt(map.get("disk name").toString()) >= 1000){
                map.put("disk name", "SSD" +map.get("disk name").toString());
            } else {
                map.put("disk name", "HDD" +map.get("disk name").toString());
            }

        }
    }

    public static List<Double> getYaxisLabels(List<Map<String,Object>> diskStats, String metric) {
        List<Double> yAxisLabel = new ArrayList<>();
        for(Map<String,Object> map : diskStats){
            yAxisLabel.add(Double.parseDouble(map.get(metric).toString()));
        }
        return yAxisLabel;
    }

    public static List<String> getXaxisLabels(List<Map<String,Object>> diskStats, String metric) {
        List<String> xAxisLabel = new ArrayList<>();
        for(Map<String,Object> map : diskStats){
            xAxisLabel.add(map.get(metric).toString());
        }
        return xAxisLabel;
    }
}
