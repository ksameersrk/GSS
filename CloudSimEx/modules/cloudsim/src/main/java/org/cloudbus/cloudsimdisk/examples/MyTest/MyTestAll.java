package org.cloudbus.cloudsimdisk.examples.MyTest;

import org.cloudbus.cloudsimdisk.examples.SimulationScenarios.FlushEntireStagingDiskContents;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
/**
 * Created by skulkarni9 on 4/16/17.
 */


@RunWith(Suite.class)

@Suite.SuiteClasses({
        MyRingTest.class, MySpindownOptimalAlgoTest.class, FlushEntireStagingDiskContentsTest.class
})

public class MyTestAll {
}