package org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms;

import org.cloudbus.cloudsimdisk.examples.MyRing.MyNode;

import java.util.List;

public class OptimalHelper
{
    int maxNodes;
    List<MyNode> nodes;

    public int getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    public List<MyNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<MyNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString()
    {
        return this.getMaxNodes()+" : "+this.getNodes().toString();
    }
}