package org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms;

import org.cloudbus.cloudsimdisk.examples.MyRing.MyNode;
import org.cloudbus.cloudsimdisk.examples.MyRing.MyRing;

import java.util.*;

/*
    Usage :
        public static List<MyNode> getSpunDownNodesWithPercent(MyRing myRing, int minPercent, int maxTrials)
        public static List<MyNode> getSpunDownNodesWithPercent(MyRing myRing, int minPercent)
        public static List<MyNode> getSpunDownNodesWithPercent(MyRing myRing)

    Default values :
        minPercent = 20
        maxTrials = 10
 */

public class MyNewRandomAlgorithm {

    public static void main(String args[]) {
        int nodeCount = 64;
        int partitionPower = 5;
        int replicas = 3;
        double overloadPercent = 10.0;
        String ringInputPath = "modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/SpinDownAlgorithms/smallRing.txt";
        MyRing myRing = MyRing.buildRing(ringInputPath,
                nodeCount, partitionPower, replicas, overloadPercent);

        List<MyNode> spunDownNodes = getSpunDownNodesWithPercent(myRing, 25, 1000);
        System.out.println("These are the spunDown Nodes");
        for(MyNode myNode : spunDownNodes) {
            System.out.println(myNode);
        }
    }

    public static List<MyNode> getSpunDownNodesWithPercent(MyRing myRing, int minPercent, int maxTrials) {
        int currentPercent = 0;
        List<MyNode> spunDownNodes = new ArrayList<>();
        int totalNumberOfNodes = myRing.getAllNodes().size();
        while (currentPercent < minPercent && maxTrials > 0) {
            spunDownNodes = getSpunDownNodes(myRing);
            currentPercent = (int) Math.ceil((spunDownNodes.size() * 100) / totalNumberOfNodes);
            maxTrials--;
            if(currentPercent < minPercent) {
                System.out.println("Not able to spinDown disks with given percentage : "+minPercent+", Achieved percentage : "+currentPercent + ".  Trying again!");
            }
        }
        System.out.println("Spin down successful!");
        System.out.println("Achieved percentage : "+currentPercent);
        return spunDownNodes;
    }

    public static List<MyNode> getSpunDownNodesWithPercent(MyRing myRing, int minPercent) {
        int currentPercent = 0;
        int maxTrials = 10;
        int totalNumberOfNodes = myRing.getAllNodes().size();
        List<MyNode> spunDownNodes = new ArrayList<>();
        while (currentPercent < minPercent && maxTrials > 0) {
            spunDownNodes = getSpunDownNodes(myRing);
            currentPercent = (int) Math.ceil((spunDownNodes.size() * 100) / totalNumberOfNodes);
            maxTrials--;
        }
        if(currentPercent < minPercent) {
            // throw new RuntimeException("Not able to spunDown disks with given percentage : "+minPercent+", Acheived percentage : "+currentPercent);
            System.out.println("Not able to spunDown disks with given percentage : "+minPercent+", Acheived percentage : "+currentPercent);
            getSpunDownNodesWithPercent(myRing, minPercent);

        }
        return spunDownNodes;
    }

    public static List<MyNode> getSpunDownNodesWithPercent(MyRing myRing) {
        int currentPercent = 0;
        int maxTrials = 10;
        int minPercent = 20;
        int totalNumberOfNodes = myRing.getAllNodes().size();
        List<MyNode> spunDownNodes = new ArrayList<>();
        while (currentPercent < minPercent && maxTrials > 0) {
            spunDownNodes = getSpunDownNodes(myRing);
            currentPercent = (int) Math.ceil((spunDownNodes.size() * 100) / totalNumberOfNodes);
            maxTrials--;
        }
        if(currentPercent < minPercent) {
            throw new RuntimeException("Not able to spunDown disks with given percentage : "+minPercent+", Acheived percentage : "+currentPercent);
        }
        return spunDownNodes;
    }

    private static List<MyNode> getSpunDownNodes(MyRing myRing) {
        int numberOfPartition = (int)myRing.getNumberOfPartitions();
        //displayNodeMap(myRing.getNodeToPartition());
        Map<MyNode, List<Integer>> sortedNodeMapBinary = getSortedNodeMap(myRing.getNodeToPartition(), numberOfPartition);
        List<MyNode> myNodeList = myRing.getAllNodes();
        Collections.shuffle(myNodeList, new Random());
        List<MyNode> spunDownNodes = new ArrayList<>();
        int sums[] = new int[(int)myRing.getNumberOfPartitions()];
        for(MyNode myNode : myNodeList) {
            if(isNodeSelectionValid(myNode, sortedNodeMapBinary, sums)) {
                spunDownNodes.add(myNode);
            } else {
                break;
            }
        }
        return spunDownNodes;
    }

    private static boolean isNodeSelectionValid(MyNode newSpinDownSelectionNode, Map<MyNode, List<Integer>> sortedNodeMapBinary, int sums[]) {
        List<Integer> currentSum = sortedNodeMapBinary.get(newSpinDownSelectionNode);
        listSum(currentSum, sums);
        return isPartitionSumValid(sums);
    }

    private static boolean isPartitionSumValid(int sums[]) {
        for(int i=0; i<sums.length; i++) {
            if(sums[i] > 1) {
                return false;
            }
        }
        return true;
    }

    private static void listSum(List<Integer> elementSum, int sums[]) {
        for(int i=0; i<elementSum.size(); i++) {
            sums[i] += elementSum.get(i);
        }
    }

    private static void displayNodeMap(Map<MyNode, List<Integer>> nodeToPartition)
    {
        System.out.println("*********************");
        List<MyNode> keys = new ArrayList<>(nodeToPartition.keySet());
        Collections.sort(keys, Comparator.comparing(Object::toString));
        for(MyNode myNode : keys)
        {
            System.out.println("Node : "+myNode+", Partitions : "+nodeToPartition.get(myNode));
        }
        System.out.println("*********************\n\n");
    }

    private static Map<MyNode, List<Integer>> getSortedNodeMap(Map<MyNode, List<Integer>> nodeToPartition, int numberOfPartition)
    {
        Map<MyNode, List<Integer>> result = new HashMap<>();
        List<MyNode> keys = new ArrayList<>(nodeToPartition.keySet());
        Collections.sort(keys, Comparator.comparing(Object::toString));
        List<Integer> nos = new ArrayList<>();
        for(int i=0; i<numberOfPartition; i++)
        {
            nos.add(0);
        }
        for(MyNode myNode : keys)
        {
            List<Integer> tmp = new ArrayList<>(nos);
            for(int j=0; j<nodeToPartition.get(myNode).size(); j++)
            {
                int index = nodeToPartition.get(myNode).get(j);
                tmp.set(index, tmp.get(index)+1);
            }
            result.put(myNode, tmp);
        }
        return result;
    }
}
