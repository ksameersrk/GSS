package org.cloudbus.cloudsimdisk.examples.MyRing;

import org.cloudbus.cloudsimdisk.examples.Node;

import java.util.*;

/**
 * Created by skulkarni9 on 2/26/17.
 */
public class MyZone
{
    Map<String, MyNode> myNodes;
    String name;
    double weight;

    public MyZone(String name)
    {
        this.name = name;
        this.myNodes = new HashMap<>();
        this.weight = 0.0;
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

    public double getWeight() {
        this.calculateWeight();
        return weight;
    }

    public void setWeight(double weight)
    {
        this.weight = weight;
    }

    public void calculateWeight() {
        double weight = 0.0;
        for(MyNode n : this.getAllNodes())
        {
            weight += n.getWeight();
        }
        this.setWeight(weight);
    }

    @Override
    public String toString()
    {
        return this.myNodes.values().toString();
    }
}
