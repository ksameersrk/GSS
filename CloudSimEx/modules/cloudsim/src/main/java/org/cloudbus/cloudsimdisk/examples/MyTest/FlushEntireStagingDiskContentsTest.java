package org.cloudbus.cloudsimdisk.examples.MyTest;

import org.cloudbus.cloudsimdisk.examples.MyRing.MyRing;
import org.cloudbus.cloudsimdisk.examples.MyRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.cloudbus.cloudsimdisk.examples.SimulationScenarios.FlushEntireStagingDiskContents.startSimulation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by spadigi on 4/17/17.
 */
public class FlushEntireStagingDiskContentsTest {
    // node properties
    int totalNoOfNodes = 4;

    // staging disk properties
    boolean addStagingDisk = false;

    int numberOfOperations = 5;
    String distribution = "read intensive";

    // will have a set of predefined workloads , user selects one of them,
    // predefindedWorkloadNumber variable stores the workload id
    int predefindedWorkloadNumber = 1;

    int noOfReplicas = 3; //default 3
    String cachingMechanism = "LRU"; // FIFO also possible
    int HDDType = 1; // basicallly this number is the id for storage and power model, will assign ids to them
    //Scenarios : this part is to be done in front end
    int SSDType = 2;
    int percentageFlushAt = 90;
    int percentageFlushTill = 70;
    boolean realisticSSD = true; // if true the capacity split across reqd no of SSDs, if false single SSD with full capacity
    String pathToWorkload = "files/basic/operations/workload.txt";
    String pathToStartingFileList = "files/basic/operations/startingFileList.txt";
    String pathToInputLog = "files/basic/operations/idealInputLog.txt";
    boolean generateInputLog = false;

    MyRunner runner = getRunner();
    List<Map<String,Object>> diskStats;

    public FlushEntireStagingDiskContentsTest() throws Exception {
    }

    public MyRunner getRunner() throws Exception{
        return runner = startSimulation(totalNoOfNodes, addStagingDisk, numberOfOperations, predefindedWorkloadNumber, noOfReplicas, cachingMechanism, HDDType, SSDType,
                percentageFlushAt, percentageFlushTill, realisticSSD, pathToWorkload, pathToStartingFileList, pathToInputLog, generateInputLog);
    }

    @Before
    public void before() throws Exception
    {

        diskStats = runner.getDiskStats();

    }

    @Test
    public void testSuccessfulRun(){
        assertTrue(runner != null);
        assertTrue(diskStats != null);
    }

    // check if we have got the stats for all HDDs
    @Test
    public void testDiskDataCount() {
        assertEquals(diskStats.size(), totalNoOfNodes);
    }

    @Test
    public void testEnergyCorrectness() {
        assertEquals(diskStats.get(0).get("total energy"), Double.parseDouble(diskStats.get(0).get("idle energy").toString()) + Double.parseDouble(diskStats
                .get(0)
                .get("active energy").toString()));
    }

    // in this scenario no disk should be spun down
    @Test
    public void testWithoutStagingDiskScenarioCharacteristics() {
        assertEquals(diskStats.get(0).get("spun down time"), 0.0);
    }

}
