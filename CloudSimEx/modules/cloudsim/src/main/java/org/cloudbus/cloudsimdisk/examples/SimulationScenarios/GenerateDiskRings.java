package org.cloudbus.cloudsimdisk.examples.SimulationScenarios;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.cloudbus.cloudsimdisk.examples.MyRing.MyNode;
import org.cloudbus.cloudsimdisk.examples.MyRing.MyRing;
import org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms.MyNewRandomAlgorithm;

import org.cloudbus.cloudsimdisk.examples.UI.InputJSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GenerateDiskRings {
    public static  MyRing createRing(int noOfDisks, int diskType ){
        int partitionPower = 5;
        int replicas = 3;
        double overloadPercent = 10.0;

        String ringInputPath = "modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/MyRing/rings.txt";
        MyRing myRing = MyRing.buildRing(ringInputPath, noOfDisks, partitionPower, replicas, overloadPercent, false, diskType);

        return myRing;
    }

    public static List<MyNode> getSpunDownNodesList(MyRing myRing)
    {
        List<MyNode> spunDownNodes = new ArrayList<>();
        MyNewRandomAlgorithm spinDownRandomAlgorithm = new MyNewRandomAlgorithm();
        spunDownNodes = spinDownRandomAlgorithm.getSpunDownNodesWithPercent(myRing, 27, 10000);
        System.out.println("Out of "+ myRing.getAllNodes().size() + " disks, No. of disks spun down = " + spunDownNodes.size());

        return  spunDownNodes;
    }

    public static void serializeObjects(Object obj, String filePath)
    {
        try {
            byte[] data = SerializationUtils.serialize((Serializable) obj);
            FileUtils.writeByteArrayToFile(new File(filePath), data);
            //FileUtils.writeStringToFile(new File(filePath), jsonStringObject);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void dumpObjects(MyRing myRing, List<MyNode> spunDownNodes)
    {
        serializeObjects(myRing, "files/basic/operations/myRing.json");
        serializeObjects(spunDownNodes, "files/basic/operations/spunDownNodesList.json");
    }

    public static void main(String args[]) throws Exception{

        String base_directory = "/Users/spadigi/Desktop/greenSwiftSimulation/GSS/";
        //String base_directory = "/media/sai/New Volume/greenSwiftSimulation/GSS/";
        // String base_directory = "/home/ksameersrk/Desktop/GSS/";
        Gson jsonParser = new Gson();

        String filePathToJson = base_directory + "server/data/input_data.json";
        //String filePathToJson = "/Users/spadigi/Desktop/greenSwiftSimulation/GSS/server/data/input_data.json";
        String jsonData = FileUtils.readFileToString(new File(filePathToJson));
        InputJSONObject inputObject = jsonParser.fromJson(jsonData, InputJSONObject.class);
        // node properties
        int totalNoOfNodes = inputObject.getTotalNoOfNodes();
        // we deal with only HDDs here as no. of SSDs is determined by that percentage
        int HDDType = inputObject.getHddDiskType() - 1; // basically this number is the id for storage and power model, will assign ids to them

        MyRing myRing = createRing(totalNoOfNodes, HDDType);
        List<MyNode> spunDownNodes = getSpunDownNodesList(myRing);

        dumpObjects(myRing, spunDownNodes);

    }

}
