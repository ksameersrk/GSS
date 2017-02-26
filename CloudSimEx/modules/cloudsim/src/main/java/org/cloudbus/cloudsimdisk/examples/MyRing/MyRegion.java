package org.cloudbus.cloudsimdisk.examples.MyRing;

import java.util.*;

/**
 * Created by skulkarni9 on 2/26/17.
 */
public class MyRegion
{
    Map<String, MyZone> myZones;
    String name;
    double weight;

    public MyRegion(String name)
    {
        this.name = name;
        this.myZones = new HashMap<>();
        this.weight = 0.0;
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
        this.calculateWeight();
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void calculateWeight() {
        double weight = 0.0;
        for(MyZone myZone : this.getAllZones())
        {
            weight += myZone.getWeight();
        }
        this.setWeight(weight);
    }

    @Override
    public String toString()
    {
        return this.name+" : "+this.myZones.toString();
    }
}
