package org.cloudbus.cloudsimdisk.examples;

import java.util.*;
import java.lang.*;
import java.security.*;
import java.nio.*;
import java.io.*;

import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;

public class Ring
{
    private HashMap<Integer, Node> nodes;
    private ArrayList<Integer> partitionToNode;
    private int replicas;
    private int partitionShift;
    
    public Ring(HashMap<Integer,Node> nodes, ArrayList<Integer> partitionToNode, int replicas)
    {
        this.nodes = nodes;
        this.partitionToNode = partitionToNode;
        this.replicas = replicas;
        
        int partitionPower = 1;
        while(Math.pow(2.0, (double)partitionPower) < nodes.size())
        {
            partitionPower++;
        }
        if(partitionToNode.size() != Math.pow(2.0, (double)partitionPower))
        {
            new Exception("partitionToPower is not power of 2");
        }
        this.partitionShift = 32 - partitionPower;
    }
    
    public long getUnsignedInt(int x)
    {
        return x & 0x00000000ffffffffL;
    }
    
    public long MD5(String md5)
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

    public ArrayList<Node> getAllNodes()
    {
        ArrayList<Node> allNodes = new ArrayList<>(nodes.values());
        return allNodes;
    }
    public static Ring buildRing(HashMap<Integer,Node> nodes, int partitionPower, int replicas)
    {
        ArrayList<Integer> partitionToNode = new ArrayList<Integer>();
        long parts = (long) 2 << partitionPower;
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
                        break;
                    }
                }
            }
        }
        Collections.shuffle(partitionToNode);
        Ring ring = new Ring(nodes, partitionToNode, replicas);
        return ring;
    }

    public static void main(String[] args)
    {
        File file = new File("sources/org/cloudbus/cloudsimdisk/examples/rings.in");
        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            int Node_Count, Partition_Power, Replicas;
            String filePath = in.readLine().trim();
            String a[] = in.readLine().trim().split(" ");
            Node_Count = Integer.parseInt(a[0]);
            Partition_Power = Integer.parseInt(a[1]);
            Replicas = Integer.parseInt(a[2]);
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
            Ring ring = buildRing(hm, Partition_Power, Replicas);
            for(Node n : ring.getNodes(filePath))
            {
                System.out.println(n);
            }
        }
        catch (Exception e)
        {
            new Exception("Main");
        }
    }
}
