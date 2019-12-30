package org.cloudbus.cloudsimdisk.examples;

import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;

/**
 * Created by sai on 15/10/16.
 */
public class Node {

    private int id;
    private int zone;
    private double weight;
    private double desiredParts;
    private StorageModelHdd hddModel;
    private PowerModelHdd hddPowerModel;
    private boolean isSpunDown;

    public Node(int id, int zone, double weight,StorageModelHdd hddModel, PowerModelHdd hddPowerModel)
    {
        this.id = id;
        this.zone = zone;
        this.weight = weight;
        this.desiredParts = 0.0;
        this.hddModel = hddModel;
        this.hddPowerModel = hddPowerModel;
        this.isSpunDown = false;

    }

    public Node(int id, int zone, double weight,StorageModelHdd hddModel, PowerModelHdd hddPowerModel, boolean isSpunDown)
    {
        this.id = id;
        this.zone = zone;
        this.weight = weight;
        this.desiredParts = 0.0;
        this.hddModel = hddModel;
        this.hddPowerModel = hddPowerModel;
        this.isSpunDown = isSpunDown;
    }

    public String toString()
    {
        return "ID : "+id+", Zone : "+zone+", Weight : "+weight+", Desired Parts : "+desiredParts+" , Disk Model : "+hddModel.getModelNumber();
    }

    public int getID()
    {
        return this.id;
    }

    public int getZone()
    {
        return this.zone;
    }

    public double getWeight()
    {
        return this.weight;
    }

    public StorageModelHdd getStorageModel()
    {
        return this.hddModel;
    }

    public PowerModelHdd getPowerModel()
    {
        return this.hddPowerModel;
    }

    public double getDesiredParts()
    {
        return this.desiredParts;
    }

    public void decrementDesiredParts()
    {
        this.desiredParts -= 1;
    }

    public void setDesiredParts(double desiredParts)
    {
        this.desiredParts = desiredParts;
    }
    public  void  setIsSpunDown(boolean isSpunDown) {this.isSpunDown = isSpunDown;}

    public boolean equals(Node n)
    {
        return this.id == n.id;
    }

    public boolean getIsSpunDown(){ return this.isSpunDown; }


}
