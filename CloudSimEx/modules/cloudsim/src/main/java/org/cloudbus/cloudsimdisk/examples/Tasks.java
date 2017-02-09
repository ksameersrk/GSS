package org.cloudbus.cloudsimdisk.examples;

import java.util.ArrayList;

/**
 * Created by sai on 2/11/16.
 */
public class Tasks {
    ArrayList<String> PUTrequestArrivalTimesSource = new ArrayList<String>();
    ArrayList<String> dataFiles = new ArrayList<String>();
    ArrayList<String> GETrequestArrivalTimesSource = new ArrayList<String>();
    ArrayList<String> reqdFiles = new ArrayList<String>();
    ArrayList<String> UPDATErequestArrivalTimesSource = new ArrayList<String>();
    ArrayList<String> updateFiles = new ArrayList<String>();
    ArrayList<String> DELETErequestArrivalTimesSource = new ArrayList<String>();
    ArrayList<String> deleteFiles = new ArrayList<String>();
    Node node;

    public Tasks(Node node, String newTask)
    {
        dataFiles = new ArrayList<>();
        reqdFiles = new ArrayList<>();
        updateFiles = new ArrayList<>();
        PUTrequestArrivalTimesSource = new ArrayList<>();
        GETrequestArrivalTimesSource = new ArrayList<>();
        String tmp[] = newTask.split(",");
        if (tmp[0].equals("PUT")) {
            PUTrequestArrivalTimesSource.add(tmp[1]);
            //dataFiles.add(tmp[2] + "," + tmp[3]);
            dataFiles.add(tmp[2]);
        }
        else if (tmp[0].equals("GET")) {
            GETrequestArrivalTimesSource.add(tmp[1]);
            reqdFiles.add(tmp[2]+"");
        }
        else if (tmp[0].equals("UPDATE")) {
            UPDATErequestArrivalTimesSource.add(tmp[1]);
            //updateFiles.add(tmp[2] + "," + tmp[3]);
            updateFiles.add(tmp[2]);
        }
        else if (tmp[0].equals("DELETE")) {
            DELETErequestArrivalTimesSource.add(tmp[1]);
            deleteFiles.add(tmp[2]+"");
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
            //dataFiles.add(tmp[2] + "," + tmp[3]);
            dataFiles.add(tmp[2]);
        }
        else if (tmp[0].equals("GET")) {
            GETrequestArrivalTimesSource.add(tmp[1]);
            reqdFiles.add(tmp[2]+"");
        }
        else if (tmp[0].equals("UPDATE")) {
            UPDATErequestArrivalTimesSource.add(tmp[1]);
            //updateFiles.add(tmp[2] + "," + tmp[3]);
            updateFiles.add(tmp[2]);
        }
        else if (tmp[0].equals("DELETE")) {
            DELETErequestArrivalTimesSource.add(tmp[1]);
            deleteFiles.add(tmp[2]+"");
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
        for(String str : UPDATErequestArrivalTimesSource)
        {
            sb.append(str+"\n");
        }
        for(String str : DELETErequestArrivalTimesSource)
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
            sb.append(str+", ");
        }
        for(String str : reqdFiles)
        {
            sb.append(str+", ");
        }
        for(String str : updateFiles)
        {
            sb.append(str+", ");
        }
        for(String str : deleteFiles)
        {
            sb.append(str+", ");
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

    public String getUpdateFile()
    {
        StringBuilder sb = new StringBuilder();
        for(String str : updateFiles)
        {
            sb.append(str+"\n");
        }
        return sb.toString().trim();
    }

    public String getDeleteFile()
    {
        StringBuilder sb = new StringBuilder();
        for(String str : deleteFiles)
        {
            sb.append(str+"\n");
        }
        return sb.toString().trim();
    }

    public String toString()
    {
        return node + "\n"+ dataFiles + "\n" + PUTrequestArrivalTimesSource + "\n" + reqdFiles + "\n" + GETrequestArrivalTimesSource + "\n" + updateFiles+ "\n" + UPDATErequestArrivalTimesSource+ "\n" + deleteFiles + "\n" + DELETErequestArrivalTimesSource + "\n" + "----------\n"+getArrivalFile()+"\n";
    }
}
