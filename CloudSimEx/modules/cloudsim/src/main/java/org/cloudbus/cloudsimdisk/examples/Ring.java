package org.cloudbus.cloudsimdisk.examples;

import java.lang.reflect.Array;
import java.util.*;
import java.lang.*;
import java.security.*;
import java.nio.*;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;

public class Ring
{
    private HashMap<Integer, Node> nodes;
    private ArrayList<Integer> partitionToNode;
    private int replicas;
    private int partitionShift;
    private Map<Integer, List<Node>> zoneIdToNodes;
    private Map<String, List<Node>> handOffNodes;

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
        this.zoneIdToNodes = this.getZoneIdToNodes();
        this.handOffNodes = new HashMap<>();
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

    public Map<Integer, List<Node>> getZoneIdToNodes()
    {
        Map<Integer, List<Node>> zoneIdToNodes = new HashMap<>();
        for(Node n : this.getAllNodes())
        {
            if(zoneIdToNodes.containsKey(n.getZone()))
            {
                zoneIdToNodes.get(n.getZone()).add(n);
            }
            else
            {
                List<Node> newNodeList = new ArrayList<>();
                newNodeList.add(n);
                zoneIdToNodes.put(n.getZone(), newNodeList);
            }
        }
        return zoneIdToNodes;
    }

    public void deleteHandOffNodes(String filePath)
    {
        if(this.handOffNodes.containsKey(filePath))
        {
            this.handOffNodes.remove(filePath);
        }
    }

    /*
        getActiveNodes takes into account following
            - If all three nodes from ring are active, then return it.
            - If less then 3 nodes are active, then handoff nodes are choosen
              from other zones.
            - An entry of handoff nodes is maintained.
            - If User wishes to delete handOff nodes, then he has to execute
              deleteHandOffNodes(String filePath)
     */
    
    public ArrayList<Node> getActiveNodes(String filePath)
    {
        List<Node> activeNodes = new ArrayList<>();
        List<Node> currentHandOffNodes = new ArrayList<>();
        Set<Integer> zones = new HashSet<>();
        for(Node n : getNodes(filePath))
        {
            if(!n.getIsSpunDown())
            {
                activeNodes.add(n);
                zones.add(n.getZone());
            }
        }
        if(activeNodes.size() != 3)
        {
            if(this.handOffNodes.containsKey(filePath))
            {
                int NumOfHandOffNodes = 3 - activeNodes.size();
                if(NumOfHandOffNodes < this.handOffNodes.get(filePath).size())
                {
                    for(int i=0; i<NumOfHandOffNodes; i++)
                    {
                        currentHandOffNodes.add(this.handOffNodes.get(filePath).get(i));
                    }
                }
                else
                {
                    currentHandOffNodes.addAll(this.handOffNodes.get(filePath));
                }
            }
            while (activeNodes.size() + currentHandOffNodes.size() != 3)
            {
                Set<Integer> otherZones = this.zoneIdToNodes.keySet();
                otherZones.removeAll(zones);
                ArrayList<Integer> otherZonesList = new ArrayList<>(otherZones);
                int randomNumber = ThreadLocalRandom.current().nextInt(0, otherZonesList.size());
                List<Node> nodesFromRandomZone = this.zoneIdToNodes.get(otherZonesList.get(randomNumber));
                Collections.shuffle(nodesFromRandomZone);
                for(Node n : nodesFromRandomZone)
                {
                    if(!n.getIsSpunDown())
                    {
                        currentHandOffNodes.add(n);
                        break;
                    }
                }
            }
            this.handOffNodes.put(filePath, currentHandOffNodes);
        }
        return (new ArrayList<>(activeNodes));
    }

    public ArrayList<Node> getInactiveNodes(String filePath)
    {
        List<Node> inactiveNodes = new ArrayList<>();
        for(Node n : getNodes(filePath))
        {
            if(n.getIsSpunDown())
            {
                inactiveNodes.add(n);
            }
        }
        return (new ArrayList<>(inactiveNodes));
    }

    public ArrayList<Node> getAllNodes()
    {
        return new ArrayList<>(nodes.values());
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
        File file = new File("/Users/skulkarni9/Desktop/8thSem/GSS/CloudSimEx/modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/rings.in");
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
            for(Node n : ring.getActiveNodes(filePath))
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
