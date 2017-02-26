package org.cloudbus.cloudsimdisk.examples.MyRing;

import org.cloudbus.cloudsimdisk.examples.MyConstants;

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
    long numberOfPartitions;
    long totalNumberOfPartitions;
    int replicas;

    public MyRing(int partitonPower, int replicas)
    {
        this.myRegions = new HashMap();
        this.replicas = replicas;
        this.numberOfPartitions = (long) 1<<partitonPower;
        this.totalNumberOfPartitions = this.numberOfPartitions * this.replicas;
        this.calculateNumberOfPartitionsByWeight();
    }

    public Collection<MyRegion> getAllRegions()
    {
        return this.myRegions.values();
    }

    public long getTotalNumberOfPartitions()
    {
        return this.totalNumberOfPartitions;
    }

    public int getReplicas()
    {
        return this.replicas;
    }

    public void calculateNumberOfPartitionsByWeight()
    {
        for(MyRegion myRegion : this.getAllRegions())
        {
            myRegion.calculateNumberOfPartitionsByWeight(this.getTotalNumberOfPartitions(), this.getWeight());
        }
    }

    public void calculateNumberOfPartitionsByDispersion()
    {
        double regionPartitions = this.getTotalNumberOfPartitions()/this.getAllRegions().size();
        for(MyRegion myRegion : this.getAllRegions())
        {
            myRegion.calculateNumberOfPartitionsByDispersion(regionPartitions);
        }
    }

    public void calculateNumberOfPartitionsDifference()
    {
        for(MyRegion myRegion : this.getAllRegions())
        {
            myRegion.calculateNumberOfPartitionsDifference();
        }
    }

    public void calculatePartitions()
    {
        this.calculateNumberOfPartitionsByWeight();
        this.calculateNumberOfPartitionsByDispersion();
        this.calculateNumberOfPartitionsDifference();
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
        double weight = 0.0;
        for(MyRegion myRegion : this.getAllRegions())
        {
            weight += myRegion.getWeight();
        }
        return weight;
    }

    public double getAverageWeight() {
        double weight = 0.0;
        for(MyRegion myRegion : this.getAllRegions())
        {
            weight += myRegion.getAverageWeight();
        }
        return weight/this.myRegions.size();
    }

    @Override
    public String toString()
    {
        return this.myRegions.values().toString();
    }

    public static MyRing buildRing(String fileName, int nodeCount, int partitionPower, int replicas)
    {
        MyRing myRing = new MyRing(partitionPower, replicas);
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
        MyRing myRing = buildRing("modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/MyRing/rings.txt", 8, 4, 3);
        System.out.println(myRing);
        System.out.println("Total Weight : "+myRing.getWeight());
        System.out.println("Average Weight : "+myRing.getAverageWeight());
        myRing.calculatePartitions();
        System.out.println("Total Number of Partitions : "+myRing.getTotalNumberOfPartitions());

        display(myRing);
    }

    public static void display(MyRing myRing)
    {
        for(MyRegion myRegion : myRing.getAllRegions())
        {
            System.out.println(myRegion.getName()+", ByWeight : "+myRegion.getNumberOfPartitionsByWeight()+
                    ", ByDispersion : "+myRegion.getNumberOfPartitionsByDispersion()+", Diff : "+myRegion.getNumberOfPartitionsDifference());
            for(MyZone myZone : myRegion.getAllZones())
            {
                System.out.println("\t"+myZone.getName()+", ByWeight : "+myZone.getNumberOfPartitionsByWeight()+
                        ", ByDispersion : "+myZone.getNumberOfPartitionsByDispersion()+" , Diff : "+myZone.getNumberOfPartitionsDifference());
                for(MyNode myNode : myZone.getAllNodes())
                {
                    System.out.println("\t\t"+myNode.getName()+", ByWeight : "+myNode.getNumberOfPartitionsByWeight()+
                            ", ByDispersion : "+myNode.getNumberOfPartitionsByDispersion()+", Diff : "+myNode.getNumberOfPartitionsDifference());
                }
            }
        }
    }
}