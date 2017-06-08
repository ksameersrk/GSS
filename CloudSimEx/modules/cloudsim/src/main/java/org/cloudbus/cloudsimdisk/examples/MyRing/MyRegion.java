package org.cloudbus.cloudsimdisk.examples.MyRing;

import java.io.Serializable;
import java.util.*;

/**
 * Created by skulkarni9 on 2/26/17.
 */
public class MyRegion implements Serializable
{
    Map<String, MyZone> myZones;
    String name;
    double numberOfPartitionsByWeight;
    double numberOfPartitionsByDispersion;
    double numberOfPartitionsDifference;

    public MyRegion(String name)
    {
        this.name = name;
        this.myZones = new HashMap<>();
    }

    public String getName()
    {
        return this.name;
    }

    public void addZone(MyZone myZone)
    {
        if(!this.containsByName(myZone.getName()))
        {
            this.myZones.put(myZone.getName(), myZone);
        }
    }

    public boolean containsByName(String zoneName)
    {
        return this.myZones.containsKey(zoneName);
    }

    public MyZone getZoneByName(String zoneName)
    {
        return this.myZones.get(zoneName);
    }

    public Collection<MyZone> getAllZones()
    {
        return myZones.values();
    }

    public double getWeight() {
        double weight = 0.0;
        for(MyZone myZone : this.getAllZones())
        {
            weight += myZone.getWeight();
        }
        return weight;
    }

    public double getAverageWeight() {
        double weight = 0.0;
        for(MyZone myZone : this.getAllZones())
        {
            weight += myZone.getAverageWeight();
        }
        return weight/this.myZones.size();
    }

    public double getNumberOfPartitionsByWeight() {
        return numberOfPartitionsByWeight;
    }

    public void setNumberOfPartitionsByWeight(double numberOfPartitions) {
        this.numberOfPartitionsByWeight = numberOfPartitions;
    }

    public void calculateNumberOfPartitionsByWeight(long totalNumberOfPartitions, double totalWeight)
    {
        for(MyZone myZone : this.getAllZones())
        {
            myZone.calculateNumberOfPartitionsByWeight(totalNumberOfPartitions, totalWeight);
        }
        double partitions = 0.0;
        for(MyZone myZone : this.getAllZones())
        {
            partitions += myZone.getNumberOfPartitionsByWeight();
        }
        this.setNumberOfPartitionsByWeight(partitions);
    }

    public double getNumberOfPartitionsByDispersion() {
        return numberOfPartitionsByDispersion;
    }

    public void setNumberOfPartitionsByDispersion(double numberOfPartitionsByDispersion) {
        this.numberOfPartitionsByDispersion = numberOfPartitionsByDispersion;
    }

    public void calculateNumberOfPartitionsByDispersion(double partitons)
    {
        double zonePartitions = partitons / this.getAllZones().size();
        for(MyZone myZone : this.getAllZones())
        {
            myZone.calculateNumberOfPartitionsByDispersion(zonePartitions);
        }
        this.setNumberOfPartitionsByDispersion(partitons);
    }

    public double getNumberOfPartitionsDifference() {
        return numberOfPartitionsDifference;
    }

    public void setNumberOfPartitionsDifference(double numberOfPartitionsDifference) {
        this.numberOfPartitionsDifference = numberOfPartitionsDifference;
    }

    public void calculateNumberOfPartitionsDifference() {
        for(MyZone myZone : this.getAllZones())
        {
            myZone.calculateNumberOfPartitionsDifference();
        }
        this.setNumberOfPartitionsDifference(this.getNumberOfPartitionsByWeight()-this.getNumberOfPartitionsByDispersion());
    }

    public void decrementPartition()
    {
        this.numberOfPartitionsByWeight--;
    }

    public boolean contiansPartition(Integer partition)
    {
        for(MyZone myZone : this.getAllZones())
        {
            if(myZone.containsPartition(partition))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        return this.name+" : "+this.myZones.toString();
    }
}
