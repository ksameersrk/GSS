package org.cloudbus.cloudsimdisk.examples.GenerateDataset;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FinalStage {
    static final String INPUT_SAMPLE_FILE = "/Users/skulkarni9/PersonalProjects/GSS-Other/azure/sample.txt";
    static final String OUTPUT_SAMPLE_FILE = "/Users/skulkarni9/PersonalProjects/GSS-Other/azure/sample_out.txt";
    static final String INPUT_FILE = "/Users/skulkarni9/PersonalProjects/GSS-Other/azure/workload_part1.txt";
    static final String OUTPUT_FILE = "/Users/skulkarni9/PersonalProjects/GSS-Other/azure/workload_final.txt";
    static final String UPDATE_OPS = "UPDATE";
    static final String GET_OPS = "GET";
    static final int flushIndex = 10;

    static Map<String, Boolean> booleanMap = new HashMap<>();

    public static void main(String[] args) {
        File fileInput = new File(INPUT_SAMPLE_FILE);
        File fileOutput = new File(OUTPUT_SAMPLE_FILE);
        int index = 0;
        int indexInLakh = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(fileInput))) {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileOutput));
            String line;
            while ((line = br.readLine()) != null) {
                index++;
                String fields[] = line.split(",");
                line = line + "\n";
                if(fields[0].equals(GET_OPS)) {
                    booleanMap.put(fields[2], false);

                    //Write to a file
                    bufferedWriter.write(line);

                } else {
                    if(booleanMap.containsKey(fields[2])) {
                        if(!booleanMap.get(fields[2])) {
                            booleanMap.put(fields[2], true);

                            //Write to file
                            bufferedWriter.write(line);

                        }
                    } else {
                        booleanMap.put(fields[2], true);

                        //Write to file
                        bufferedWriter.write(line);

                    }
                }

                if(index == flushIndex) {
                    index = 0;
                    indexInLakh++;
                    System.out.println(indexInLakh+" lakh ops completed");
                    bufferedWriter.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
