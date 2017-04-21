package org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by spadigi on 4/14/17.
 */
public class StartingFileListGenerator {
    public static void generateStartingFile(String readFromPath, String writeToPathStartingFile, String writeToPathInputLog) throws IOException{
        try (BufferedReader br = new BufferedReader(new FileReader(new File(readFromPath)))) {
            System.out.println("Starting inputLog and startingFileList generation");
            int count = 0;
            String line;
            String startingFileList = "";
            String inputLog = "";
            HashSet<String> filesAdded = new HashSet<>();
            while ((line = br.readLine()) != null) {
                count = count + 1;
                System.out.print(count + ", ");
                line = line.trim();
                String data[] = line.split(",");

                if (data[0].equals("PUT")) {
                    filesAdded.add(data[2]);
                    inputLog = inputLog + line + "\n";
                }
                else {
                    if(filesAdded.contains(data[2])){
                        // do nothing
                    } else {
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
            }

            startingFileList = startingFileList.trim();
            inputLog = inputLog.trim();

            FileUtils.writeStringToFile(new File(writeToPathStartingFile), startingFileList.toString());
            FileUtils.writeStringToFile(new File(writeToPathInputLog), inputLog.toString());

        }
    }

    public static void main(String args[]) throws IOException{
        String readFromPath = "/Users/spadigi/Desktop/greenSwiftSimulation/workload/harvard/research/5k_ops_GSS_style.txt";
        String writeToPath_starting_file = "/Users/spadigi/Desktop/greenSwiftSimulation/workload/pre-processing-workloads/harvard_research_graph" +
                "/GSS_style_starting_file.txt";
        String writeToPathInputLog = "/Users/spadigi/Desktop/greenSwiftSimulation/workload/pre-processing-workloads/harvard_research_graph" +
                "/GSS_style_input_log.txt";
        generateStartingFile(readFromPath, writeToPath_starting_file, writeToPathInputLog);
    }
}
