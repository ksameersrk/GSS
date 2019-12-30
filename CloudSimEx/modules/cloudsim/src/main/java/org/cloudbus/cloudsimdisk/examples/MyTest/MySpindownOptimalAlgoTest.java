package org.cloudbus.cloudsimdisk.examples.MyTest;

import org.cloudbus.cloudsimdisk.examples.MyRing.MyNode;
import org.cloudbus.cloudsimdisk.examples.MyRing.MyRing;
import org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms.OptimalHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms.MySpinDownOptimalAlgorithm.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by skulkarni9 on 4/17/17.
 */
public class MySpindownOptimalAlgoTest {
    MyRing myRing;
    OptimalHelper optimalHelper;
    int numberOfPartition;

    @Before
    public void beforeTest(){
        int nodeCount = 8;
        int partitionPower = 4;
        int replicas = 3;
        double overloadPercent = 10.0;
        String ringInputPath = "modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/SpinDownAlgorithms/smallRing.txt";
        myRing = MyRing.buildRing(ringInputPath,
                nodeCount, partitionPower, replicas, overloadPercent);

        numberOfPartition = (int)myRing.getNumberOfPartitions();
        Map<MyNode, List<Integer>> newMap = getSortedNodeMap(myRing.getNodeToPartition(), numberOfPartition);

        optimalHelper = new OptimalHelper();
        optimalHelper.setMaxNodes(0);
        optimalHelper.setNodes(new ArrayList<>());
        findOptimalSolution(powerSet(newMap.keySet()), optimalHelper, newMap, numberOfPartition);
    }

    @Test
    public void testPowerSet(){
        Map<MyNode, List<Integer>> newMap = getSortedNodeMap(myRing.getNodeToPartition(), numberOfPartition);
        assertEquals(powerSet(newMap.keySet()).size(), 256);
    }

    @Test
    public void testMaxSpindownNodes(){
        assertTrue(optimalHelper.getMaxNodes() > 0);
    }

    @Test
    public void testConsistency(){
        Map<MyNode, List<Integer>> newMap = getSortedNodeMap(myRing.getNodeToPartition(), numberOfPartition);
        optimalHelper = new OptimalHelper(); // Just checking if it gives same result
        optimalHelper.setMaxNodes(0);
        optimalHelper.setNodes(new ArrayList<>());
        findOptimalSolution(powerSet(newMap.keySet()), optimalHelper, newMap, numberOfPartition);
        int count1 = optimalHelper.getMaxNodes();

        optimalHelper = new OptimalHelper(); // Just checking if it gives same result
        optimalHelper.setMaxNodes(0);
        optimalHelper.setNodes(new ArrayList<>());
        findOptimalSolution(powerSet(newMap.keySet()), optimalHelper, newMap, numberOfPartition);
        int count2 = optimalHelper.getMaxNodes();

        assertEquals(count1, count2);
    }

    @Test
    public void testCheckForPartitionCount(){
        Map<MyNode, List<Integer>> newMap = getSortedNodeMap(myRing.getNodeToPartition(), numberOfPartition);
        List<MyNode> nodes = myRing.getPrimaryNodes("asd");
        assertEquals(newMap.get(nodes.get(0)).size(), numberOfPartition);
    }
}
