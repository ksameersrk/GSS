package org.cloudbus.cloudsimdisk.examples;

import java.util.ArrayList;

/**
 * Created by sai on 2/11/16.
 */
public class Tasks {
    ArrayList<String> PUTrequestArrivalTimesSource;
    ArrayList<String> dataFiles;
    ArrayList<String> GETrequestArrivalTimesSource;
    ArrayList<String> reqdFiles;

    Node node;

    public Tasks(Node node, String newTask)
    {
        dataFiles = new ArrayList<>();
        reqdFiles = new ArrayList<>();
        PUTrequestArrivalTimesSource = new ArrayList<>();
        GETrequestArrivalTimesSource = new ArrayList<>();
        String tmp[] = newTask.split(",");
        if (tmp[0].equals("PUT")) {
            PUTrequestArrivalTimesSource.add(tmp[1]);
            dataFiles.add(tmp[2] + "," + tmp[3]);
        }
        else if (tmp[0].equals("GET")) {
            GETrequestArrivalTimesSource.add(tmp[1]);
            reqdFiles.add(tmp[2]+"");
        }
        this.node = node;
    }

    public Node getNode()
    {
        return this.node;
    }

    public void addTask(String newTask)
    {
        String tmp[] = newTask.split(",");
        if (tmp[0].equals("PUT")) {
            PUTrequestArrivalTimesSource.add(tmp[1]);
            dataFiles.add(tmp[2] + "," + tmp[3]);
        }
        else if (tmp[0].equals("GET")) {
            GETrequestArrivalTimesSource.add(tmp[1]);
            reqdFiles.add(tmp[2]+"");
        }

    }

    public String getArrivalFile()
    {
        StringBuilder sb = new StringBuilder();
        for(String str : PUTrequestArrivalTimesSource)
        {
            sb.append(str+"\n");
        }
        for(String str : GETrequestArrivalTimesSource)
        {
            sb.append(str+"\n");
        }

        return sb.toString().trim();
    }

    public String getDataFile()
    {
        StringBuilder sb = new StringBuilder();
        for(String str : dataFiles)
        {
            sb.append(str+"\n");
        }
        return sb.toString().trim();
    }

    public String getFiles()
    {
        StringBuilder sb = new StringBuilder();
        for(String str : dataFiles)
        {
            sb.append(str+",");
        }
        for(String str : reqdFiles)
        {
            sb.append(str+"\n");
        }
        return sb.toString().trim();
    }

    public String getReqdFile()
    {
        StringBuilder sb = new StringBuilder();
        for(String str : reqdFiles)
        {
            sb.append(str+"\n");
        }
        return sb.toString().trim();
    }

    public String toString()
    {
        return node + "\n"+ dataFiles + "\n" + PUTrequestArrivalTimesSource + "\n" + reqdFiles + "\n" + GETrequestArrivalTimesSource + "----------\n"+getArrivalFile()+"\n";
    }
}
