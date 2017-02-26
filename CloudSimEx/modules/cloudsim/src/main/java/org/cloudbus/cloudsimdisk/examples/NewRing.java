package org.cloudbus.cloudsimdisk.examples;

/**
 * Created by skulkarni9 on 2/24/17.
 */

import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.*;
import java.lang.*;

public class NewRing
{
    private Map<Integer, Node> nodes;
    private ArrayList<Integer> partitionToNode;
    private Map<Node, List<Integer>> nodeToPartitionMap;
    private Map<Integer, Node> partitionToNodeMap;
    private int replicas;
    private int partitionShift;

    public NewRing(HashMap<Integer,Node> nodes, ArrayList<Integer> partitionToNode, int replicas, int partitionPower)
    {
        this.nodes = nodes;
        this.partitionToNode = partitionToNode;
        this.replicas = replicas;
        this.partitionShift = 32 - partitionPower;
        this.createMappings();
    }

    public Map<Node, List<Integer>> getNodeToPartitionMaping()
    {
        return this.nodeToPartitionMap;
    }

    public Map<Integer, Node> getPartitionToNodeMapping()
    {
        return this.partitionToNodeMap;
    }

    private void createMappings()
    {
        this.nodeToPartitionMap = new HashMap<>();
        this.partitionToNodeMap = new HashMap<>();
        for(int partition=0; partition<this.partitionToNode.size(); partition++)
        {
            Node n = this.nodes.get(this.partitionToNode.get(partition));
            this.partitionToNodeMap.put(partition, n);
            if(nodeToPartitionMap.containsKey(n))
            {
                nodeToPartitionMap.get(n).add(partition);
            }
            else
            {
                List<Integer> partitionList = new ArrayList<>();
                partitionList.add(partition);
                nodeToPartitionMap.put(n, partitionList);
            }
        }
    }

    private long getUnsignedInt(int x)
    {
        return x & 0x00000000ffffffffL;
    }

    private long MD5(String md5)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes("UTF-8"));
            int result = ByteBuffer.wrap(array).order(ByteOrder.BIG_ENDIAN).getInt();
            return getUnsignedInt(result) >> this.partitionShift;
        }
        catch (Exception e)
        {
            new Exception("MD5 Failed");
        }
        return -1l;
    }

    public ArrayList<Node> getNodes(String filePath)
    {
        int part = (int)MD5(filePath);
        ArrayList<Integer> node_ids = new ArrayList<Integer>();
        node_ids.add(this.partitionToNode.get(part));
        ArrayList<Node> zones = new ArrayList<Node>();
        zones.add(this.nodes.get(node_ids.get(0)));
        for(int replica=1; replica<this.replicas; replica++)
        {
            while(node_ids.contains(this.partitionToNode.get(part)) &&
                    zones.contains(this.nodes.get(this.partitionToNode.get(part))))
            {
                part++;
                if(part >= this.partitionToNode.size())
                {
                    part = 0;
                }
            }
            node_ids.add(this.partitionToNode.get(part));
            zones.add(this.nodes.get(node_ids.get(node_ids.size()-1)));
        }
        ArrayList<Node> results = new ArrayList<Node>();
        for(int i : node_ids)
        {
            results.add(this.nodes.get(i));
        }
        return results;
    }

    public ArrayList<Integer> getPartitions(String filePath)
    {
        int part = (int)MD5(filePath);
        ArrayList<Integer> node_ids = new ArrayList<Integer>();
        ArrayList<Integer> partitions = new ArrayList<Integer>();
        partitions.add(part);
        node_ids.add(this.partitionToNode.get(part));
        ArrayList<Node> zones = new ArrayList<Node>();
        zones.add(this.nodes.get(node_ids.get(0)));
        for(int replica=1; replica<this.replicas; replica++)
        {
            while(node_ids.contains(this.partitionToNode.get(part)) &&
                    zones.contains(this.nodes.get(this.partitionToNode.get(part))))
            {
                part++;
                if(part >= this.partitionToNode.size())
                {
                    part = 0;
                }
            }
            partitions.add(part);
            node_ids.add(this.partitionToNode.get(part));
            zones.add(this.nodes.get(node_ids.get(node_ids.size()-1)));
        }
        return partitions;
    }

    public Node getNodeByPartition(int partitionNumber)
    {
        if(this.partitionToNodeMap.containsKey(partitionNumber))
        {
            return this.partitionToNodeMap.get(partitionNumber);
        }
        return null;
    }

    public List<Integer> getPartitionByNode(Node n)
    {
        if(this.nodeToPartitionMap.containsKey(n))
        {
            return this.nodeToPartitionMap.get(n);
        }
        return null;
    }

    public List<Node> getActiveNodes(String filePath)
    {
        List<Node> primaryNodes = new ArrayList<>();
        for(Node n : getNodes(filePath))
        {
            if(!n.getIsSpunDown())
            {
                primaryNodes.add(n);
            }
        }
        return primaryNodes;
    }

    public List<Node> getInactiveNodes(String filePath)
    {
        List<Node> inactiveNodes = new ArrayList<>();
        for(Node n : getNodes(filePath))
        {
            if(n.getIsSpunDown())
            {
                inactiveNodes.add(n);
            }
        }
        return inactiveNodes;
    }

    public ArrayList<Node> getAllNodes()
    {
        return new ArrayList<>(nodes.values());
    }

    public static NewRing buildRing(HashMap<Integer,Node> nodes, int partitionPower, int replicas)
    {
        ArrayList<Integer> partitionToNode = new ArrayList<Integer>();
        long parts = (long) 1 << partitionPower;
        double totalWeight = 0.0;
        for(Node obj : nodes.values())
        {
            totalWeight += obj.getWeight();
        }
        for(Node obj : nodes.values())
        {
            obj.setDesiredParts(parts / totalWeight * obj.getWeight());
        }
        for(int part=0; part<parts; part++)
        {
            Boolean isNotBreak = true;
            for(Node obj : nodes.values())
            {
                if(obj.getDesiredParts() >= 1)
                {
                    obj.decrementDesiredParts();
                    partitionToNode.add(obj.getID());
                    partitionToNode.add(obj.getID());
                    partitionToNode.add(obj.getID());
                    isNotBreak = false;
                    break;
                }
            }
            if(isNotBreak)
            {
                for(Node obj : nodes.values())
                {
                    if(obj.getDesiredParts() >= 0)
                    {
                        obj.decrementDesiredParts();
                        partitionToNode.add(obj.getID());
                        partitionToNode.add(obj.getID());
                        partitionToNode.add(obj.getID());
                        break;
                    }
                }
            }
        }
        Collections.shuffle(partitionToNode);
        System.out.println("partitionTONode");
        for(int i=0; i<partitionToNode.size();i++)
        {
            System.out.println(i+" : "+partitionToNode.get(i));
        }
        NewRing ring = new NewRing(nodes, partitionToNode, replicas, partitionPower);
        return ring;
    }

    public static void main(String[] args)
    {
        File file = new File("files/basic/StagingDiskAndSpinDown/rings.in");
        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            int nodeCount, partitionPower, replicas;
            String data[];
            String filePath = "ABC/DEF/GHI";
            nodeCount = 256;
            partitionPower = 10;
            replicas = 3;
            HashMap<Integer, Node> hm = new HashMap<Integer, Node>();
            for(int i=0; i<nodeCount; i++)
            {
                data = in.readLine().trim().split(" ");
                int id = Integer.parseInt(data[0]);
                int zone = Integer.parseInt(data[1]);
                double weight = Double.parseDouble(data[2]);
                StorageModelHdd hddModel = MyConstants.STORAGE_MODEL_HDD; // model of disks in the persistent storage
                PowerModelHdd hddPowerModel = MyConstants.STORAGE_POWER_MODEL_HDD; // power model of disks
                hm.put(id, new Node(id, zone, weight, hddModel, hddPowerModel));
            }
            NewRing ring = buildRing(hm, partitionPower, replicas);
            Map<Integer, Node> partitionMap = ring.getPartitionToNodeMapping();
            for(Integer p : ring.getPartitions(filePath))
            {
                System.out.println("Node ID : "+partitionMap.get(p).getID()+", Partition : "+p+", File : "+filePath);
            }
        }
        catch (Exception e)
        {
            new Exception("Main");
        }
    }
}