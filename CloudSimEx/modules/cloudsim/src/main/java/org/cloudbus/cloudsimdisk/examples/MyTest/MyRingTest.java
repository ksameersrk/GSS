package org.cloudbus.cloudsimdisk.examples.MyTest;

import org.cloudbus.cloudsimdisk.examples.MyRing.MyRing;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by skulkarni9 on 4/16/17.
 */
public class MyRingTest {

    int nodeCount = 4;
    int partitionPower = 4;
    int replicas = 3;
    double overloadPercent = 10.0;
    String ringInputPath = "modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/MyRing/rings.txt";
    MyRing myRing;

    @Before
    public void before()
    {
        myRing = MyRing.buildRing(ringInputPath, nodeCount, partitionPower, replicas, overloadPercent);
    }

    @Test
    public void testReplicasCount() {
        assertEquals(myRing.getReplicas(), replicas);
    }

    @Test
    public void testPartitionCount() {
        assertEquals(myRing.getNumberOfPartitions(), 16);
    }

    @Test
    public void testWeightValue() {
        assertTrue(myRing.getWeight() < nodeCount);
    }

    @Test
    public void testRegionCount(){
        assertEquals(myRing.getAllRegions().size(), 1);
    }

    @Test
    public void testNodeCount(){
        assertEquals(myRing.getAllNodes().size(), nodeCount);
    }

    @Test
    public void testPrimaryNodesSize(){
        assertEquals(myRing.getPrimaryNodes("dasdsad").size(), 3);
        assertEquals(myRing.getPrimaryNodes("crew*^*(Y(*").size(), 3);
        assertEquals(myRing.getPrimaryNodes("DBUIQD*Y&*").size(), 3);
        assertEquals(myRing.getPrimaryNodes("j9823un99").size(), 3);
        assertEquals(myRing.getPrimaryNodes("(&#**())(#*&^%").size(), 3);
    }

    @Test
    public void testPartitionCheck() {
        assertTrue(new Integer(myRing.getPartition("fiajsnsd")) != null);
        assertTrue(new Integer(myRing.getPartition("324dqe32ce")) != null);
        assertTrue(new Integer(myRing.getPartition("*&BY*OY")) != null);
        assertTrue(new Integer(myRing.getPartition("LP)I#)I")) != null);
    }
}
