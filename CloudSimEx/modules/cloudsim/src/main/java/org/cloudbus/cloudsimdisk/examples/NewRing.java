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
import java.util.concurrent.ThreadLocalRandom;
import java.lang.*;

public class NewRing
{
    private HashMap<Integer, Node> nodes;
    private ArrayList<Integer> partitionToNode;
    private Map<Node, List<Integer>> nodeToPartitionMap;
    private Map<Integer, Node> partitionToNodeMap;
    private int replicas;
    private int partitionShift;
    private Map<Integer, List<Node>> zoneIdToNodes;
    private Map<String, List<Node>> handOffNodesMap;
    private Map<String, List<Node>> primaryNodesMap;

    public NewRing(HashMap<Integer,Node> nodes, ArrayList<Integer> partitionToNode, int replicas)
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
        this.handOffNodesMap = new HashMap<>();
        this.primaryNodesMap = new HashMap<>();
        this.createMappings();
        System.out.println(this.partitionToNode.size());
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

    public List<Node> getHandOffNodes(String filePath)
    {
        if(this.handOffNodesMap.containsKey(filePath))
        {
            return this.handOffNodesMap.get(filePath);
        }
        return new ArrayList<>();
    }

    public void addHandOffNodesEntry(String filePath, ArrayList<Node> nodes)
    {
        if(this.handOffNodesMap.containsKey(filePath))
        {
            this.handOffNodesMap.get(filePath).addAll(nodes);
        }
        else
        {
            this.handOffNodesMap.put(filePath, nodes);
        }
    }

    public void removeHandOffNodesEntry(String filePath)
    {
        if(this.handOffNodesMap.containsKey(filePath))
        {
            this.handOffNodesMap.remove(filePath);
        }
    }

    public List<Node> getPrimaryNodesNodes(String filePath)
    {
        if(this.primaryNodesMap.containsKey(filePath))
        {
            return this.primaryNodesMap.get(filePath);
        }
        return new ArrayList<>();
    }

    public void addPrimaryNodesEntry(String filePath, ArrayList<Node> nodes)
    {
        if(this.primaryNodesMap.containsKey(filePath))
        {
            this.primaryNodesMap.get(filePath).addAll(nodes);
        }
        else
        {
            this.primaryNodesMap.put(filePath, nodes);
        }
    }

    public void removePrimaryNodesEntry(String filePath)
    {
        if(this.primaryNodesMap.containsKey(filePath))
        {
            this.primaryNodesMap.remove(filePath);
        }
    }

    public Map<String, List<Node>> getActiveNodes(String filePath)
    {
        List<Node> primaryNodes = new ArrayList<>();
        List<Node> currentHandOffNodes = new ArrayList<>();
        Set<Integer> zones = new HashSet<>();
        Map<String, List<Node>> result = new HashMap<>();
        for(Node n : getNodes(filePath))
        {
            if(!n.getIsSpunDown())
            {
                primaryNodes.add(n);
                zones.add(n.getZone());
            }
        }
        if(primaryNodes.size() != 3)
        {
            if(this.handOffNodesMap.containsKey(filePath))
            {
                int NumOfHandOffNodes = 3 - primaryNodes.size();
                if(NumOfHandOffNodes < this.handOffNodesMap.get(filePath).size())
                {
                    for(int i=0; i<NumOfHandOffNodes; i++)
                    {
                        currentHandOffNodes.add(this.handOffNodesMap.get(filePath).get(i));
                    }
                }
                else
                {
                    currentHandOffNodes.addAll(this.handOffNodesMap.get(filePath));
                }
            }
            int tries = 0;
            while (primaryNodes.size() + currentHandOffNodes.size() != 3 && tries != 1000)
            {
                Set<Integer> otherZones = this.zoneIdToNodes.keySet();
                otherZones.removeAll(zones);
                ArrayList<Integer> otherZonesList = new ArrayList<>(otherZones);
                if(otherZonesList.size() != 0)
                {
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
                else if(zones.size() != 0)
                {
                    List newZones = new ArrayList<>(zones);
                    int randomNumber = ThreadLocalRandom.current().nextInt(0, newZones.size());
                    List<Node> nodesFromRandomZone = this.zoneIdToNodes.get(newZones.get(randomNumber));
                    System.out.println("DEBUG : "+nodesFromRandomZone.toString());
                    Collections.shuffle(nodesFromRandomZone);
                    for(Node n : nodesFromRandomZone)
                    {
                        if(!n.getIsSpunDown() && !primaryNodes.contains(n))
                        {
                            currentHandOffNodes.add(n);
                            break;
                        }
                    }
                }
                else
                {
                    System.out.println("Cannot Assign HandOff nodes : Need to Spin UP some disks");
                }
                tries++;
            }
            if(tries == 1000)
            {
                System.out.println("Could Not Assign the HandOff Nodes");
                System.exit(999);
            }
        }
        result.put("primary", primaryNodes);
        result.put("handOff", currentHandOffNodes);
        return result;
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
        NewRing ring = new NewRing(nodes, partitionToNode, replicas);
        return ring;
    }

    public static void main(String[] args)
    {
        File file = new File("files/basic/StagingDiskAndSpinDown/rings.in");
        String filePath = "ABC/DEF/GHIaazz";
        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            int Node_Count, Partition_Power, Replicas;
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
            NewRing ring = buildRing(hm, Partition_Power, Replicas);
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

