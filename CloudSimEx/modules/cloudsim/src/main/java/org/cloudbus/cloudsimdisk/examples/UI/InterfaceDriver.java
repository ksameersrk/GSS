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

    //public static String base_directory = "/Users/skulkarni9/Desktop/8thSem/GSS/";
    public static String base_directory = "/Users/spadigi/Desktop/greenSwiftSimulation/GSS/";

    public static void main(String args[]) throws Exception
    {
        Gson jsonParser = new Gson();

                String filePathToJson = base_directory + "server/data/input_data.json";
        //String filePathToJson = "/Users/spadigi/Desktop/greenSwiftSimulation/GSS/server/data/input_data.json";
        String jsonData = FileUtils.readFileToString(new File(filePathToJson));
        InputJSONObject inputObject = jsonParser.fromJson(jsonData, InputJSONObject.class);

        /*
        // node properties
        int totalNoOfNodes = 16;

        // staging disk properties
        boolean addStagingDisk;

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
        int percentageFlushTill = 70;
        boolean realisticSSD = true; // if true the capacity split across reqd no of SSDs, if false single SSD with full capacity

        //String pathToWorkload = "files/basic/operations/workload.txt";
        String pathToWorkload = "/Users/spadigi/Desktop/fyp_5k_workload.txt";
        String pathToStartingFileList = "files/basic/operations/startingFileList.txt";
        String pathToInputLog = "files/basic/operations/idealInputLog.txt";
        boolean generateInputLog = false;

        int scenario = 2;
        */

        // My thing starting here
        System.out.println(inputObject.getCachingMechanism());
        System.out.println(inputObject.getHddDiskType());
        System.out.println(inputObject.getManualTextarea());
        System.out.println(inputObject.getNoOfReplicas());
        System.out.println(inputObject.getNumberOfOperations());
        System.out.println(inputObject.getPredefindedWorkloadNumber());
        System.out.println(inputObject.getScenario());
        System.out.println(inputObject.getSsdDiskType());
        System.out.println(inputObject.getTotalNoOfNodes());
        System.out.println(inputObject.getWorkloadType());

        // node properties
        int totalNoOfNodes = inputObject.getTotalNoOfNodes();

        // staging disk properties
        boolean addStagingDisk = true;

        int numberOfOperations = inputObject.getNumberOfOperations();
        String distribution = "read intensive";

        // will have a set of predefined workloads , user selects one of them,
        // predefindedWorkloadNumber variable stores the workload id
        int predefindedWorkloadNumber = inputObject.getPredefindedWorkloadNumber();

        int noOfReplicas = 3; //default 3
        String cachingMechanism = "FIFO"; // FIFO also possible #TODO LRU not working
        int HDDType = inputObject.getHddDiskType(); // basicallly this number is the id for storage and power model, will assign ids to them
        //Scenarios : this part is to be done in front end
        int SSDType = inputObject.getSsdDiskType();
        int percentageFlushAt = 90;
        int percentageFlushTill = 70;
        boolean realisticSSD = true; // if true the capacity split across reqd no of SSDs, if false single SSD with full capacity

        String pathToWorkload = "files/basic/operations/workload.txt";
        String pathToStartingFileList = "files/basic/operations/startingFileList.txt";
        String pathToInputLog = "files/basic/operations/idealInputLog.txt";
        boolean generateInputLog = false;

        int scenario = inputObject.getScenario();
        // My thing ending here

        if(scenario == 1){
            addStagingDisk = false;
            MyRunner runner = startSimulation(totalNoOfNodes, addStagingDisk, numberOfOperations, predefindedWorkloadNumber, noOfReplicas, cachingMechanism,
                    HDDType,
                    SSDType,
                    percentageFlushAt, percentageFlushTill, realisticSSD, pathToWorkload, pathToStartingFileList, pathToInputLog, generateInputLog);

            List<Map<String,Object>> diskStats = runner.getDiskStats();
            //System.out.println(diskStats.toString());

            getSortedAndDiskNameChangedDiskStats(diskStats);

            drawLineGraph(diskStats, "without staging disk", scenario);

            Map<String, Double> scenarioStat = runner.getScenarioStats();
            Map<String, Object> pieChartJSON = new HashMap<>();
            List<String> label = new ArrayList<>();
            List<Double> diskPower = new ArrayList<>();
            label.add("Always Active Disks");
            label.add("Spun Down Disks");
            diskPower.add(scenarioStat.get("always active disk power consumption"));
            diskPower.add(scenarioStat.get("spun down disk power consumption"));
            pieChartJSON.put("label", label);
            pieChartJSON.put("data", diskPower);
            dumpToFile(pieChartJSON, "pieChartActiveVsSpundown", scenario);
        }
        else if(scenario == 2) {
            addStagingDisk = true;
            MyRunner runner = startSimulation(totalNoOfNodes, addStagingDisk, numberOfOperations, predefindedWorkloadNumber, noOfReplicas, cachingMechanism,
                    HDDType, SSDType,
                    percentageFlushAt, percentageFlushTill, realisticSSD, pathToWorkload, pathToStartingFileList, pathToInputLog, generateInputLog);

            List<Map<String,Object>> diskStats = runner.getDiskStats();
            getSortedAndDiskNameChangedDiskStats(diskStats);
            //System.out.println(diskStats.toString());
            drawLineGraph(diskStats, "with staging disk", scenario);

            Map<String, Double> scenarioStat = runner.getScenarioStats();
            Map<String, Object> pieChartJSON = new HashMap<>();
            List<String> label = new ArrayList<>();
            List<Double> diskPower = new ArrayList<>();
            label.add("Always Active Disks");
            label.add("Spun Down Disks");
            diskPower.add(scenarioStat.get("always active disk power consumption"));
            diskPower.add(scenarioStat.get("spun down disk power consumption"));
            pieChartJSON.put("label", label);
            pieChartJSON.put("data", diskPower);
            dumpToFile(pieChartJSON, "pieChartActiveVsSpundown", scenario);

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

            List<String> xAxisLabelsSSD = getXaxisLabels(diskStats, "disk name");
            List<Double> yAxisLabelTotalPowerSSD = getYaxisLabels(diskStatsSSD, "total energy");
            List<Double> yAxisLabelIdleTimeSSD = getYaxisLabels(diskStatsSSD, "idle time");
            List<Double> yAxisLabelActiveTimeSSD = getYaxisLabels(diskStatsSSD,"active time" );
            List<Double> yAxisLabelIdleEnergySSD = getYaxisLabels(diskStatsSSD, "idle energy");
            List<Double> yAxisLabelActiveEnergySSD = getYaxisLabels(diskStatsSSD, "active energy");

            List<String> xAxisLabels = getXaxisLabels(diskStats, "disk name");
            List<Double> yAxisLabelTotalPower = getYaxisLabels(diskStats, "total energy");
            List<Double> yAxisLabelIdleTime = getYaxisLabels(diskStats, "idle time");
            List<Double> yAxisLabelActiveTime = getYaxisLabels(diskStats,"active time" );
            List<Double> yAxisLabelIdleEnergy = getYaxisLabels(diskStats, "idle energy");
            List<Double> yAxisLabelActiveEnergy = getYaxisLabels(diskStats, "active energy");

            System.out.println(xAxisLabelsSSD.toString());
            System.out.println(yAxisLabelTotalPowerSSD.toString());
            System.out.println(xAxisLabels.toString());
            System.out.println(yAxisLabelTotalPower.toString());
            Map<String, Object> graphJsonTotalPower = getLineGraphJSON(xAxisLabelsSSD,yAxisLabelTotalPower,yAxisLabelTotalPowerSSD,"without staging disk",
                    "with staging disk");
            Map<String, Object> graphJsonIdleTime = getLineGraphJSON(xAxisLabelsSSD,yAxisLabelIdleTime,yAxisLabelIdleTimeSSD,"without staging disk","with " +
                    "staging disk");
            Map<String, Object> graphJsonActiveTime = getLineGraphJSON(xAxisLabelsSSD,yAxisLabelActiveTime,yAxisLabelActiveTimeSSD,"without staging disk",
                    "with staging disk");
            Map<String, Object> graphJsonIdleEnergy = getLineGraphJSON(xAxisLabelsSSD,yAxisLabelIdleEnergy,yAxisLabelIdleEnergySSD,"without staging disk",
                    "with staging disk");
            Map<String, Object> graphJsonActiveEnergy = getLineGraphJSON(xAxisLabelsSSD,yAxisLabelActiveEnergy,yAxisLabelActiveEnergySSD,"without staging " +
                    "disk", "with staging disk");

            dumpToFile(graphJsonTotalPower, "line_chart_total_power", scenario);
            dumpToFile(graphJsonIdleTime,"line_chart_idle_time", scenario);
            dumpToFile(graphJsonActiveTime,"line_chart_active_time", scenario);
            dumpToFile(graphJsonIdleEnergy,"line_chart_idle_energy", scenario);
            dumpToFile(graphJsonActiveEnergy,"line_chart_active_energy", scenario);

            Map<String, Double> scenarioStat = runner.getScenarioStats();
            Map<String, Object> pieChartJSON = new HashMap<>();
            List<String> label = new ArrayList<>();
            List<Double> diskPower = new ArrayList<>();
            label.add("Always Active Disks");
            label.add("Spun Down Disks");
            diskPower.add(scenarioStat.get("always active disk power consumption"));
            diskPower.add(scenarioStat.get("spun down disk power consumption"));
            pieChartJSON.put("label", label);
            pieChartJSON.put("data", diskPower);
            dumpToFile(pieChartJSON, "pieChartActiveVsSpundownWithoutStagingDisk", scenario);



            Map<String, Double> scenarioStatSSD = runnerSSD.getScenarioStats();
            Map<String, Object> pieChartJSONswiftVsGSS = new HashMap<>();

            List<Double> diskPowerWithStagingDisk = new ArrayList<>();
            diskPowerWithStagingDisk.add(scenarioStatSSD.get("always active disk power consumption"));
            diskPowerWithStagingDisk.add(scenarioStatSSD.get("spun down disk power consumption"));
            pieChartJSON.put("label", label);
            pieChartJSON.put("data", diskPower);
            dumpToFile(pieChartJSON, "pieChartActiveVsSpundownWithStagingDisk", scenario);

            List<String> labelSwiftVsGSS = new ArrayList<>();
            List<Double> diskPowerSwiftVsGSS = new ArrayList<>();
            labelSwiftVsGSS.add("Without Staging Disk");
            labelSwiftVsGSS.add("With Staging Disk");
            diskPowerSwiftVsGSS.add(scenarioStat.get("all disk power consumption"));
            diskPowerSwiftVsGSS.add(scenarioStatSSD.get("all disk power consumption"));
            pieChartJSONswiftVsGSS.put("label", labelSwiftVsGSS);
            pieChartJSONswiftVsGSS.put("data", diskPowerSwiftVsGSS);
            dumpToFile(pieChartJSONswiftVsGSS, "pieChartWithVsWithoutSSD", scenario);

        }




    }

    public static void dumpToFile(Map<String, Object> graphJson, String filename, int scenario) throws IOException{
        Gson gson = new Gson();
        String jsonInString = gson.toJson(graphJson);
        //String path = "/Users/skulkarni9/Desktop/8thSem/GSS/server/data/scenario2/" + filename + ".json";
        //String path = "/Users/spadigi/Desktop/greenSwiftSimulation/GSS/server/data/scenario2/" + filename + ".json";
        String path = base_directory + "server/data/scenario" + Integer.toString(scenario) + "/" + filename + ".json";
        FileUtils.writeStringToFile(new File(path), jsonInString);
    }

    public static void drawLineGraph(List<Map<String,Object>> diskStats, String seriesName, int scenario) throws IOException{

        List<String> xAxisLabels = getXaxisLabels(diskStats, "disk name");
        List<Double> yAxisLabelTotalPower = getYaxisLabels(diskStats, "total energy");
        List<Double> yAxisLabelIdleTime = getYaxisLabels(diskStats, "idle time");
        List<Double> yAxisLabelActiveTime = getYaxisLabels(diskStats,"active time" );
        List<Double> yAxisLabelIdleEnergy = getYaxisLabels(diskStats, "idle energy");
        List<Double> yAxisLabelActiveEnergy = getYaxisLabels(diskStats, "active energy");

        System.out.println(xAxisLabels.toString());
        System.out.println(yAxisLabelTotalPower.toString());
        Map<String, Object> graphJsonTotalPower = getLineGraphJSON(xAxisLabels,yAxisLabelTotalPower,seriesName);
        Map<String, Object> graphJsonIdleTime = getLineGraphJSON(xAxisLabels,yAxisLabelIdleTime,seriesName);
        Map<String, Object> graphJsonActiveTime = getLineGraphJSON(xAxisLabels,yAxisLabelActiveTime,seriesName);
        Map<String, Object> graphJsonIdleEnergy = getLineGraphJSON(xAxisLabels,yAxisLabelIdleEnergy,seriesName);
        Map<String, Object> graphJsonActiveEnergy = getLineGraphJSON(xAxisLabels,yAxisLabelActiveEnergy,seriesName);

        dumpToFile(graphJsonTotalPower, "line_chart_total_power", scenario);
        dumpToFile(graphJsonIdleTime,"line_chart_idle_time", scenario);
        dumpToFile(graphJsonActiveTime,"line_chart_active_time", scenario);
        dumpToFile(graphJsonIdleEnergy,"line_chart_idle_energy", scenario);
        dumpToFile(graphJsonActiveEnergy,"line_chart_active_energy", scenario);
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
