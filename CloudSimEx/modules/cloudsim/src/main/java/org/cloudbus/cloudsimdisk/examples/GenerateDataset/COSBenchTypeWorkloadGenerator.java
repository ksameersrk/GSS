package org.cloudbus.cloudsimdisk.examples.GenerateDataset;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by spadigi on 2/28/17.
 */

/*
* The COSBench tool allows users to define a Swift workload based on the following two aspects:
*       (1) range of the object sizes in the workload (e.g. from 1MB to 10MB).
*       (2) the ratio of PUT, GET and DELETE operations (e.g. 1:8:1), i.e we can choose whether the workload has to be upload or download intensive
*/
public class COSBenchTypeWorkloadGenerator {

    public static  void generateWorkload(String typeOfWorkload, String objectSize, String outputFilePath, int stagingDiskCapacity, int minNoOfFlushes) throws Exception
    {

        /*
        * String outputFilePath -> the file to which generated workload has to be written to,
        * int stagingDiskCapacity -> using this characteristic we try ensure that a flush down happens in staging disk for a minimum no. of times,
        * String typeOfWorkload -> download intensive[GET: 90%, PUT: 5%, DELETE:5%], upload intensive [GET: 5%, PUT: 90%, DELETE:5%] and
        *                           balanced[GET: 45%, PUT: 45%, DELETE:10%]
        * String objectSize -> small(size range:1KB-100KB), large(size range:1MB – 10MB)
        */

        int totalUploadCapacity = stagingDiskCapacity * minNoOfFlushes;

        Map<String, Double> uploadOps = getUploadOps(totalUploadCapacity, objectSize);

        ArrayList<String> downloadOps = getDownloadOps(typeOfWorkload, uploadOps.size());

        ArrayList<String> deleteOps = getDeleteOps(typeOfWorkload, uploadOps.size());

        writeOpsToFile(outputFilePath, uploadOps, downloadOps, deleteOps);

    }

    public static Map<String, Double> getUploadOps(int totalUploadCapacity, String objectSize){
        double lowerSizeLimit = 0.0;
        double upperSizeLimit = 0.0;
        if(objectSize.equals("small")){
            lowerSizeLimit = 0.001;
            upperSizeLimit = 0.1;
        }
        else if(objectSize.equals("large")){
            lowerSizeLimit = 1.0;
            upperSizeLimit = 10.0;
        }

        Map<String, Double> uploadOps = new LinkedHashMap<>();

        double totalUploadedSize = 0.0;
        int i = 0;
        do{
            double random = ThreadLocalRandom.current().nextDouble(lowerSizeLimit, upperSizeLimit);
            // round off to 3 decimal places
            random = Math.round(random*1000)/1000.0;

            uploadOps.put("File"+i, random);

            i++;
            totalUploadedSize += random;
        } while (totalUploadedSize < totalUploadCapacity);
        return uploadOps;
    }

    public static ArrayList<String> getDownloadOps(String typeOfWorkload, int noOfUploadOps){

        int uploadPercentage = 0;
        int downloadPercentage = 0;

        if(typeOfWorkload.equals("upload intensive")) {
            uploadPercentage = 90;
            downloadPercentage = 5;
        }
        else if(typeOfWorkload.equals("download intensive")){
            uploadPercentage = 5;
            downloadPercentage = 90;
        }
        else if(typeOfWorkload.equals("balanced")) {
            uploadPercentage = 45;
            downloadPercentage = 45;
        }


        ArrayList<String> downloadOps = new ArrayList<>();

        int noOfDownloadOpsReqd = Math.round(downloadPercentage*noOfUploadOps/uploadPercentage);

        for(int i = 0; i<noOfDownloadOpsReqd; ++i){
            int random = ThreadLocalRandom.current().nextInt(0, noOfUploadOps);
            downloadOps.add("File"+random);
        }

        return downloadOps;
    }

    public static ArrayList<String> getDeleteOps(String typeOfWorkload, int noOfUploadOps){

        int uploadPercentage = 0;
        int deletePercentage = 0;

        if(typeOfWorkload.equals("upload intensive")) {
            uploadPercentage = 90;
            deletePercentage = 5;
        }
        else if(typeOfWorkload.equals("download intensive")){
            uploadPercentage = 5;
            deletePercentage = 5;
        }
        else if(typeOfWorkload.equals("balanced")) {
            uploadPercentage = 45;
            deletePercentage = 10;
        }


        ArrayList<String> deleteOps = new ArrayList<>();

        int noOfDeleteOpsReqd = Math.round(deletePercentage*noOfUploadOps/uploadPercentage);

        for(int i = 0; i<noOfDeleteOpsReqd; ++i){
            int random = ThreadLocalRandom.current().nextInt(0, noOfUploadOps);
            deleteOps.add("File"+random);
        }

        return deleteOps;
    }

    public static void writeOpsToFile(String outputFilePath, Map<String, Double> uploadOps, ArrayList<String> downloadOps, ArrayList<String> deleteOps) throws Exception
    {

        StringBuilder operations = new StringBuilder();

        int i =0;
        for(String fileName : uploadOps.keySet()){
            if(i > 0)
                operations.append("\n");
            operations.append("PUT,0," + fileName + "," + uploadOps.get(fileName));
            i++;
        }

        for (String fileName : downloadOps) {
            operations.append("\n");
            operations.append("GET,1," + fileName);
        }

        for (String fileName : deleteOps) {
            operations.append("\n");
            operations.append("DELETE,2," + fileName);
        }

        FileUtils.writeStringToFile(new File(outputFilePath), operations.toString());
    }

    public static void main(String args[]) throws Exception
    {
        generateWorkload("upload intensive", "small", "/Users/spadigi/Desktop/greenSwiftSimulation/GSS/CloudSimEx/modules/cloudsim/src/main/java/org" +
                "/cloudbus/cloudsimdisk/examples/GenerateDataset/dataset.txt", 1000, 2);
    }
}
