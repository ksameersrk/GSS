package org.cloudbus.cloudsimdisk.examples.MyRing;

import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;

/**
 * Created by skulkarni9 on 2/26/17.
 */
public class MyNode
{
    String name;
    double weight;
    private StorageModelHdd hddModel;
    private PowerModelHdd hddPowerModel;
    private boolean isSpunDown;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public StorageModelHdd getHddModel() {
        return hddModel;
    }

    public void setHddModel(StorageModelHdd hddModel) {
        this.hddModel = hddModel;
    }

    public PowerModelHdd getHddPowerModel() {
        return hddPowerModel;
    }

    public void setHddPowerModel(PowerModelHdd hddPowerModel) {
        this.hddPowerModel = hddPowerModel;
    }

    public boolean isSpunDown() {
        return isSpunDown;
    }

    public void setSpunDown(boolean spunDown) {
        isSpunDown = spunDown;
    }

    @Override
    public String toString()
    {
        return this.getName();
    }
}
