package org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by spadigi on 4/14/17.
 */
public class StartingFileListGenerator {

    public static String filterWorkload(String readFromPath) throws IOException {
        String filteredWorkloadPath = "";
        try (BufferedReader br = new BufferedReader(new FileReader(new File(readFromPath)))) {
            System.out.println("Starting filtering of input workload ... \n");

            int count = 0;
            String line;
            String filteredFileContents = "";
            String inputLog = "";

            ArrayList<String>  tmp = new ArrayList<String>(Arrays.asList(readFromPath.split("/")));
            tmp.remove(tmp.size()-1);
            filteredWorkloadPath = String.join("/", tmp) + "/workload_filtered.txt";
            System.out.print("Filtered workload path : " + filteredWorkloadPath);

            FileUtils.writeStringToFile(new File(filteredWorkloadPath), filteredFileContents.toString());

            ArrayList<String> addedFileList = new ArrayList<>();
            ArrayList<String> deletedFileList = new ArrayList<>();

            while((line = br.readLine()) != null) {
                String data[] = line.split(",");

                if (data[0].equals("PUT")) {
                    if (addedFileList.contains(data[2]) == false)
                    {
                        addedFileList.add(data[2]);
                        filteredFileContents = filteredFileContents + line + "\n";
                        if (deletedFileList.contains(data[2])) {
                            deletedFileList.remove(deletedFileList.indexOf(data[2]));
                        }
                    }
                }

                else if (data[0].equals("GET")) {
                    if (deletedFileList.contains(data[2]) == false){
                        filteredFileContents = filteredFileContents + line + "\n";
                        if (addedFileList.contains(data[2]) == false)
                            addedFileList.add(data[2]);
                    }
                }
                else if (data[0].equals("UPDATE")) {
                    if (deletedFileList.contains(data[2]) == false){
                        filteredFileContents = filteredFileContents + line + "\n";
                        if (addedFileList.contains(data[2]) == false)
                            addedFileList.add(data[2]);
                    }
                }
                else if (data[0].equals("DELETE")) {
                    if (deletedFileList.contains(data[2]) == false){
                        deletedFileList.add(data[2]);
                        addedFileList.remove(data[2]);
                        filteredFileContents = filteredFileContents + line + "\n";
                    }
                }
                count = count + 1;

                if(count%100 == 0){
                    FileUtils.writeStringToFile(new File(filteredWorkloadPath), filteredFileContents.toString(), true);
                    filteredFileContents = "";

                }
            }

            FileUtils.writeStringToFile(new File(filteredWorkloadPath), filteredFileContents.toString(), true);
        }

        return filteredWorkloadPath;
    }

    public static void generateStartingFile(String readFromPath, String writeToPathStartingFile, String writeToPathInputLog) throws IOException{
        try (BufferedReader br = new BufferedReader(new FileReader(new File(readFromPath)))) {
            System.out.println("Starting inputLog and startingFileList generation ...");
            int count = 0;
            String line;
            String startingFileList = "";
            String inputLog = "";

            FileUtils.writeStringToFile(new File(writeToPathStartingFile), startingFileList.toString());
            FileUtils.writeStringToFile(new File(writeToPathInputLog), inputLog.toString());

            HashSet<String> filesAdded = new HashSet<>();
            while ((line = br.readLine()) != null) {
                count = count + 1;
                System.out.println(count);
                line = line.trim();
                String data[] = line.split(",");

                if (data[0].equals("PUT")) {
                    filesAdded.add(data[2]);
                    inputLog = inputLog + line + "\n";
                }
                else {
                    if(filesAdded.contains(data[2])){
                        // do nothing
                    } else if (data[0].equals("UPDATE")) {
                        startingFileList = startingFileList + data[2] + "," + data[3] + "\n";
                        filesAdded.add(data[2]);
                    }

                    if(data[0].equals("UPDATE")){
                        inputLog = inputLog + line + "\n";
                    }
                    else {
                        inputLog = inputLog + data[0] + "," + data[1] + "," + data[2]  + "\n";
                    }
                }

                if(count%100 == 0){
                    FileUtils.writeStringToFile(new File(writeToPathStartingFile), startingFileList.toString(), true);
                    FileUtils.writeStringToFile(new File(writeToPathInputLog), inputLog.toString(), true);

                    startingFileList = "";
                    inputLog = "";
                }
            }

            startingFileList = startingFileList.trim();
            inputLog = inputLog.trim();

            FileUtils.writeStringToFile(new File(writeToPathStartingFile), startingFileList.toString(), true);
            FileUtils.writeStringToFile(new File(writeToPathInputLog), inputLog.toString(), true);

        }
    }

    public static void main(String args[]) throws IOException{

        String readFromPath = "/media/sai/New Volume/greenSwiftSimulation/GSS/CloudSimEx/files/basic/operations/workload_medium.txt";
        String writeToPath_starting_file = "/Users/spadigi/Desktop/greenSwiftSimulation/workload/pre-processing-workloads/harvard_research_graph" +
                "/GSS_style_starting_file_dummy.txt";
        String writeToPathInputLog = "/Users/spadigi/Desktop/greenSwiftSimulation/workload/pre-processing-workloads/harvard_research_graph" +
                "/GSS_style_input_log_dummy.txt";

        filterWorkload(readFromPath);
        //generateStartingFile(readFromPath, writeToPath_starting_file, writeToPathInputLog);
    }
}
