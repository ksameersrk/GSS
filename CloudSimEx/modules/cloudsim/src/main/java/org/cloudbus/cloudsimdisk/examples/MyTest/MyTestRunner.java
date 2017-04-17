package org.cloudbus.cloudsimdisk.examples.MyTest;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Created by skulkarni9 on 4/16/17.
 */
public class MyTestRunner {
    public static void main(String args[])
    {
        Result result = JUnitCore.runClasses(MyTestAll.class);

        for (Failure failure : result.getFailures()) {
            System.out.println("Failure : " +failure.toString());
        }

        System.out.println("Was Successful : "+result.wasSuccessful());
        System.out.println("Test run count : "+result.getRunCount());
        System.out.println("Test failure count : "+result.getFailureCount());
        System.out.println("Test runtime in milliseconds : "+result.getRunTime());
    }
}
