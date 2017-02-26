package org.cloudbus.cloudsimdisk.examples;

import java.util.*;
import java.lang.*;
import java.io.*;

import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;
import java.util.concurrent.ThreadLocalRandom;

public class SpinningDownAlgo3
{
    public static void main(String[] args)
    {
        File file = new File("files/basic/StagingDiskAndSpinDown/rings.in");
        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            int Node_Count, Partition_Power, Replicas;
            String a[];
            Node_Count = 5;
            Partition_Power = 3;
            Replicas = 3;
            HashMap<Integer, Node> hm = new HashMap<Integer, Node>();
            for(int i=0; i<Node_Count; i++)
            {
                a = in.readLine().trim().split(" ");
                int id = Integer.parseInt(a[0]);
                int zone = Integer.parseInt(a[1]);
                double weight = Double.parseDouble(a[2]);
                StorageModelHdd hddModel = MyConstants.STORAGE_MODEL_HDD; // model of disks in the persistent storage
                PowerModelHdd hddPowerModel = MyConstants.STORAGE_POWER_MODEL_HDD; // power model of disks

                hm.put(id, new Node(id, zone, weight, hddModel, hddPowerModel));
            }
            NewRing ring = NewRing.buildRing(hm, Partition_Power, Replicas);

            Map<Node, List<Integer>> nodeToPartitionList = getNodeToPartitionList(ring);
            displayInteger(nodeToPartitionList);
            /*display(nodeToFileList);

            System.out.println("Simulating After Each Operation : ");
            List<List<Node>> result = simulate(
                    "files/basic/SpinningDownAlgo1/idealInputLog.txt",
                    ring.getAllNodes(),
                    ring);

            for(int i=0; i<result.size(); ++i)
            {
                List<Integer> tmp = new ArrayList<>();
                for(Node n : result.get(i))
                {
                    tmp.add(n.getID());
                }

                tmp.sort(Comparator.comparing(Integer::intValue));

                System.out.println("Operation No : "+(i+1)+", No of Nodes spunDownAble : "+tmp.size()+" are : "+tmp);
            }
            */
        }
        catch (Exception e)
        {
            new Exception("Main");
        }
    }

    public static List<Node> analyze(Map<Node, ArrayList<String>> map,
                                     Map<String, Integer> fileCountMap,
                                     int opsCount)
    {
        int N = map.keySet().size();
        int randomNum;

        List<Node> spunDownable = new ArrayList<>();
        List<Node> allNodes = new ArrayList<>();
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
                List<Node> tmp = new ArrayList<Node>();
                tmp.addAll(allNodes);
                for(Node n : tmp)
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

    public static boolean isNodeToBeRemoved(Node node, Map<String, Integer> fileCountMap,
                                            Map<Node, ArrayList<String>> map, int minCount)
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

    public static List<List<Node>> simulate(String filename, ArrayList<Node> nodes, NewRing ring)
    {
        Map<Node, ArrayList<String>> map = new HashMap<>();
        Map<String, Integer> fileCountMap = new HashMap<>();
        List<List<Node>> spunDownSeq = new ArrayList<>();
        for(Node n : nodes)
        {
            map.put(n, new ArrayList<>());
        }
        File file = new File(filename);
        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            int N = Integer.parseInt(in.readLine().trim());
            for(int i=0; i<N; i++)
            {
                String data[] = in.readLine().trim().split(",");
                String ops = data[0];
                String object = data[2];
                if(ops.equalsIgnoreCase("PUT"))
                {
                    for(Node n : ring.getNodes(object))
                    {
                        map.get(n).add(object);
                    }
                    fileCountMap.put(object, 3);
                }
                else if(ops.equalsIgnoreCase("DELETE"))
                {
                    for(Node n : ring.getNodes(object))
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

    public static Map<Node, ArrayList<String>> getNodeToFileList(String filename, ArrayList<Node> nodes, NewRing ring)
    {
        Map<Node, ArrayList<String>> map = new HashMap<>();
        for(Node n : nodes)
        {
            map.put(n, new ArrayList<>());
        }
        File file = new File(filename);
        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            int N = Integer.parseInt(in.readLine().trim());
            for(int i=0; i<N; i++)
            {
                String object = in.readLine().trim().split(",")[2];
                for(Node n : ring.getNodes(object))
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

    public static Map<Node, List<Integer>> getNodeToPartitionList(NewRing ring)
    {
        Map<Node, List<Integer>> map = new HashMap<>();
        for(Node n : ring.getAllNodes())
        {
            map.put(n, ring.getPartitionByNode(n));
        }
        return map;
    }

    public static void display(Map<Node, ArrayList<String>> map)
    {
        ArrayList<Node> nodes = new ArrayList<>(map.keySet());
        nodes.sort(Comparator.comparing(Node::getID));
        for(Node n : nodes)
        {
            System.out.println(n.getID()+" : "+map.get(n).toString());
        }
    }

    public static void displayInteger(Map<Node, List<Integer>> map)
    {
        ArrayList<Node> nodes = new ArrayList<>(map.keySet());
        nodes.sort(Comparator.comparing(Node::getID));
        for(Node n : nodes)
        {
            System.out.println(n.getID()+" : "+map.get(n).toString());
        }
    }
}