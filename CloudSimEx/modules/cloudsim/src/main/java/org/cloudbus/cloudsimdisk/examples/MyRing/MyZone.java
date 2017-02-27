package org.cloudbus.cloudsimdisk.examples.MyRing;

import java.util.*;

/**
 * Created by skulkarni9 on 2/26/17.
 */
public class MyZone
{
    Map<String, MyNode> myNodes;
    String name;
    double numberOfPartitionsByWeight;
    double numberOfPartitionsByDispersion;
    double numberOfPartitionsDifference;

    public MyZone(String name)
    {
        this.name = name;
        this.myNodes = new HashMap<>();
    }

    public String getName()
    {
        return this.name;
    }

    public void addNode(MyNode myNode)
    {
        this.myNodes.put(myNode.getName(), myNode);
    }

    public boolean containsByName(String nodeName)
    {
        return this.myNodes.containsKey(nodeName);
    }

    public MyNode getNodeByName(String nodeName)
    {
        return this.myNodes.get(nodeName);
    }

    public Collection<MyNode> getAllNodes()
    {
        return this.myNodes.values();
    }

    public double getAverageWeight()
    {
        return getWeight()/this.myNodes.size();
    }

    public double getWeight() {
        double weight = 0.0;
        for(MyNode n : this.getAllNodes())
        {
            weight += n.getWeight();
        }
        return weight;
    }

    public double getNumberOfPartitionsByWeight() {
        return numberOfPartitionsByWeight;
    }

    public void setNumberOfPartitionsByWeight(double numberOfPartitions) {
        this.numberOfPartitionsByWeight = numberOfPartitions;
    }

    public void calculateNumberOfPartitionsByWeight(long totalNumberOfPartitions, double totalWeight)
    {
        for(MyNode myNode : this.getAllNodes())
        {
            myNode.calculateNumberOfPartitionsByWeight(totalNumberOfPartitions, totalWeight);
        }
        double partitions = 0.0;
        for(MyNode myNode : this.getAllNodes())
        {
            partitions += myNode.getNumberOfPartitionsByWeight();
        }
        this.setNumberOfPartitionsByWeight(partitions);
    }

    public double getNumberOfPartitionsByDispersion() {
        return numberOfPartitionsByDispersion;
    }

    public void setNumberOfPartitionsByDispersion(double numberOfPartitionsByDispersion) {
        this.numberOfPartitionsByDispersion = numberOfPartitionsByDispersion;
    }

    public void calculateNumberOfPartitionsByDispersion(double partitions)
    {
        double nodePartitions = partitions / this.getAllNodes().size();
        for(MyNode myNode : this.getAllNodes())
        {
            myNode.calculateNumberOfPartitionsByDispersion(nodePartitions);
        }
        this.setNumberOfPartitionsByDispersion(partitions);
    }

    public double getNumberOfPartitionsDifference() {
        return numberOfPartitionsDifference;
    }

    public void setNumberOfPartitionsDifference(double numberOfPartitionsDifference) {
        this.numberOfPartitionsDifference = numberOfPartitionsDifference;
    }

    public void calculateNumberOfPartitionsDifference() {
        for(MyNode myNode : this.getAllNodes())
        {
            myNode.calculateNumberOfPartitionsDifference();
        }
        this.setNumberOfPartitionsDifference(this.getNumberOfPartitionsByWeight()-this.getNumberOfPartitionsByDispersion());
    }

    public void decrementPartition()
    {
        this.numberOfPartitionsByWeight--;
    }

    public boolean containsPartition(Integer partition)
    {
        for(MyNode myNode : this.getAllNodes())
        {
            if(myNode.containsPartition(partition))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        return this.myNodes.values().toString();
    }
}
