package org.cloudbus.cloudsimdisk.examples.SpinDownAlgorithms;

import org.cloudbus.cloudsimdisk.examples.MyRing.MyNode;
import org.cloudbus.cloudsimdisk.examples.MyRing.MyRing;
import org.cloudbus.cloudsimdisk.examples.Node;
import org.cloudbus.cloudsimdisk.examples.Ring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by skulkarni9 on 2/27/17.
 */
public class MySpinDownRandomAlgorithm
{
    public static void main(String args[])
    {
        int nodeCount = 8;
        int partitionPower = 4;
        int replicas = 3;
        double overloadPercent = 10.0;
        String ringInputPath = "modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/MyRing/rings.txt";
        MyRing myRing = MyRing.buildRing(ringInputPath,
                nodeCount, partitionPower, replicas, overloadPercent);

        String operationsInputPath = "modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/"+
                "examples/SpinDownAlgorithms/smallDataset.txt";
        int numberOfInputLines = 400;
        Map<MyNode, List<String>> nodeToFileList = getNodeToFileList(operationsInputPath, myRing, numberOfInputLines);
        display(nodeToFileList);

        System.out.println("Simulating After Each Operation : ");
        List<List<MyNode>> result = simulate(operationsInputPath, myRing, numberOfInputLines);

        for(int i=0; i<result.size(); ++i)
        {
            List<String> tmp = new ArrayList<>();
            for(MyNode n : result.get(i))
            {
                tmp.add(n.getName());
            }

            tmp.sort(Comparator.comparing(String::hashCode));

            System.out.println("Operation No : "+(i+1)+", No of Nodes spunDownAble : "+tmp.size()+" are : "+tmp);
        }
    }

    public static Map<MyNode, List<String>> getNodeToFileList(String filename, MyRing ring, int N)
    {
        List<MyNode> nodes = ring.getAllNodes();
        Map<MyNode, List<String>> map = new HashMap<>();
        for(MyNode n : nodes)
        {
            map.put(n, new ArrayList<>());
        }
        File file = new File(filename);
        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            for(int i=0; i<N; i++)
            {
                String object = in.readLine().trim().split(",")[2];
                for(MyNode n : ring.getPrimaryNodes(object))
                {
                    map.get(n).add(object);
                }
            }
        }
        catch (Exception e)
        {
            new Exception("getNodeToFileList");
        }

        return map;
    }

    public static List<MyNode> analyze(Map<MyNode, List<String>> map,
                                     Map<String, Integer> fileCountMap,
                                     int opsCount)
    {
        int N = map.keySet().size();
        int randomNum;

        List<MyNode> spunDownable = new ArrayList<>();
        List<MyNode> allNodes = new ArrayList<>();
        allNodes.addAll(map.keySet());
        while(N>0)
        {
            randomNum = ThreadLocalRandom.current().nextInt(0, N);
            List<String> files = map.get(allNodes.get(randomNum));
            for(String filename : files)
            {
                fileCountMap.put(filename, fileCountMap.get(filename)-1);
            }
            if(isPolicyValid(fileCountMap, 2))
            {
                N--;
                spunDownable.add(allNodes.get(randomNum));
                allNodes.remove(randomNum);
                List<MyNode> tmp = new ArrayList<>();
                tmp.addAll(allNodes);
                for(MyNode n : tmp)
                {
                    if(isNodeToBeRemoved(n, fileCountMap, map, 2))
                    {
                        N--;
                        allNodes.remove(n);
                    }
                }
            }
            else
            {
                return spunDownable;
            }
        }
        return spunDownable;

    }

    public static boolean isNodeToBeRemoved(MyNode node, Map<String, Integer> fileCountMap,
                                            Map<MyNode, List<String>> map, int minCount)
    {
        for(String filename : map.get(node))
        {
            if(fileCountMap.get(filename) <= minCount)
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isPolicyValid(Map<String, Integer> fileCount, int minCount)
    {
        for(int count : fileCount.values())
        {
            if(count<minCount)
            {
                return false;
            }
        }
        return true;
    }

    public static List<List<MyNode>> simulate(String filename, MyRing ring, int N)
    {
        List<MyNode> nodes = ring.getAllNodes();
        Map<MyNode, List<String>> map = new HashMap<>();
        Map<String, Integer> fileCountMap = new HashMap<>();
        List<List<MyNode>> spunDownSeq = new ArrayList<>();
        for(MyNode n : nodes)
        {
            map.put(n, new ArrayList<>());
        }
        File file = new File(filename);
        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            for(int i=0; i<N; i++)
            {
                String data[] = in.readLine().trim().split(",");
                String ops = data[0];
                String object = data[2];
                if(ops.equalsIgnoreCase("PUT"))
                {
                    for(MyNode n : ring.getPrimaryNodes(object))
                    {
                        map.get(n).add(object);
                    }
                    fileCountMap.put(object, 3);
                }
                else if(ops.equalsIgnoreCase("DELETE"))
                {
                    for(MyNode n : ring.getPrimaryNodes(object))
                    {
                        map.get(n).remove(object);
                    }
                }
                Map<String, Integer> tmpFileCountMap = new HashMap<>();
                for(String str : fileCountMap.keySet())
                {
                    tmpFileCountMap.put(str, fileCountMap.get(str));
                }
                spunDownSeq.add(analyze(map, tmpFileCountMap, i+1));
            }
        }
        catch (Exception e)
        {
            new Exception("getNodeToFileList");
        }
        return spunDownSeq;
    }

    public static void display(Map<MyNode, List<String>> map)
    {
        ArrayList<MyNode> nodes = new ArrayList<>(map.keySet());
        nodes.sort(Comparator.comparing(MyNode::getName));
        for(MyNode n : nodes)
        {
            System.out.println(n.getName()+" : "+map.get(n).toString());
        }
    }
}
