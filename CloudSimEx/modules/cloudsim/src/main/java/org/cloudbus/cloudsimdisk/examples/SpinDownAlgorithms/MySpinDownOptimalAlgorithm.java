package org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms;

import org.cloudbus.cloudsimdisk.examples.MyRing.MyNode;
import org.cloudbus.cloudsimdisk.examples.MyRing.MyRing;
import java.util.*;

/**
 * Created by skulkarni9 on 4/10/17.
 */
public class MySpinDownOptimalAlgorithm
{
    public static void main(String args[])
    {
        int nodeCount = 8;
        int partitionPower = 4;
        int replicas = 3;
        double overloadPercent = 10.0;
        String ringInputPath = "modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/SpinDownAlgorithms/smallRing.txt";
        MyRing myRing = MyRing.buildRing(ringInputPath,
                nodeCount, partitionPower, replicas, overloadPercent);

        int numberOfPartition = (int)myRing.getNumberOfPartitions();
        displayNodeMap(myRing.getNodeToPartition());

        Map<MyNode, List<Integer>> newMap = getSortedNodeMap(myRing.getNodeToPartition(), numberOfPartition);
        for(MyNode myNode : newMap.keySet())
        {
            System.out.println(myNode+" : "+newMap.get(myNode));
        }

        OptimalHelper optimalHelper = new OptimalHelper();
        optimalHelper.setMaxNodes(0);
        optimalHelper.setNodes(new ArrayList<>());
        /*
        for(Set<MyNode> myNodes : powerSet(newMap.keySet()))
        {
            System.out.println(myNodes);
        }
        System.out.println("_________________");
        */
        System.out.println("Results : ");
        findOptimalSolution(powerSet(newMap.keySet()), optimalHelper, newMap, numberOfPartition);
        System.out.println("Output : " + optimalHelper);

        optimalHelper = new OptimalHelper(); // Just checking if it gives same result
        optimalHelper.setMaxNodes(0);
        optimalHelper.setNodes(new ArrayList<>());
        findOptimalSolution(powerSet(newMap.keySet()), optimalHelper, newMap, numberOfPartition);
        System.out.println("Output : " + optimalHelper);
        System.out.println("END");
    }

    public static void findOptimalSolution(Set<Set<MyNode>> sets, OptimalHelper optimalHelper, Map<MyNode, List<Integer>> newMap, int numberOfPartition)
    {
        List<List<Integer>> partitionList;
        for(Set<MyNode> myNodes : sets)
        {
            partitionList = new ArrayList<>();
            for(MyNode myNode : myNodes)
            {
                List<Integer> innerList = new ArrayList<>(newMap.get(myNode));
                partitionList.add(innerList);
            }
            List<Integer> finalPartitionList = sumList(partitionList, numberOfPartition);
            if(checkForValidity(finalPartitionList) && myNodes.size() > optimalHelper.getMaxNodes())
            {
                optimalHelper.setNodes(new ArrayList<>(myNodes));
                optimalHelper.setMaxNodes(myNodes.size());
            }
        }
    }

    public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new HashSet<Set<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
        for (Set<T> set : powerSet(rest)) {
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    public static List<Integer> sumList(List<List<Integer>> a, int numberOfPartition)
    {
        List<Integer> result = new ArrayList<>();
        for(int i=0; i<numberOfPartition; i++)
        {
            result.add(0);
        }
        //System.out.println("a"+result.size());
        for(int i=0; i<a.size(); i++)
        {
            for(int j=0; j<numberOfPartition; j++)
            {
                result.set(j, result.get(j)+a.get(i).get(j));
            }
        }
        //System.out.println("b"+result.size());
        return result;
    }

    public static boolean checkForValidity(List<Integer> list)
    {
        for(int n : list)
        {
            if(n > 1)
            {
                return false;
            }
        }
        return true;
    }

    public static void displayNodeMap(Map<MyNode, List<Integer>> nodeToPartition)
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

    public static Map<MyNode, List<Integer>> getSortedNodeMap(Map<MyNode, List<Integer>> nodeToPartition, int numberOfPartition)
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