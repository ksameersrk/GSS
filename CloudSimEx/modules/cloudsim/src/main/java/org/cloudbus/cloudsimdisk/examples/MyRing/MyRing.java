package org.cloudbus.cloudsimdisk.examples.MyRing;

import org.cloudbus.cloudsimdisk.examples.MyConstants;
import org.cloudbus.cloudsimdisk.examples.NewRing;
import org.cloudbus.cloudsimdisk.examples.Node;
import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created by skulkarni9 on 2/26/17.
 */
public class MyRing
{
    Map<String,MyRegion> myRegions;
    double weight;

    public MyRing()
    {
        this.myRegions = new HashMap();
        this.weight = 0.0;
    }

    public Collection<MyRegion> getAllRegions()
    {
        return this.myRegions.values();
    }

    public boolean containsByName(String regionName)
    {
        return this.myRegions.containsKey(regionName);
    }

    public MyRegion getRegionByName(String regionName)
    {
        return this.myRegions.get(regionName);
    }

    public void addRegion(MyRegion myRegion)
    {
        if(!this.containsByName(myRegion.getName()))
        {
            this.myRegions.put(myRegion.getName(), myRegion);
        }
    }

    public double getWeight() {
        this.calculateWeight();
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void calculateWeight() {
        double weight = 0.0;
        for(MyRegion myRegion : this.getAllRegions())
        {
            weight += myRegion.getWeight();
        }
        this.setWeight(weight);
    }

    @Override
    public String toString()
    {
        return this.myRegions.values().toString();
    }

    public static MyRing buildRing(String fileName, int nodeCount)
    {
        MyRing myRing = new MyRing();
        File file = new File(fileName);
        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            for(int line=0; line<nodeCount; line++)
            {
                String data[] = in.readLine().trim().split(",");
                String regionName = data[0];
                String zoneName = data[1];
                String nodeName = data[2];
                double weight = Double.parseDouble(data[3]);

                MyRegion myRegion;
                MyZone myZone;
                MyNode myNode;

                myRegion = myRing.containsByName(regionName) ? myRing.getRegionByName(regionName) : new MyRegion(regionName);
                myZone = myRegion.containsByName(zoneName) ? myRegion.getZoneByName(zoneName) : new MyZone(zoneName);

                myNode = new MyNode();
                myNode.setName(nodeName);
                myNode.setWeight(weight);
                myNode.setHddModel(MyConstants.STORAGE_MODEL_HDD);
                myNode.setHddPowerModel(MyConstants.STORAGE_POWER_MODEL_HDD);
                myNode.setSpunDown(false);

                myZone.addNode(myNode);
                myRegion.addZone(myZone);
                myRing.addRegion(myRegion);
            }
        }
        catch (Exception e)
        {
            new Exception("Main");
        }
        return myRing;
    }

    public static void main(String[] args)
    {
        MyRing myRing = buildRing("modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/MyRing/rings.txt", 8);
        System.out.println(myRing);
        System.out.println(myRing.getWeight());
    }
}